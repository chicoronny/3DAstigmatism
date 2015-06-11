package org.calibration;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.plugin.FolderOpener;
import ij.plugin.ImageCalculator;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import ij.util.ThreadUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.data.Calibration;
import org.data.Chunk;
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
	private Map<Integer,Double> thresholds;
	
	private static int CENTROID = 0;
	private static int LSQ = 1;
	private static int LSQCONV = 2;
	
	public static int INDEX_W0 = 0;
	public static int INDEX_A = 1;
	public static int INDEX_B = 2;
	public static int INDEX_C = 3;
	public static int INDEX_D = 4;
	
	private Properties props;
	private int maxIter;
	private int maxEval;
	private Calibration cal;
	private int refineGrid;
	private int numThreads = Runtime.getRuntime().availableProcessors();
	
	public Pipeline(String path) {
		props = new Properties();
		try {
			FileInputStream is = new FileInputStream(path);
			props.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadImages(String startpath){
		
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
		getThresholds();
		System.out.println("Images loaded and thresholded");
	}
	
	private ImagePlus medianFilter(int sFilter) {
		
		MedianFilter<Integer> mf = new MedianFilter<>();

		// Determine dimensions of the image
		int w = img.getWidth();
		int h = img.getHeight();
		int d = img.getStackSize();

		ImageProcessor[] in = new ImageProcessor[d];
		ShortProcessor result = new ShortProcessor(w,h);
		for (int z = 0; z < d; z++) 
			in[z] = img.getStack().getProcessor(z + 1);		
		
		final Vector< Chunk > chunks = divideIntoChunks( w*h, numThreads  );
		Thread[] threads = ThreadUtil.createThreadArray(numThreads);
		for ( int i = 0; i < threads.length; i++ ){
			final Chunk chunk = chunks.get( i );
			int r = sFilter / 2;
			threads[ i ] = new Thread( "FilterChunk " + i ){
				@Override
				public void run(){	
					long pos = chunk.getStartPosition();
					for ( long step = 0; step < chunk.getLoopSize(); step++ ){
						int y = (int) Math.floorDiv(pos + step, w);
						int x = (int) (pos + step - (y * w));
						List<Integer> values = new ArrayList<>();
						for (int z = 0; z < d; z++) {
							final ImageProcessor k = in[z];
							for (int j = y - r; j <= y + r; j++) {
								if (j < 0 || j >= h)
									continue;
								for (int i = x - r; i <= x + r; i++) {
									if (i >= 0 && i < w)
										values.add(k.get(i, j));
								}
							}
						}
						int median = mf.select(values,values.size()/ 2);
						result.set(x, y, median);
					}
					
				}
			};
		}
		ThreadUtil.startAndJoin(threads);
		
		ImagePlus out = new ImagePlus("",result);
		System.out.println("Images median filtered");
		return out;
	}
	
	private List<Peak> NMSFinder(int n, int max){
		List<Peak> peaks = new ArrayList<>();
		NMS nms = new NMS();
		ImageStack is = img.getStack();
		for(int i=1;i<=stackSize;i++){																															////////// stack length
			ImageProcessor ip = is.getProcessor(i);
			Double cutoff = thresholds.get(i);
			if (cutoff==null) cutoff=0d;
			nms.run(i,ip, n, max, cutoff, false);													
			peaks.addAll(nms.getPeaks());
		}
		System.out.println("Peaks found: " + peaks.size());
		return peaks;
	}
	
	private List<FittedPeak> fitter(List<Peak> peaks, int roi_size, int fitterType){
		final LSQFitter lsq = new LSQFitter();
		final CentroidFitter cf = new CentroidFitter();
		
		final ImageStack is = img.getStack();
		final int width = img.getWidth();
		final int height = img.getHeight();
		List<FittedPeak>fitted = Collections.synchronizedList(new ArrayList<FittedPeak>());
		
		final Vector< Chunk > chunks = divideIntoChunks( peaks.size(), numThreads  );
		Thread[] threads = ThreadUtil.createThreadArray(numThreads);
		for ( int i = 0; i < threads.length; i++ ){
			final Chunk chunk = chunks.get( i );
			threads[ i ] = new Thread( "FitterChunk " + i ){
				@Override
				public void run(){	
					List<Peak> chunkPeaks = peaks.subList((int)chunk.getStartPosition(), (int)(chunk.getStartPosition()+chunk.getLoopSize()));
					for (Peak p : chunkPeaks){
						int xstart = p.getX()-roi_size;
						int ystart = p.getY()-roi_size;
						int xend = p.getX()+roi_size;
						int yend = p.getY()+roi_size;
						double[] results = new double[4];
						
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
						
						double SxSy = results[2]*results[2] - results[3]*results[3];			
						
						fitted.add(new FittedPeak(p,results[0],results[1], calculateZ(SxSy)));
					}
				}
			};
		}

		ThreadUtil.startAndJoin(threads);
		System.out.println("Peaks fitted: " + fitted.size());
		return fitted;
	}
	
	private double calculateZ(final double SxSy){
		final double[] curve = cal.getCalibcurve();
		final double[] zgrid = cal.getZgrid();
		final int end = curve.length-1;
		if(end<1) return 0;
		final double zStep = Math.abs(zgrid[0]-zgrid[1]);
		double max = Double.MIN_VALUE;
		for (double i : zgrid) 
			max = Math.max(max, i);
		double min = Double.MAX_VALUE;
		for (double i : zgrid) 
			min = Math.min(min, i);
		double result = (max-min)/2 + min;
		
		// reuse calibration curve -- we can use this as starting point
		if (SxSy>Math.min(curve[0],curve[end]) && SxSy<Math.max(curve[0],curve[end])){
			double distance = Math.abs(curve[0] - SxSy);
			int idx = 0;
			for(int c = 1; c < curve.length; c++){
			    double cdistance = Math.abs(curve[c] - SxSy);
			    if(cdistance < distance){
			        idx = c;
			        distance = cdistance;
			    }
			}
			double zStart = zgrid[idx] - zStep;
			double zEnd =   zgrid[idx] + zStep;
			double fStep = (zEnd - zStart) / refineGrid;
			// make a finer grid and calculate z for it
			for (double fgrid = zStart ; fgrid<=zEnd; fgrid += fStep){
				double curveWx = valuesWith(fgrid,cal.getparam());
				double curveWy = valuesWith(fgrid,cal.getparam());
				double calib = curveWx*curveWx-curveWy*curveWy;
				double cdistance = Math.abs(calib - SxSy);
				if(cdistance < distance){
					distance = cdistance;
					result = fgrid;
				}
			}
		} else {
			double zStart = 0 - zStep;
			double zEnd =   0 + zStep;
			double fStep = (zEnd - zStart) / (zgrid.length * refineGrid);
			double distance = Math.abs(SxSy);
			// make a fine grid and calculate z for it
			for (double fgrid = zStart ; fgrid<=zEnd; fgrid += fStep){
				double curveWx = valuesWith(fgrid,cal.getparam());
				double curveWy = valuesWith(fgrid,cal.getparam());
				double calib = curveWx*curveWx-curveWy*curveWy;
				double cdistance = Math.abs(calib - SxSy);
				if(cdistance < distance){
					distance = cdistance;
					result = fgrid;
				}
			}
		}	
		return result;
	}
	
	private double valuesWith(double input, double[] params) {
        double b = (input-params[INDEX_C])/params[INDEX_D];
    	double value = params[INDEX_W0]*Math.sqrt(1+b*b+params[INDEX_A]*b*b*b+params[INDEX_B]*b*b*b*b);
        return value;
    }
	
	
	private void getThresholds(){
		thresholds = new HashMap<>();
		ImageStack is = img.getStack();
		for (int i=1; i<=stackSize;i++ ){
			ImageProcessor ip = is.getProcessor(i);
			thresholds.put(i, ip.getStatistics().mean+3*ip.getStatistics().stdDev);
		}
	}
	
	public static Vector< Chunk > divideIntoChunks( final long imageSize, final int numThreads )
	{
		final long threadChunkSize = imageSize / numThreads;
		final long threadChunkMod = imageSize % numThreads;

		final Vector< Chunk > chunks = new Vector< Chunk >();

		for ( int threadID = 0; threadID < numThreads; ++threadID )
		{
			// move to the starting position of the current thread
			final long startPosition = threadID * threadChunkSize;

			// the last thread may has to run longer if the number of pixels
			// cannot be divided by the number of threads
			final long loopSize;
			if ( threadID == numThreads - 1 )
				loopSize = threadChunkSize + threadChunkMod;
			else
				loopSize = threadChunkSize;

			chunks.add( new Chunk( startPosition, loopSize ) );
		}

		return chunks;
	}

	@Override
	public void run() {
		cal = new Calibration();
		cal.readCSV(props.getProperty("calibrationFile", "calib.csv"));
		maxIter = Integer.parseInt(props.getProperty("maxIter", "1000"));
		maxEval = Integer.parseInt(props.getProperty("maxEval", "1000"));
		refineGrid = Integer.parseInt(props.getProperty("refineGrid", "11"));
		loadImages(props.getProperty("startPath", "."));
		ImagePlus imMedian = medianFilter(Integer.parseInt(props.getProperty("filterSize", "5")));
		ImageCalculator calcul = new ImageCalculator(); 
 	    calcul.run("Substract", img, imMedian);
 	    List<Peak> peaks = NMSFinder(Integer.parseInt(props.getProperty("gridSize", "5")),
 	    		Integer.parseInt(props.getProperty("maxNumPeaks", "300")));
 	    List<FittedPeak> fitted = fitter(peaks, Integer.parseInt(props.getProperty("fitWindowSize", "10")), LSQCONV);
 	    csvWriter w = new csvWriter(new File(props.getProperty("ouputPath", ".")));
 	    for (FittedPeak p:fitted)
 	    	w.process(p.toString());
 	    w.close();
 	   System.out.println("wrote result to file: " + props.getProperty("ouputPath"));
	}

}
