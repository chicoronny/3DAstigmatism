package org.micromanager.AstigPlugin.tests;

import java.io.File;
import org.micromanager.AstigPlugin.interfaces.Frame;
import org.micromanager.AstigPlugin.pipeline.AbstractModule;
import org.micromanager.AstigPlugin.pipeline.ImageLoader;
import org.micromanager.AstigPlugin.pipeline.ImageMath;
import org.micromanager.AstigPlugin.pipeline.Manager;
import org.micromanager.AstigPlugin.pipeline.SaveImages;
import org.micromanager.AstigPlugin.plugins.FastMedianFilter;
import org.micromanager.AstigPlugin.tools.FileInfoVirtualStack;
import org.micromanager.AstigPlugin.tools.LemmingUtils;

import ij.ImagePlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;

public class FastMedianTest<T extends IntegerType<T> & NativeType<T> & RealType<T>, F extends Frame<T>> {
	
	private Manager pipe;
	private void setUp() {
		final File file = new File("H:\\Images\\set1.tif");
       
    	ImagePlus loc_im = FileInfoVirtualStack.openVirtual(file.getAbsolutePath());

		final ImageLoader<T> tif = new ImageLoader<T>(loc_im, LemmingUtils.readCameraSettings("camera.props"));
		
		final FastMedianFilter<T> filter = new FastMedianFilter<T>(50, true);
		
		final AbstractModule imsaver = new SaveImages<T,F>("H:\\Images\\test.tif");
		final ImageMath<T> substracter = new ImageMath<T>();
		substracter.setOperator(ImageMath.operators.SUBSTRACTION);
		/*final NMSDetector<T, F> peak = new NMSDetector<T, F>(6,10);
		final Fitter<T> fitter = new AstigFitter<T,F>(10, LemmingUtils.readCSV("H:\\Images\\set1-calib.csv").get("param"));
		//final Fitter<T> fitter = new CentroidFitter<T>(7, 100);

		final SaveLocalizations saver = new SaveLocalizations(new File("H:\\Images\\set1.csv"));*/
		
		pipe = new Manager();
		pipe.add(tif);
		pipe.add(filter);
		pipe.add(substracter);
		pipe.add(imsaver);
		
		pipe.linkModules(tif, filter, true, loc_im.getStackSize());
		pipe.linkModules(tif, substracter);
		pipe.linkModules(filter, substracter);
		pipe.linkModules(substracter, imsaver);
		pipe.getMap();
	}

	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {
		FastMedianTest t = new FastMedianTest();
		t.setUp();
		t.pipe.run();
	}

}
