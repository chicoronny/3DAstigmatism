package org.micromanager.AstigPlugin.tests;

import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import org.micromanager.AstigPlugin.pipeline.FrameElements;
import org.micromanager.AstigPlugin.pipeline.ImgLib2Frame;
import org.micromanager.AstigPlugin.plugins.NMSDetector;
import org.micromanager.AstigPlugin.tools.FileInfoVirtualStack;
import org.micromanager.AstigPlugin.tools.LemmingUtils;

import ij.ImagePlus;
import ij.gui.PointRoi;
import ij.gui.StackWindow;
import ij.process.FloatPolygon;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class NMSTest<T extends NativeType<T> & RealType<T>> {
		
	@SuppressWarnings("unchecked")
	private void setUp() {
		final File file = new File(System.getProperty("user.home")+"/ownCloud/exp-images.tif");
		final ImagePlus loc_im  = FileInfoVirtualStack.openVirtual(file.getAbsolutePath());
		final double pixelSize = loc_im.getCalibration().pixelDepth;
		final StackWindow previewerWindow = new StackWindow(loc_im, loc_im.getCanvas());
		previewerWindow.setImage(loc_im);
		previewerWindow.getCanvas().fitToWindow();
		previewerWindow.repaint();
		previewerWindow.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (previewerWindow != null) previewerWindow.close();
			}
		});
		final ImagePlus img = previewerWindow.getImagePlus();
		img.setSlice(150);
		img.setDisplayRange(0, 8000);
		NMSDetector<T> detector =  new NMSDetector<T>(15, 5);
		
		Img<T> curImage = LemmingUtils.wrap(img.getStack().getProcessor(150).getPixels(), new long[]{img.getWidth(), img.getHeight()});
		final ImgLib2Frame<T> curFrame = new ImgLib2Frame<T>(
				150, (int) curImage.dimension(0), (int) curImage.dimension(1), pixelSize, curImage);
		FrameElements<T> detResults = (FrameElements<T>) detector.preview(curFrame);
		if (detResults.getList().isEmpty()) return;
		final FloatPolygon points = LemmingUtils.convertToPoints(detResults.getList(), new Rectangle(0,0,img.getWidth(),img.getHeight()), pixelSize);
		final PointRoi roi = new PointRoi(points);
		previewerWindow.getImagePlus().setRoi(roi);
	}

	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {
		NMSTest mt = new NMSTest();
		mt.setUp();
	}

}
