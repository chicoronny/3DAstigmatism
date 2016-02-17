package org.micromanager.AstigPlugin.tests;

import java.io.File;
import org.micromanager.AstigPlugin.interfaces.Frame;
import org.micromanager.AstigPlugin.pipeline.Fitter;
import org.micromanager.AstigPlugin.pipeline.ImageLoader;
import org.micromanager.AstigPlugin.pipeline.Manager;
import org.micromanager.AstigPlugin.pipeline.SaveLocalizations;
import org.micromanager.AstigPlugin.plugins.AstigFitter;
import org.micromanager.AstigPlugin.plugins.NMSDetector;
import org.micromanager.AstigPlugin.tools.FileInfoVirtualStack;
import org.micromanager.AstigPlugin.tools.LemmingUtils;

import ij.ImagePlus;
import ij.plugin.FolderOpener;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;

public class ManagerTest<T extends IntegerType<T> & NativeType<T> & RealType<T>, F extends Frame<T>> {
	
	private Manager pipe;
	private ImagePlus loc_im;
	private static String appPath ="D:\\Images\\";
	
	private void setUp() {
		final File file = new File(appPath+"p500ast_.tif");
        
		if (file.isDirectory()){
        	FolderOpener fo = new FolderOpener();
        	fo.openAsVirtualStack(true);
        	loc_im = fo.openFolder(file.getAbsolutePath());
        }
        
        if (file.isFile()){
        	loc_im = FileInfoVirtualStack.openVirtual(file.getAbsolutePath());
        }
	
	    if (loc_im ==null)
		    return;
		
		final ImageLoader<T> tif = new ImageLoader<T>(loc_im, LemmingUtils.readCameraSettings("camera.props"));

		final NMSDetector<T, F> peak = new NMSDetector<T, F>(70,7);
		//Fitter fitter = new QuadraticFitter(10);
		//@SuppressWarnings("unchecked")
		final Fitter<T> fitter = new AstigFitter<T,F>(7, LemmingUtils.readCSV(appPath+"set1-calib.csv").get("param"));
		//final Fitter<T> fitter = new CentroidFitter<T>(7, 100);

		final SaveLocalizations saver = new SaveLocalizations(new File(appPath+"p500ast_.csv"));
		
		pipe = new Manager();
		pipe.add(tif);
		pipe.add(peak);
		pipe.add(fitter);
		pipe.add(saver);
		
		pipe.linkModules(tif, peak, true, loc_im.getStackSize());
		pipe.linkModules(peak,fitter);
		pipe.linkModules(fitter,saver);
		pipe.getMap();
	}

	@SuppressWarnings({ "rawtypes" })
	public static void main(String[] args) {
		ManagerTest mt = new ManagerTest();
		mt.setUp();
		mt.pipe.run();
	}

}
