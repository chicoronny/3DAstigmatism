package org.micromanager.AstigPlugin.tests;

import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import org.micromanager.AstigPlugin.pipeline.FrameElements;
import org.micromanager.AstigPlugin.plugins.NMSFastMedian;
import org.micromanager.AstigPlugin.tools.FileInfoVirtualStack;
import org.micromanager.AstigPlugin.tools.LemmingUtils;

import ij.ImagePlus;
import ij.gui.PointRoi;
import ij.gui.StackWindow;
import ij.process.FloatPolygon;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class NMSFastMedianTest<T extends NativeType<T> & RealType<T>> {
		
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
		NMSFastMedian<T> preProcessor =  new NMSFastMedian<T>(100, true, 15, 5);
		List<Double> cameraProps = LemmingUtils.readCameraSettings(System.getProperty("user.home")+"/camera.props");
		
		FrameElements<T> detResults = preProcessor.preview(img, 100, cameraProps);
		if (detResults.getList().isEmpty()) return;
		final FloatPolygon points = LemmingUtils.convertToPoints(detResults.getList(), new Rectangle(0,0,img.getWidth(),img.getHeight()), pixelSize);
		final PointRoi roi = new PointRoi(points);
		previewerWindow.getImagePlus().setRoi(roi);
	}

	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {
		NMSFastMedianTest mt = new NMSFastMedianTest();
		mt.setUp();
	}

}
