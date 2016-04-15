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
import org.micromanager.AstigPlugin.tools.FileInfoVirtualStack;
import org.micromanager.AstigPlugin.tools.LemmingUtils;

import ij.ImagePlus;
import ij.plugin.FolderOpener;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class AstigFitterTest<T extends NativeType<T> & RealType<T>> {
	
	private Manager pipe;
	private Map<Integer, Store> storeMap;
	private ImagePlus loc_im;
	
	private void setUp() {
		final File file = new File("/media/backup/ownCloud/set1.tif");
        
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
		
		final ImageLoader<T> tif = new ImageLoader<T>(loc_im, LemmingUtils.readCameraSettings(System.getProperty("user.home")+"camera.props"));
		final NMSDetector<T> peak = new NMSDetector<T>(50,10);
		final Fitter<T> fitter = new AstigFitter<T>(7, LemmingUtils.readCSV("/media/backup/ownCloud/set1-calb.csv"));
		final SaveLocalizations saver = new SaveLocalizations(new File("/media/backup/ownCloud/set1-b.csv"));
		
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
		assertEquals(true,((Store) mt.storeMap.values().iterator().next()).isEmpty());
		assertEquals(true,((Store) mt.storeMap.values().iterator().next()).isEmpty());
	}

}
