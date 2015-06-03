package org.calibration;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.plugin.Duplicator;
import ij.plugin.FolderOpener;
import ij.plugin.ImageCalculator;
import ij.process.ImageProcessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.data.Calibration;
import org.data.FittedPeak;
import org.data.Peak;
import org.data.csvWriter;
import org.filters.MedianFilter;
import org.filters.NMS;
import org.fitter.CentroidFitter;
import org.fitter.LSQFitter;

public class Pipeline implements Runnable {
	private ImagePlus img;
	private int stackSize;
	Map<Integer,Double> thresholds;
	
	private static int CENTROID = 0;
	private static int LSQ = 1;
	private static int LSQCONV = 2;
	
	Properties props;
	private int maxIter;
	private int maxEval;
	
	public Pipeline(String path) {
		props = new Properties();
		try {
			FileInputStream is = new FileInputStream(path);
			props.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void loadImages(String startpath){
		
		JFileChooser fc = new JFileChooser(startpath);
		
		int returnVal = fc.showOpenDialog(new JFrame());
   	 
        if (returnVal != JFileChooser.APPROVE_OPTION) return;
		File file = fc.getSelectedFile();
		String filename = file.getAbsolutePath();
        
		if (file.isDirectory())
			img = FolderOpener.open(filename);
		
		
		if (file.isFile())
			img = new ImagePlus(filename);
		
		stackSize = img.getStackSize();
		
	}
	
	public ImagePlus medianFilter(int sFilter){
		ImagePlus result = new Duplicator().run(img);
		
		// Determine dimensions of the image
		int w = img.getWidth();
		int h = img.getHeight();
		int d = img.getStackSize();

		ImageProcessor[] in = new ImageProcessor[d];
		ImageProcessor[] out = new ImageProcessor[d];
		for(int z = 0; z < d; z++) {
			in[z]  = img.getStack().getProcessor(z + 1);
			out[z] = result.getStack().getProcessor(z + 1);
		}
		
		float[] values = new float[sFilter * sFilter * d];
		int r = sFilter / 2;
		
		for(int z = 0; z < d; z++) {
			for(int y = 0; y < h; y++) {
				for(int x = 0; x < w; x++) {
					int idx = 0;
					for(int k = z - r; k <= z + r; k++) {
						if(k < 0 || k >= d)
							continue;

						for(int j = y - r; j <= y + r; j++) {
							if(j < 0 || j >= h)
								continue;

							for(int i = x - r; i <= x + r; i++) {
								if(i >= 0 && i < w)
									values[idx++] = in[k].getf(i, j);
							}
						}
					}
					MedianFilter.fastmedian(values, idx);
					out[z].setf(x, y, values[idx / 2]);
				}
			}
		}
		return result;
	}
	
	public List<Peak> NMSFinder(){
		List<Peak> peaks = new ArrayList<>();
		NMS nms = new NMS();
		ImageStack is = img.getStack();
		int n = 5;
		int max = 300;
		for(int i=1;i<=stackSize;i++){																															////////// stack length
			ImageProcessor ip = is.getProcessor(i);
			Double cutoff = thresholds.get(i);
			if (cutoff==null) cutoff=0d;
			nms.run(i,ip, n, max, cutoff, false);													
			peaks.addAll(nms.getPeaks());
		}
		return peaks;
	}
	
	protected List<FittedPeak> fitter(List<Peak> peaks, int roi_size, int fitterType){
		
		LSQFitter lsq = new LSQFitter();
		CentroidFitter cf = new CentroidFitter();
		double[] results = new double[4];
		
		ImageStack is = img.getStack();
		int width = img.getWidth();
		int height = img.getHeight();
		List<FittedPeak>fitted = new ArrayList<>();

		
		for (Peak p : peaks){
			int xstart = p.getX()-roi_size;
			int ystart = p.getY()-roi_size;
			int xend = p.getX()+roi_size;
			int yend = p.getY()+roi_size;
			
			// Boundaries
			if(xstart<0)
				xstart = 0;
			if(ystart < 0)
				ystart = 0;
			if(xend >= width)
				xend = width-1;
			if(yend >= height)
				yend = height-1;
			
			if (fitterType==CENTROID)
				results = cf.fitCentroidandWidth(is.getProcessor(p.getSlice()), 
						new Roi(xstart, ystart, xend-xstart, yend-ystart),
						thresholds.get(p.getSlice()).intValue());
			if (fitterType==LSQ)
				results= lsq.fit2Dsingle(is.getProcessor(p.getSlice()), 
						new Roi(xstart, ystart, xend-xstart, yend-ystart), maxIter);
			
			if (fitterType==LSQCONV)
				results= lsq.fit1DGsingle(is.getProcessor(p.getSlice()), 
						new Roi(xstart, ystart, xend-xstart, yend-ystart), maxIter, maxEval);
			p.setSX(results[2]);
			p.setSY(results[3]);
			fitted.add(new FittedPeak(p,results[0],results[1],0));
		}

		return fitted;
	}
	
	protected void getThresholds(){
		thresholds = new HashMap<>();
		ImageStack is = img.getStack();
		for (int i=1; i<=stackSize;i++ ){
			ImageProcessor ip = is.getProcessor(i);
			thresholds.put(i, ip.getStatistics().mean+3*ip.getStatistics().stdDev);
		}
	}

	@Override
	public void run() {
		Calibration cal = new Calibration();
		cal.readCSV(IJ.getDirectory("image")+"_" + props.getProperty("calibrationFile", "calib.csv"));
		maxIter = Integer.parseInt(props.getProperty("maxIter", "1000"));
		maxEval = Integer.parseInt(props.getProperty("maxEval", "1000"));
		loadImages(props.getProperty("startPath", "."));
		ImagePlus imMedian = medianFilter(Integer.parseInt(props.getProperty("filterSize", "10")));
		ImageCalculator calcul = new ImageCalculator(); 
 	    calcul.run("Substract", img, imMedian);
 	    List<Peak> peaks = NMSFinder();
 	    List<FittedPeak> fitted = fitter(peaks, Integer.parseInt(props.getProperty("fitWindowSize", "10")), CENTROID);
 	    csvWriter w = new csvWriter(new File(props.getProperty("ouputPath", ".")));
 	    for (FittedPeak p:fitted)
 	    	w.process(p.toString());
 	    w.close();
	}

}
