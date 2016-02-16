package org.micromanager.AstigPlugin.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Map;

import org.micromanager.AstigPlugin.interfaces.Frame;
import org.micromanager.AstigPlugin.interfaces.Store;
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
	private Map<Integer, Store> storeMap;
	private ImagePlus loc_im;
	private static String appPath ="D:\\Images\\";
	
	private void setUp() {
		final File file = new File(appPath+"set1.tif");
        
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

		final NMSDetector<T, F> peak = new NMSDetector<T, F>(6,9);
		//Fitter fitter = new QuadraticFitter(10);
		//@SuppressWarnings("unchecked")
		final Fitter<T> fitter = new AstigFitter<T,F>(9, LemmingUtils.readCSV(appPath+"set1-calib.csv").get("param"));
		//final Fitter<T> fitter = new CentroidFitter<T>(7, 100);

		final SaveLocalizations saver = new SaveLocalizations(new File(appPath+"set1.csv"));
		
		pipe = new Manager();
		pipe.add(tif);
		pipe.add(peak);
		pipe.add(fitter);
		pipe.add(saver);
		
		pipe.linkModules(tif, peak, true, loc_im.getStackSize());
		pipe.linkModules(peak,fitter);
		pipe.linkModules(fitter,saver);
		storeMap = pipe.getMap();
	}

	@SuppressWarnings({ "rawtypes" })
	public static void main(String[] args) {
		ManagerTest mt = new ManagerTest();
		mt.setUp();
		mt.pipe.run();
		assertEquals(true,((Store) mt.storeMap.values().iterator().next()).isEmpty());
		assertEquals(true,((Store) mt.storeMap.values().iterator().next()).isEmpty());
	}

}
