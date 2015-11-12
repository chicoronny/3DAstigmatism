package org.micromanager.AstigPlugin.tests;

import static org.junit.Assert.*;
import ij.ImagePlus;
import ij.plugin.FolderOpener;

import java.io.File;
import java.util.Map;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;

import org.junit.Before;
import org.junit.Test;
import org.micromanager.AstigPlugin.interfaces.Frame;
import org.micromanager.AstigPlugin.interfaces.Store;
import org.micromanager.AstigPlugin.pipeline.Fitter;
import org.micromanager.AstigPlugin.pipeline.ImageLoader;
import org.micromanager.AstigPlugin.pipeline.Manager;
import org.micromanager.AstigPlugin.pipeline.SaveLocalizationPrecision3D;
import org.micromanager.AstigPlugin.plugins.CentroidFitter;
import org.micromanager.AstigPlugin.plugins.NMSDetector;
import org.micromanager.AstigPlugin.tools.FileInfoVirtualStack;

public class ManagerTest<T extends NumericType<T> & NativeType<T> & RealType<T>, F extends Frame<T>> {
	
	private Manager pipe;
	private Map<Integer, Store> storeMap;
	private ImagePlus loc_im;

	@Before
	public void setUp() throws Exception {
		final File file = new File(System.getProperty("user.home")+"/ownCloud/storm/test_image.tif");
        
		if (file.isDirectory()){
        	FolderOpener fo = new FolderOpener();
        	fo.openAsVirtualStack(true);
        	loc_im = fo.openFolder(file.getAbsolutePath());
        }
        
        if (file.isFile()){
        	loc_im = FileInfoVirtualStack.openVirtual(file.getAbsolutePath());
        }
	
	    if (loc_im ==null)
		    throw new Exception("File not found");
		
		final ImageLoader<T> tif = new ImageLoader<T>(loc_im);

		final NMSDetector<T, F> peak = new NMSDetector<T, F>(100,7);
		//Fitter fitter = new QuadraticFitter(10);
		//@SuppressWarnings("unchecked")
		//Fitter fitter = new Fitter(7, Settings.readCSV(System.getProperty("user.home")+"/ownCloud/storm/calTest.csv").get("param"));
		final Fitter<T> fitter = new CentroidFitter<T>(7, 100);

		final SaveLocalizationPrecision3D saver = new SaveLocalizationPrecision3D(new File(System.getProperty("user.home")+"/ownCloud/storm/fitted3.csv"));
		
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

	@Test
	public void testRun() {
		pipe.run();
		assertEquals(true,storeMap.values().iterator().next().isEmpty());
		assertEquals(true,storeMap.values().iterator().next().isEmpty());
	}

}
