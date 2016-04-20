package org.micromanager.AstigPlugin.tools;

import java.io.IOException;
import java.util.Properties;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.VirtualStack;
import ij.io.FileInfo;
import ij.io.FileOpener;
import ij.io.OpenDialog;
import ij.io.TiffDecoder;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

/** This class opens a multi-page TIFF file as a virtual stack. It
implements the File/Import/TIFF Virtual Stack command. */
public class FileInfoVirtualStack extends VirtualStack implements Runnable{
	
	private FileInfo[] info;
	private int nImages;

	/* Default constructor. */
	private FileInfoVirtualStack() {}

	/* Constructs a FileInfoVirtualStack from a FileInfo object. */
	public FileInfoVirtualStack(FileInfo fi) {
		info = new FileInfo[1];
		info[0] = fi;
		ImagePlus imp = open();
		if (imp!=null)
			imp.show();
	}

	/* Constructs a FileInfoVirtualStack from a FileInfo 
		object and displays it if 'show' is true. */
	public FileInfoVirtualStack(FileInfo fi, boolean show) {
		info = new FileInfo[1];
		info[0] = fi;
		ImagePlus imp = open();
		if (imp!=null && show)
			imp.show();
	}

	/** Opens the specified tiff file as a virtual stack. */
	public static ImagePlus openVirtual(String path) {
		OpenDialog  od = new OpenDialog("Open TIFF", path);
		String name = od.getFileName();
		String  dir = od.getDirectory();
		if (name==null)
			return null;
		FileInfoVirtualStack stack = new FileInfoVirtualStack();
		stack.init(dir, name);
		if (stack.info==null)
			return null;
		return stack.open();
	}

	public void run() {
		OpenDialog  od = new OpenDialog("Open TIFF", IJ.getDirectory("current"));
		String name = od.getFileName();
		String  dir = od.getDirectory();
		if (name==null)
			return;
		init(dir, name);
		if (info==null)
			return;
		ImagePlus imp = open();
		if (imp!=null)
			imp.show();
	}
	
	private void init(String dir, String name) {
		if (name.endsWith(".zip")) {
			IJ.error("Virtual Stack", "ZIP compressed stacks not supported");
			return;
		}
		TiffDecoder td = new TiffDecoder(dir, name);
		if (IJ.debugMode) td.enableDebugging();
		IJ.showStatus("Decoding TIFF header...");
		try {
			info = td.getTiffInfo();
		} catch (IOException e) {
			String msg = e.getMessage();
			if (msg==null||msg.equals("")) msg = ""+e;
			IJ.error("TiffDecoder", msg);
			return;
		}
		if (info==null || info.length==0) {
			IJ.error("Virtual Stack", "This does not appear to be a TIFF stack");
			return;
		}
		if (IJ.debugMode)
			IJ.log(info[0].debugInfo);
	}
		
	private ImagePlus open() {
		FileInfo fi = info[0];
		int n = fi.nImages;
		if (info.length==1 && n>1) {
			info = new FileInfo[n];
			long size = fi.width*fi.height*fi.getBytesPerPixel();
			for (int i=0; i<n; i++) {
				info[i] = (FileInfo)fi.clone();
				info[i].nImages = 1;
				info[i].longOffset = fi.getOffset() + i*(size + fi.gapBetweenImages);
			}
		}
		nImages = info.length;
		FileOpener fo = new FileOpener(info[0] );
		ImagePlus imp = fo.open(false);
		if (nImages==1 && fi.fileType==FileInfo.RGB48)
			return imp;
		Properties props = fo.decodeDescriptionString(fi);
		ImagePlus imp2 = new ImagePlus(fi.fileName, this);
		imp2.setFileInfo(fi);
		if (imp!=null && props!=null) {
			setBitDepth(imp.getBitDepth());
			imp2.setCalibration(imp.getCalibration());
			imp2.setOverlay(imp.getOverlay());
			if (fi.info!=null)
				imp2.setProperty("Info", fi.info);
			int channels = getInt(props,"channels");
			int slices = getInt(props,"slices");
			int frames = getInt(props,"frames");
			if (channels*slices*frames==nImages) {
				imp2.setDimensions(channels, slices, frames);
				if (getBoolean(props, "hyperstack"))
					imp2.setOpenAsHyperStack(true);
			}
			if (channels>1 && fi.description!=null) {
				int mode = IJ.COMPOSITE;
				if (fi.description.contains("mode=color"))
					mode = IJ.COLOR;
				else if (fi.description.contains("mode=gray"))
					mode = IJ.GRAYSCALE;
				imp2 = new CompositeImage(imp2, mode);
			}
		}
		return imp2;
	}

	private static int getInt(Properties props, String key) {
		Double n = getNumber(props, key);
		return n!=null?(int)n.doubleValue():1;
	}

	private static Double getNumber(Properties props, String key) {
		String s = props.getProperty(key);
		if (s!=null) {
			try {
				return Double.valueOf(s);
			} catch (NumberFormatException e) {e.printStackTrace();}
		}	
		return null;
	}

	private static boolean getBoolean(Properties props, String key) {
		String s = props.getProperty(key);
		return s != null && s.equals("true");
	}

	/** Deletes the specified image, were 1<=n<=nImages. */
	public void deleteSlice(int n) {
		if (n<1 || n>nImages)
			throw new IllegalArgumentException("Argument out of range: "+n);
		if (nImages<1) return;
		System.arraycopy(info, n, info, n - 1, nImages - n);
		info[nImages-1] = null;
		nImages--;
	}
	
	/** Returns an ImageProcessor for the specified image,
		were 1<=n<=nImages. Returns null if the stack is empty.
	*/
	public ImageProcessor getProcessor(int n) {
		if (n<1 || n>nImages)
			throw new IllegalArgumentException("Argument out of range: "+n);
		//if (n>1) IJ.log("  "+(info[n-1].getOffset()-info[n-2].getOffset()));
		info[n-1].nImages = 1; // why is this needed?
		ImagePlus imp;
		if (IJ.debugMode) {
			long t0 = System.currentTimeMillis();
			FileOpener fo = new FileOpener(info[n-1]);
			imp = fo.open(false);
			IJ.log("FileInfoVirtualStack: "+n+", offset="+info[n-1].getOffset()+", "+(System.currentTimeMillis()-t0)+"ms");
		} else {
			FileOpener fo = new FileOpener(info[n-1]);
			imp = fo.open(false);
		}
		if (imp!=null)
			return imp.getProcessor();
		int w=getWidth(), h=getHeight();
		IJ.log("Read error or file not found ("+n+"): "+info[n-1].directory+info[n-1].fileName);
		switch (getBitDepth()) {
			case 8: return new ByteProcessor(w, h);
			case 16: return new ShortProcessor(w, h);
			case 24: return new ColorProcessor(w, h);
			case 32: return new FloatProcessor(w, h);
			default: return null;
		}
	 }
 
	 /** Returns the number of images in this stack. */
	public int getSize() {
		return nImages;
	}

	/** Returns the label of the Nth image. */
	public String getSliceLabel(int n) {
		if (n<1 || n>nImages)
			throw new IllegalArgumentException("Argument out of range: "+n);
		if (info[0].sliceLabels==null || info[0].sliceLabels.length!=nImages)
			return null;
		return info[0].sliceLabels[n-1];
	}

	public int getWidth() {
		return info[0].width;
	}
	
	public int getHeight() {
		return info[0].height;
	}
	
	/** Adds an image to this stack. */
	public synchronized  void addImage(FileInfo fileInfo) {
		nImages++;
		//IJ.log("addImage: "+nImages+"	"+fileInfo);
		if (info==null)
			info = new FileInfo[250];
		if (nImages==info.length) {
			FileInfo[] tmp = new FileInfo[nImages*2];
			System.arraycopy(info, 0, tmp, 0, nImages);
			info = tmp;
		}
		info[nImages-1] = fileInfo;
	}

}
