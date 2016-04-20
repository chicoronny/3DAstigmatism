package org.micromanager.AstigPlugin.math;

import org.micromanager.AstigPlugin.interfaces.FitterInterface;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;

public class CentroidFitterRA<T extends RealType<T>> implements FitterInterface  {
	
	private final IntervalView<T> op;
	private final double thresh;
	private final double[] center;

	public CentroidFitterRA(IntervalView<T> op_, double threshold_){
		op = op_;
		thresh = threshold_;
		center = new double[op.numDimensions()];
		for (int d=0; d<op.numDimensions();++d)
			center[d] = op.min(d)+(op.dimension(d)/2);		
	}
	
	@Override
	public double[] fit(){
		
		Cursor<T> c = op.cursor();
		int n = op.numDimensions();
		
		double [] r = new double[n*2+1];
		double sum = 0;
		
		while (c.hasNext()){
			 c.fwd();
			 
			 double s = c.get().getRealDouble()-thresh;
			 if (s>0){
				 for (int i = 0; i < n; i++){
					 int pos = c.getIntPosition(i);
					 r[i] += (center[i] - pos) * s;
				 }
				 sum = sum + s;
			 }
		}
		
		if (sum == 0) return null;
		
		for (int i = 0; i < n; i++) 
			r[i] = (r[i] / sum) + center[i];
		
		double[] dev = new double[n];
		c.reset();
		while (c.hasNext()){
			c.fwd();
			double s = c.get().getRealDouble()-thresh;
			if (s>0)
				 for (int i = 0; i < n; i++){
					 dev[i] += Math.abs(c.getIntPosition(i)-r[i])*s;
				 }
		}
		
		for (int i = 0; i < n; i++) 
			r[i+n] = Math.sqrt(dev[i]/sum);
		
		RandomAccess<T> ra = op.randomAccess();
		for (int i = 0; i < n; i++){
			ra.setPosition(Math.round(r[i]), i);
		}
		r[n*2] = ra.get().getRealDouble();
		
		return r;		
	}
}
