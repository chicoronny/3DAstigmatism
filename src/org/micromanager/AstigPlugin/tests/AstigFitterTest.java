package org.micromanager.AstigPlugin.tests;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import net.imglib2.type.numeric.RealType;

class AstigFitterTest<T extends NativeType<T> & RealType<T>> {
	
	private Manager pipe;
	private ImagePlus loc_im;
	private ExecutorService service;
	
	private void setUp() {
		service=Executors.newCachedThreadPool();
		final File file = new File(System.getProperty("user.home")+"/ownCloud/exp-images.tif");
        
		if (file.isDirectory()){
        	FolderOpener fo = new FolderOpener();
        	fo.openAsVirtualStack(true);
        	loc_im = fo.openFolder(file.getAbsolutePath());
        }
        
        if (file.isFile()){
        	loc_im = FileInfoVirtualStack.openVirtual(file.getAbsolutePath());
        }
	
	    if (loc_im==null)
		    return;
		
		final ImageLoader<T> tif = new ImageLoader<T>(loc_im, LemmingUtils.readCameraSettings(System.getProperty("user.home")+"/camera.props"));
		final NMSDetector<T> peak = new NMSDetector<T>(10,6);
		final Fitter<T> fitter = new AstigFitter<T>(6, LemmingUtils.readCSV(System.getProperty("user.home")+"/ownCloud/exp-calb.csv"));
		final SaveLocalizations saver = new SaveLocalizations(new File(System.getProperty("user.home")+"/ownCloud/exp-images.csv"));
		
		pipe = new Manager(service);
		pipe.add(tif);
		pipe.add(peak);
		pipe.add(fitter);
		pipe.add(saver);
		
		pipe.linkModules(tif, peak, true, loc_im.getStackSize());
		pipe.linkModules(peak,fitter);
		pipe.linkModules(fitter,saver);
	}

	@SuppressWarnings({ "rawtypes" })
	public static void main(String[] args) {
		AstigFitterTest mt = new AstigFitterTest();
		mt.setUp();
		mt.pipe.startAndJoin();
		mt.service.shutdown();
	}

}
