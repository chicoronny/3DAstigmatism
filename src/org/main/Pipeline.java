package org.main;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.io.FileInfo;
import ij.io.TiffDecoder;
import ij.plugin.FileInfoVirtualStack;
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
import javax.swing.JOptionPane;

import org.data.Calibration;
import org.data.Chunk;
import org.data.FittedPeak;
import org.data.Peak;
import org.data.csvWriter;
import org.filters.MedianFilter;
import org.filters.NMS;
import org.fitter.CentroidFitter;
import org.fitter.Gaussian1DFitter;
//import org.fitter.Gaussian3DFitter;
//import org.fitter.Gaussian2DFitter;
import org.fitter.Gaussian3DFitter2;
import org.fitter.WeightedCentroidFitter;

public class Pipeline implements Runnable {
	private ImageStack img;
	private int stackSize;
	private Map<Integer,Double> thresholds;
	
	public static int INDEX_WX = 0;
	public static int INDEX_WY = 1;
	public static int INDEX_AX = 2;
	public static int INDEX_AY = 3;
	public static int INDEX_BX = 4;
	public static int INDEX_BY = 5;
	public static int INDEX_C = 6;
	public static int INDEX_D = 7;
	public static int INDEX_Mp = 8;
	
	private Properties props;
	private int maxIter;
	private int maxEval;
	private int winsize;
	private Calibration cal;
	private int numThreads = Runtime.getRuntime().availableProcessors();
	private String fitmethod;
	List<FittedPeak> fitted;
	private boolean filter;
	private Thread[] threads;
	private boolean stop =false;
	private int gridSize;
	private int maxNumPeaks;
	private int filterSize;
	private double alpha;
	private double beta;
	
	public Pipeline(String path) {
		props = new Properties();
		try {
			FileInputStream is = new FileInputStream(path);
			props.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Pipeline(Calibration cal, int fsize, String fitmethod, ImageStack img, boolean filter) {
		props = new Properties();
		this.cal = cal;
		maxIter = 100;
		maxEval = 1000;
		this.fitmethod = fitmethod;
		this.winsize = 20;
		this.filterSize = fsize;
		this.img = img;
		this.filter = filter;
	}
	
	
	@SuppressWarnings({ "static-access", "unused" })
	private void loadImages(String startpath){
		
		JFileChooser fc = new JFileChooser(startpath);
		
		int returnVal = fc.showOpenDialog(new JFrame());
   	 
        if (returnVal != JFileChooser.APPROVE_OPTION) return;
		File file = fc.getSelectedFile();
        
		if (file.isDirectory()){
        	FolderOpener fo = new FolderOpener();
        	fo.openAsVirtualStack(true);
        	img = fo.open(file.getAbsolutePath()).getStack();
        }
        
        if (file.isFile()){
        	File dir = file.getParentFile();
        	TiffDecoder td = new TiffDecoder(dir.getAbsolutePath(), file.getName());
        	FileInfo[] info;
			try {info = td.getTiffInfo();}
    		catch (IOException e) {
    			String msg = e.getMessage();
    			if (msg==null||msg.equals("")) msg = ""+e;
    			IJ.error("TiffDecoder", msg);
    			return;
    		}
    		if (info==null || info.length==0) {
    			IJ.error("Virtual Stack", "This does not appear to be a TIFF stack");
    			return;
    		}
        	FileInfoVirtualStack fivs = new FileInfoVirtualStack(info[0], false);
        	img = fivs;
        	System.out.println("Slices: " + fivs.getSize());
        }
		
		stackSize = img.getSize();
		System.out.println("Images loaded");
	}
	
	private ImagePlus medianFilter(int sFilter) {
		
		final MedianFilter<Integer> mf = new MedianFilter<Integer>();

		// Determine dimensions of the image
		final int w = img.getWidth();
		final int h = img.getHeight();
		final int d = img.getSize();

		final ImageProcessor[] in = new ImageProcessor[d];
		final ShortProcessor result = new ShortProcessor(w,h);
		for (int z = 0; z < d; z++) 
			in[z] = img.getProcessor(z + 1);		
		
		final Vector< Chunk > chunks = divideIntoChunks( w*h, numThreads  );
		Thread[] threads = ThreadUtil.createThreadArray(numThreads);
		for ( int i = 0; i < threads.length; i++ ){
			final Chunk chunk = chunks.get( i );
			final int r = sFilter / 2;
			threads[ i ] = new Thread( "FilterChunk " + i ){
				@Override
				public void run(){	
					long pos = chunk.getStartPosition();
					for ( long step = 0; step < chunk.getLoopSize(); step++ ){
						int y = (int) Math.floor((pos + step) / w);
						int x = (int) (pos + step - (y * w));
						List<Integer> values = new ArrayList<Integer>();
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
	
	private List<FittedPeak> gaussian1DFitter(final List<Peak> peaks, ImageProcessor ip, final int size) {
		final List<FittedPeak>fitted = new ArrayList<FittedPeak>();
		final int halfSize =size / 2;
				
		if (peaks.isEmpty()) return fitted;
		
		for (Peak p : peaks) {
			final Roi origroi = new Roi(p.getX() - halfSize, p.getY() - halfSize, size, size);
			final Roi roi = new Roi(ip.getRoi().intersection(origroi.getBounds()));
			Gaussian1DFitter lf = new Gaussian1DFitter(ip, roi, maxIter, maxEval);
			double[] results = null;
			results = lf.fit();
			if (results!=null){
				double SxSy = results[2]*results[2] - results[3]*results[3];			
				fitted.add(new FittedPeak(p.getSlice(),p.getX(),p.getY(),p.getValue(),results[2], results[3], results[0],results[1], calculateZ(SxSy), results[4], results[5]));
			}
		}
					
		return fitted;
	}
	
	private List<FittedPeak> gaussian3DFitter(final List<Peak> peaks, ImageProcessor ip, final int size){
		
		final List<FittedPeak>fitted = new ArrayList<FittedPeak>();
		final int halfSize =size / 2;
							
		if (peaks.isEmpty()) return fitted;
		
		for (Peak p : peaks) {
			final Roi origroi = new Roi(p.getX() - halfSize, p.getY() - halfSize, size, size);
			final Roi roi = new Roi(ip.getRoi().intersection(origroi.getBounds()));
			double[] results = null;
			
			Gaussian3DFitter2 gf = new Gaussian3DFitter2(ip, roi, maxIter, maxEval,cal);
			results = gf.fit();
			
			if (results!=null){
				//double SxSy = results[2]*results[2] - results[3]*results[3];			
				//fitted.add(new FittedPeak(p.getSlice(),p.getX(),p.getY(),p.getValue(),results[2], results[3], results[0],results[1], calculateZ(SxSy), results[4], 0));//results[5]));
				fitted.add(new FittedPeak(p,results[0], results[1], results[2], results[3]));
			}
		}
		return fitted;
	}
	
	private List<FittedPeak> centroidFitter(final List<Peak> peaks, ImageProcessor ip, final int roi_size, int cutoff){
		
		final int width = img.getWidth();
		final int height = img.getHeight();
		final List<FittedPeak>fitted = new ArrayList<FittedPeak>();
	
		for (Peak p : peaks){			
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
				
				results = CentroidFitter.fitCentroidandWidth(ip, new Roi(xstart, ystart, xend-xstart, yend-ystart),	cutoff);
											
				if (results!=null){
					double SxSy = results[2]*results[2] - results[3]*results[3];			
					fitted.add(new FittedPeak(p.getSlice(),p.getX(),p.getY(),p.getValue(),results[2], results[3], results[0],results[1], calculateZ(SxSy),(double) p.getValue(),0));
				}
		}

		return fitted;
	}
	
	private double calculateZ(final double SxSy){
		final double[] curve = cal.getCalibcurve();
		final double[] zgrid = cal.getZgrid();
		final int end = curve.length-1;
		
		if(end < 1) return 0;
		if(curve.length != zgrid.length) return 0;
		
		// reuse calibration curve -- we can use this as starting point
		if (SxSy < Math.min(curve[0],curve[end]) )
			return Math.min(curve[0],curve[end]);
		if (SxSy > Math.max(curve[0],curve[end]) )
			return Math.max(curve[0],curve[end]);

		return calcIterZ(SxSy, Math.min(zgrid[0],zgrid[end]), Math.max(zgrid[0],zgrid[end]), 1e-4);
		
	}
	
	private double calcIterZ(double SxSy, double start, double end, double precision) {
		double zStep = Math.abs(end-start)/10;
		double curveWx = valuesWith(start,cal.getparam())[0];
		double curveWy = valuesWith(start,cal.getparam())[1];
		double calib = curveWx*curveWx-curveWy*curveWy;
		double distance = Math.abs(calib - SxSy);
		double idx = start;
		for ( double c = start+zStep ; c<=end; c += zStep){
			curveWx = valuesWith(c,cal.getparam())[0];
			curveWy = valuesWith(c,cal.getparam())[1];
			calib = curveWx*curveWx-curveWy*curveWy;
		    double cdistance = Math.abs(calib - SxSy);
		    if(cdistance < distance){
		        idx = c;
		        distance = cdistance;
		    }
		}
		if (zStep<=precision){ 
			return idx;
		} else
			return calcIterZ(SxSy,idx - zStep, idx + zStep, precision);
	}

	// with 9 Parameters
	private double[] valuesWith(double z, double[] params) {
		double[] values = new double[2];
		double b;
		
		b = (z-params[INDEX_C]-params[INDEX_Mp])/params[INDEX_D];
		values[0] = params[INDEX_WX]*Math.sqrt(1+b*b+params[INDEX_AX]*b*b*b+params[INDEX_BX]*b*b*b*b);
	
		b = (z+params[INDEX_C]-params[INDEX_Mp])/params[INDEX_D];
		values[1] = params[INDEX_WY]
				* Math.sqrt(1 + b * b + params[INDEX_AY] * b * b * b + params[INDEX_BY] * b * b * b * b);
		
		return values;
	}
	
	
	@SuppressWarnings("unused")
	private void getThresholds(){
		thresholds = new HashMap<Integer, Double>();
		for (int i=1; i<=stackSize;i++ ){
			ImageProcessor ip = img.getProcessor(i);
			//thresholds.put(i, 1*ip.getStatistics().mean+1*ip.getStatistics().stdDev);
			thresholds.put(i, (double) ip.getAutoThreshold());
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

	public void saveCSV(String path){
		 csvWriter w = new csvWriter(new File(path+".csv"));
	 	 for (FittedPeak p:fitted)
	 	    	w.process(p.toStringCSV());
	 	 w.close();
	 	 System.out.println("wrote result to file");
	}
	
	public void saveTXT(String path){
		 csvWriter w = new csvWriter(new File(path+".txt"));
	 	 for (FittedPeak p:fitted)
	 	    	w.process(p.toString());
	 	 w.close();
	 	 System.out.println("wrote result to file");
	}

	public int getNumPeaks(){
		return fitted.size();
	}
	
	public void stopThreads() throws SecurityException {
		stop = true;
		/*for (Thread t : threads)
			t.interrupt();*/
	}
	
	@Override
	public void run() {
		System.out.println("Run pipeline");

		stackSize = img.getSize();

		if (filter) {
			ImagePlus imMedian = medianFilter(filterSize);
			ImageCalculator calcul = new ImageCalculator();
			calcul.run("Substract", new ImagePlus("", img), imMedian);
		}

		gridSize = Integer.parseInt(props.getProperty("gridSize", "10"));
		maxNumPeaks = Integer.parseInt(props.getProperty("maxNumPeaks", "300"));
		alpha = Double.parseDouble(props.getProperty("alpha", "0.2"));
		beta = Double.parseDouble(props.getProperty("beta", "0.4"));

		fitted = Collections.synchronizedList(new ArrayList<FittedPeak>());

		final Vector<Chunk> chunks = divideIntoChunks(stackSize, numThreads);
		threads = ThreadUtil.createThreadArray(numThreads);
		long startTime = System.nanoTime();
		for (int i = 0; i < threads.length; i++) {
			final Chunk chunk = chunks.get(i);
			threads[i] = new Thread("FitterChunk " + i) {
				@Override
				public void run() {
					for (long j = 0; j < chunk.getLoopSize(); j++) {
						if (!stop){
							long frame = chunk.getStartPosition() + j;
	
							if (frame % 100 == 0)
								System.out.println("Frame: " + frame);
							final ImageProcessor ip = img.getProcessor((int) frame+1);
							final double cutoff = ip.getAutoThreshold();
							final NMS nms = new NMS();
							nms.run((int) frame, ip, gridSize, maxNumPeaks, cutoff,	false);
							List<Peak> peaks = nms.getPeaks();
	
							if (fitmethod.equals("1DG")) {
								fitted.addAll(gaussian1DFitter(peaks, ip, winsize));
							} else if (fitmethod.equals("3DG")) {
								fitted.addAll(gaussian3DFitter(peaks, ip, winsize));
							} else if (fitmethod.equals("centroid")){
								fitted.addAll(centroidFitter(peaks, ip, winsize, (int) cutoff));
							} else if (fitmethod.equals("WC")){
								fitted.addAll(WCFitter(peaks,ip, winsize, alpha, beta));
							}
						}
					}
				}
			};
		}
		
		ThreadUtil.startAndJoin(threads);
		long endTime = System.nanoTime();
		
		if(stop){
			System.out.println("Interrupted");
			JOptionPane.showMessageDialog(new JFrame(), "Fitting interrupted.");
		} else {
			System.out.println("Peaks fitted: " + fitted.size());
			JOptionPane.showMessageDialog(new JFrame(), "Fitting done!");
		}
		
		long duration = (endTime - startTime) / 1000000;
		System.out.println("Fitting of " + fitted.size() +" peaks done in "+ duration + "ms");

		if (props.getProperty("ouputPath") != null) {
			csvWriter w = new csvWriter(new File(props.getProperty("ouputPath",	".")));
			for (FittedPeak p : fitted)
				w.process(p.toString());
			w.close();
			System.out.println("Done");
		}
		System.out.println("Done");
	}

	protected List<FittedPeak> WCFitter(List<Peak> peaks, ImageProcessor ip, int roi_size, double alpha, double beta) {
		final int width = img.getWidth();
		final int height = img.getHeight();
		final List<FittedPeak>fitted = new ArrayList<FittedPeak>();
	
		for (Peak p : peaks){			
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
			
			WeightedCentroidFitter wcf = new WeightedCentroidFitter(ip,new Roi(xstart, ystart, xend-xstart, yend-ystart),alpha,beta);
			
			results = wcf.fit();
			
			if (results!=null){
				double SxSy = results[2]*results[2] - results[3]*results[3];			
				fitted.add(new FittedPeak(p.getSlice(),p.getX(),p.getY(),p.getValue(),results[2], results[3], results[0],results[1], calculateZ(SxSy),(double) p.getValue(),0));
			}
		}
		return fitted;
	}

}
