package org.micromanager.AstigPlugin.tools;

import ij.process.ImageProcessor;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;

import java.awt.Rectangle;
import java.awt.image.IndexColorModel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.special.Erf;
import org.micromanager.AstigPlugin.interfaces.Element;
import org.micromanager.AstigPlugin.pipeline.Localization;

import ij.process.FloatPolygon;

public class LemmingUtils {
	
	public static FloatPolygon convertToPoints(List<Element> me, Rectangle currentRoi, double pixelSize){
		FloatPolygon polygon = new FloatPolygon();
		for (Element el: me){
			Localization loc = (Localization) el;
			polygon.addPoint(loc.getX()/pixelSize+currentRoi.getX(),loc.getY()/pixelSize+currentRoi.getY());
		}
		return polygon;
	}
	
	@SuppressWarnings("unchecked")
	public static  <T extends NativeType<T>> Img<T> wrap(final ImageProcessor imp, final long[] dims){
		final Object ip = imp.getPixels();
		String className = ip.getClass().getName();

		Img<T> theImage = null;
		if (className.contains("[S")) {
			theImage = (Img<T>) ArrayImgs.unsignedShorts((short[]) ip, dims);
		} else if (className.contains("[F")) {
			theImage = (Img<T>) ArrayImgs.floats((float[]) ip, dims);
		} else if (className.contains("[B")) {
			theImage = (Img<T>) ArrayImgs.unsignedBytes((byte[]) ip, dims);
		} else if (className.contains("[I")) {
			theImage = (Img<T>) ArrayImgs.unsignedInts((int[]) ip, dims);
		} else if (className.contains("[D")) {
			theImage = (Img<T>) ArrayImgs.doubles((double[]) ip, dims);
		}
		return theImage;
	}
	
	public static IndexColorModel Fire() {
		byte[] reds = new byte[256]; 
		byte[] greens = new byte[256]; 
		byte[] blues = new byte[256];
		int[] r = {0,0,1,25,49,73,98,122,146,162,173,184,195,207,217,229,240,252,255,255,255,255,255,255,255,255,255,255,255,255,255,255};
		int[] g = {0,0,0,0,0,0,0,0,0,0,0,0,0,14,35,57,79,101,117,133,147,161,175,190,205,219,234,248,255,255,255,255};
		int[] b = {0,61,96,130,165,192,220,227,210,181,151,122,93,64,35,5,0,0,0,0,0,0,0,0,0,0,0,35,98,160,223,255};
		for (int i=0; i<r.length; i++) {
			reds[i] = (byte)r[i];
			greens[i] = (byte)g[i];
			blues[i] = (byte)b[i];
		}
		return new IndexColorModel(8, 256, reds, greens, blues);
	}
	
	public static IndexColorModel Ice() {
		byte[] reds = new byte[256]; 
		byte[] greens = new byte[256]; 
		byte[] blues = new byte[256];
        int[] r = {0,0,0,0,0,0,0,19,29,50,48,79,112,134,158,186,201,217,229,242,250,250,250,250,251,250,250,250,250,251,251,243,230};
        int[] g = {0,156,165,176,184,190,196,193,184,171,162,146,125,107,93,81,87,92,97,95,93,93,90,85,69,64,54,47,35,19,0,4,0};
        int[] b = {0,140,147,158,166,170,176,209,220,234,225,236,246,250,251,250,250,245,230,230,222,202,180,163,142,123,114,106,94,84,64,26,27};
		for (int i=0; i<r.length; i++) {
			reds[i] = (byte)r[i];
			greens[i] = (byte)g[i];
			blues[i] = (byte)b[i];
		}
		return new IndexColorModel(8, 256, reds, greens, blues);
	}
	
	public static IndexColorModel Grays() {
		byte[] r = new byte[256];
		byte[] g = new byte[256];
		byte[] b = new byte[256];
		for(int i=0; i<256; i++) {
			r[i]=(byte)i;
			g[i]=(byte)i;
			b[i]=(byte)i;
		}
		return new IndexColorModel(8, 256, r, g, b);
	}
	
	/*
     * Compute the min and max for any {@link Iterable}, like an {@link Img}.
     *
     * The only functionality we need for that is to iterate. Therefore we need no {@link Cursor}
     * that can localize itself, neither do we need a {@link RandomAccess}. So we simply use the
     * most simple interface in the hierarchy.
     *
     * @param input - the input that has to just be {@link Iterable}
     * @param min - the type that will have min
     * @param max - the type that will have max
	 * @return 
     */
    public static < T extends Comparable< T > & Type< T > > T computeMax(
        final IterableInterval< T > input){
        /// create a cursor for the image (the order does not matter)
        final Cursor< T > cursor = input.cursor();
 
        // initialize min and max with the first image value
        T type = cursor.next();
        T max = type.copy();
 
        // loop over the rest of the data and determine min and max value
        while ( cursor.hasNext() ){
            // we need this type more than once
            type = cursor.next();
 
            if ( type.compareTo( max ) > 0 )
                max.set( type );
        }
        return max;
    }
    
    public static < T extends Comparable< T > & Type< T > > T computeMin(
        final IterableInterval< T > input){
        /// create a cursor for the image (the order does not matter)
        final Cursor< T > cursor = input.cursor();
 
        // initialize min and max with the first image value
        T type = cursor.next();
        T min = type.copy();
 
        // loop over the rest of the data and determine min and max value
        while ( cursor.hasNext() ){
            // we need this type more than once
            type = cursor.next();
 
            if ( type.compareTo( min ) < 0 )
            	min.set( type );
        }
        return min;
    }
    
    public static <T> long computeBin(final Histogram1d<T> hist) {
		long[] histogram = hist.toLongArray();
		// Otsu's threshold algorithm
		// C++ code by Jordan Bevik <Jordan.Bevic@qtiworld.com>
		// ported to ImageJ plugin by G.Landini
		int k, kStar; // k = the current threshold; kStar = optimal threshold
		int L = histogram.length; // The total intensity of the image
		long N1, N; // N1 = # points with intensity <=k; N = total number of
		// points
		long Sk; // The total intensity for all histogram points <=k
		long S;
		double BCV, BCVmax; // The current Between Class Variance and maximum
		// BCV
		double num, denom; // temporary bookkeeping

		// Initialize values:
		S = 0;
		N = 0;
		for (k = 0; k < L; k++) {
			S += k * histogram[k]; // Total histogram intensity
			N += histogram[k]; // Total number of data points
		}

		Sk = 0;
		N1 = histogram[0]; // The entry for zero intensity
		//BCV = 0;
		BCVmax = 0;
		kStar = 0;

		// Look at each possible threshold value,
		// calculate the between-class variance, and decide if it's a max
		for (k = 1; k < L - 1; k++) { // No need to check endpoints k = 0 or k =
			// L-1
			Sk += k * histogram[k];
			N1 += histogram[k];

			// The float casting here is to avoid compiler warning about loss of
			// precision and
			// will prevent overflow in the case of large saturated images
			denom = (double) (N1) * (N - N1); // Maximum value of denom is
			// (N^2)/4 =
			// approx. 3E10

			if (denom != 0) {
				// Float here is to avoid loss of precision when dividing
				num = ((double) N1 / N) * S - Sk; // Maximum value of num =
				// 255*N =
				// approx 8E7
				BCV = (num * num) / denom;
			}
			else BCV = 0;

			if (BCV >= BCVmax) { // Assign the best threshold found so far
				BCVmax = BCV;
				kStar = k;
			}
		}
		// kStar += 1; // Use QTI convention that intensity -> 1 if intensity >=
		// k
		// (the algorithm was developed for I-> 1 if I <= k.)
		return kStar;
	}
    
    public static String doubleArrayToString(double[] array){
		String result ="";
		for (double anArray : array) result += anArray + ",";
		result = result.substring(0, result.length()-1);
		return result;
	}
	
    private static double[] stringToDoubleArray(String line){
		String[] s = line.split(",");
		double[] result = new double[s.length];
		for (int n=0;n<s.length;n++)
			result[n]=Double.parseDouble(s[n].trim());
		return result;
	}
    
    public static Map<String,Object> readCSV(String path){
		Map<String,Object>  map = new HashMap<String, Object>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line=br.readLine();
			final double[] knotsX = stringToDoubleArray(line);
			PolynomialFunction[] polynomsX = new PolynomialFunction[knotsX.length-1];
			for (int n=0;n<polynomsX.length;n++){
				line=br.readLine();
				polynomsX[n]=new PolynomialFunction(stringToDoubleArray(line));
			}
			map.put("psx", new PolynomialSplineFunction(knotsX,polynomsX));
			line=br.readLine();
			if (!line.contains("--")) System.err.println("Corrupt File!");
			line=br.readLine();
			final double[] knotsY = stringToDoubleArray(line);
			PolynomialFunction[] polynomsY = new PolynomialFunction[knotsY.length-1];
			for (int n=0;n<polynomsY.length;n++){
				line=br.readLine();
				polynomsY[n]=new PolynomialFunction(stringToDoubleArray(line));
			}
			map.put("psy", new PolynomialSplineFunction(knotsY,polynomsY));
			line=br.readLine();
			if (!line.contains("--")) System.err.println("Corrupt File!");
			line=br.readLine();
			map.put("z0", Double.parseDouble(line.trim()));
			line=br.readLine();
			map.put("zStep", Double.parseDouble(line.trim()));
			line=br.readLine();
			map.put("zgrid", stringToDoubleArray(line));
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return map;
	}

	public static List<Double> readProps(String path) {
		final Locale curLocale = Locale.getDefault();
		final Locale usLocale = new Locale( "en", "US" ); // setting us locale
		Locale.setDefault( usLocale );
		
		List<Double> params = new ArrayList<Double>();
		try {
			FileReader reader = new FileReader( new File(path) );
			final Properties props = new Properties();
			props.load( reader );
			String[] paramParser = props.getProperty( "FitParameter", "" ).split( "[,\n]" );
			for (String aParamParser : paramParser) params.add(Double.parseDouble(aParamParser.trim()));
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Locale.setDefault( curLocale );
		return params;
	}
	
	public static List<Double> readCameraSettings(String path) {
		final Locale curLocale = Locale.getDefault();
		final Locale usLocale = new Locale( "en", "US" ); // setting us locale
		Locale.setDefault( usLocale );
		List<Double> settings = new ArrayList<Double>();
		
		try {
			FileReader reader = new FileReader( new File(path) );
			final Properties props = new Properties();
			props.load( reader );
			settings.add(Double.parseDouble(props.getProperty( "Offset", "150" )));
			settings.add(Double.parseDouble(props.getProperty( "EM-Gain", "1" )));
			settings.add(Double.parseDouble(props.getProperty( "Conversion", "5" )));	
			settings.add(Double.parseDouble(props.getProperty( "PixelSize", "130" )));			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Locale.setDefault( curLocale );
		return settings;
	}
	
	 public static double erf(double x) {
		return Erf.erf(x);
    }

}
