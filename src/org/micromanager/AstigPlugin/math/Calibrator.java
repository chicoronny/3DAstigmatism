package org.micromanager.AstigPlugin.math;

import java.awt.Rectangle;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.micromanager.AstigPlugin.tools.LemmingUtils;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import ij.util.ThreadUtil;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

/**
* This class handle the calibration by calling the various method of the fitter. Each fit runs on a dedicated thread to allow updating of the GUI.
* 
*/

public class Calibrator<T extends RealType<T> & NativeType<T>> {
	
	private double[] zgrid;									// z positions of the slices in the stack
	private volatile double[] Wx, Wy; 
	private int nSlice;

	private int zstep;
    private int rangeStart, rangeEnd;					// Both ends of the restricted z range and length of the restriction
    private volatile Rectangle roi;
    
	private ImageStack is;
	private BSplines b;	
	private Double offset;
	private Double em_gain;
	private Double conversion;
   
	public Calibrator(ImagePlus im, List<Double> cameraSettings, int zstep, Roi r){
		this.is = im.getStack();
		this.zstep = zstep;
    	this.nSlice = im.getNSlices(); 
    	im.getWidth(); 
    	im.getHeight();
    	this.roi = r.getBounds();
		offset = cameraSettings.get(0);
		em_gain = cameraSettings.get(1);
		conversion = cameraSettings.get(2);
	
    	// Initialize arrays
    	zgrid = new double[nSlice];						// z position of the frames
    	Wx = new double[nSlice];						// width in x of the PSF
    	Wy = new double[nSlice];						// width in y of the PSF
    	b = new BSplines();
	}
	
	
	// ////////////////////////////////////////////////////////////
	// 1D and 2D fits
	public void fitStack() {

		Thread[] threads = ThreadUtil.createThreadArray(Runtime.getRuntime().availableProcessors());
		final AtomicInteger ai = new AtomicInteger(0);

		for (int ithread = 0; ithread < threads.length; ithread++) {

			threads[ithread] = new Thread("fit_" + ithread) {
				@Override
				public void run() {
					for (int i = ai.getAndIncrement(); i < nSlice; i = ai.getAndIncrement()) {
						final ImageProcessor ip = is.getProcessor(i + 1);
						final Img<T> theImage = LemmingUtils.wrap(ip.getPixels(), new long[]{is.getWidth(), is.getHeight()});
						final Cursor<T> it = theImage.cursor();
						while(it.hasNext()){
							it.fwd();
							final double adu = Math.max((it.get().getRealDouble()-offset), 0);
							final double im2phot = adu*conversion/em_gain;
							it.get().setReal(im2phot);
						}
						final IntervalView<T> view = Views.interval(theImage, new long[]{roi.x,roi.y},  new long[]{roi.x+roi.width, roi.y+roi.height});
						final Gaussian2DFitter<T> gf = new Gaussian2DFitter<T>(view, 200, 200);
						double[] results = gf.fit();
						if (results!=null){
							Wx[i]=results[2];
							Wy[i]=results[3];
						}

					}
				}
			};
		}
		ThreadUtil.startAndJoin(threads);

		createZgrid(zgrid, 0);
		fixCurve(Wx);											
		fixCurve(Wy);
		
		b.plot(zgrid, Wx, Wy, "Lateral PSF sizes");
	}	
	
	private static void fixCurve(double[] d) {
		for (int i=1 ; i<d.length-1;i++)
			if (d[i]<0.1) d[i]=(d[i-1]+d[i+1])/2;
	}
	
	public double[] getZgrid(){
		return zgrid;
	}
	
	public void fitBSplines(final int rStart, final int rEnd){
		calculateRange(rStart, rEnd);
		int arraySize = rangeEnd-rangeStart+1;
		final double[] rangedZ = new double[arraySize];
    	final double[] rangedWx = new double[arraySize];
    	final double[] rangedWy = new double[arraySize];
    	
    	System.arraycopy(zgrid, rangeStart, rangedZ, 0, arraySize);
    	System.arraycopy(Wx, rangeStart, rangedWx, 0, arraySize);
    	System.arraycopy(Wy, rangeStart, rangedWy, 0, arraySize); 
    	
    	double maxz = zgrid[zgrid.length-1];
    	
       Thread t = new Thread(new Runnable() {

				@Override
	            public void run() {
			    	b.init(rangedZ, rangedWx, rangedWy);
				
					// Display result
					b.plotWxWyFitCurves();
	            }
	        });
		    t.start();
		    try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}
	
	//////////////////////////////////////////////////////////////
	// Save
	public void saveCalib(String path){
		b.saveAsCSV(path);
	}
	
	//////////////////////////////////////////////////////////////
	// Misc functions

	private void createZgrid(double[] z, int offset){
		for(int i=0;i<z.length;i++)
			z[i] = (i-offset)*zstep;
	}
	
	private void calculateRange(final int rStart, final int rEnd){
		double minStart = Math.abs(rStart-zgrid[0]);
		double minEnd = Math.abs(rEnd-zgrid[nSlice-1]);
		int iStart = 0;
		int iEnd = nSlice-1;
		for(int i=1;i<nSlice;i++){
			if(Math.abs(rStart-zgrid[i])<minStart){
				minStart = Math.abs(rStart-zgrid[i]);
				iStart = i;
			}
			if(Math.abs(rEnd-zgrid[nSlice-1-i])<minEnd){
				minEnd = Math.abs(rEnd-zgrid[nSlice-1-i]);
				iEnd = nSlice-1-i;
			}
		}
		this.rangeStart = iStart;
		this.rangeEnd = iEnd;
	}
	
	public void closePlotWindows() {
		b.closePlotWindows();
	}
	
}













