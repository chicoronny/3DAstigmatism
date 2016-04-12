package org.micromanager.AstigPlugin.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Executors;

import org.micromanager.AstigPlugin.interfaces.Store;
import org.micromanager.AstigPlugin.pipeline.Fitter;
import org.micromanager.AstigPlugin.pipeline.ImageLoader;
import org.micromanager.AstigPlugin.pipeline.Manager;
import org.micromanager.AstigPlugin.pipeline.SaveLocalizations;
import org.micromanager.AstigPlugin.plugins.AstigFitter;
import org.micromanager.AstigPlugin.plugins.NMSDetector;
import org.micromanager.AstigPlugin.plugins.NMSFastMedian;
import org.micromanager.AstigPlugin.tools.FileInfoVirtualStack;
import org.micromanager.AstigPlugin.tools.LemmingUtils;

import ij.ImagePlus;
import ij.plugin.FolderOpener;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;

public class AstigFitterTest<T extends IntegerType<T> & NativeType<T> & RealType<T>> {
	
	private Manager pipe;
	private Map<Integer, Store> storeMap;
	private ImagePlus loc_im;
	
	private void setUp() {
		String sim = "C:/Users/Ries/Documents/PluginTest/MT3d/150";
		String scal = "C:/Users/Ries/Documents/PluginTest/MT3d/cal.csv";
		String sres = "C:/Users/Ries/Documents/PluginTest/MT3d/150-fitonraw.txt";
		
		final File file = new File(sim);
        
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
		
		final ImageLoader<T> tif = new ImageLoader<T>(loc_im, LemmingUtils.readCameraSettings("C:/Users/Ries/git/3DAstigmatism2/camera.props"));
		final NMSFastMedian<T> peak = new NMSFastMedian<T>(50, false, 2,15);
		final Fitter<T> fitter = new AstigFitter<T>(15, LemmingUtils.readCSV(scal));

		final SaveLocalizations saver = new SaveLocalizations(new File(sres));
		
		pipe = new Manager(Executors.newCachedThreadPool());
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
		AstigFitterTest mt = new AstigFitterTest();
		mt.setUp();
		mt.pipe.startAndJoin();
	//	assertEquals(true,((Store) mt.storeMap.values().iterator().next()).isEmpty());
	//	assertEquals(true,((Store) mt.storeMap.values().iterator().next()).isEmpty());
	}

}
