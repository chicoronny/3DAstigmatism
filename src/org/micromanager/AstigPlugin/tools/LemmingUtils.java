package org.micromanager.AstigPlugin.tools;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;

import java.awt.image.IndexColorModel;
import java.util.List;

import org.micromanager.AstigPlugin.interfaces.Element;
import org.micromanager.AstigPlugin.pipeline.Localization;

import ij.process.FloatPolygon;

public class LemmingUtils {
	
	public static FloatPolygon convertToPoints(List<Element> me, double pixelSize){
		FloatPolygon polygon = new FloatPolygon();
		for (Element el: me){
			Localization loc = (Localization) el;
			polygon.addPoint(loc.getX()/pixelSize,loc.getY()/pixelSize);
		}
		return polygon;
	}
	
	@SuppressWarnings("unchecked")
	public static  <T extends NativeType<T>> Img<T> wrap(Object ip, long[] dims){
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
	
	/**
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

}
