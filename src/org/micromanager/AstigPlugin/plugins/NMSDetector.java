package org.micromanager.AstigPlugin.plugins;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.micromanager.AstigPlugin.interfaces.Element;
import org.micromanager.AstigPlugin.interfaces.Frame;
import org.micromanager.AstigPlugin.pipeline.Detector;
import org.micromanager.AstigPlugin.pipeline.FrameElements;
import org.micromanager.AstigPlugin.pipeline.Localization;
import org.micromanager.AstigPlugin.tools.LemmingUtils;

public class NMSDetector <T extends RealType<T>, F extends Frame<T>> extends Detector<T,F> {
	
	private double threshold;
	
	private int n_;

	private int counter=0;

	public NMSDetector(final double threshold, final int size) {
		this.threshold = threshold;
		this.n_ = size;
	}
		
	@Override
	public FrameElements<T> detect(F frame) {
		final RandomAccessibleInterval<T> interval = frame.getPixels();
		final RandomAccess<T> ra = interval.randomAccess();
        
        // compute min and max of the Image
        final T max = LemmingUtils.computeMax( Views.iterable(interval));
        double threshold_ = max.getRealDouble() / 100 * threshold;
		
		int i,j,ii,jj,ll,kk;
		int mi,mj;
		boolean failed=false;
		long width_= interval.dimension(0);
		long height_ = interval.dimension(1);
		List<Element> found = new ArrayList<Element>();
	
		for(i=0;i<=width_-1-n_;i+=n_+1){	// Loop over (n+1)x(n+1)
			for(j=0;j<=height_-1-n_;j+=n_+1){
				mi = i;
				mj = j;
				for(ii=i;ii<=i+n_;ii++){	
					for(jj=j;jj<=j+n_;jj++){
						ra.setPosition(new int[]{ii,jj});
						final T first = ra.get().copy();
						ra.setPosition(new int[]{mi,mj});
						final T second = ra.get().copy();
						if (first.compareTo(second) > 0){	
							mi = ii;
							mj = jj;
						}
					}
				}
				failed = false;
				
				Outer:
				for(ll=mi-n_;ll<=mi+n_;ll++){	
					for(kk=mj-n_;kk<=mj+n_;kk++){
						if((ll<i || ll>i+n_) || (kk<j || kk>j+n_)){
							if(ll<width_ && ll>0 && kk<height_ && kk>0){
								ra.setPosition(new int[]{ll,kk});
								T first = ra.get().copy();
								ra.setPosition(new int[]{mi,mj});
								T second = ra.get().copy();
								if(first.compareTo(second) > 0){
									failed = true;
									break Outer;
								}
							}
						}
					}
				}
				if(!failed){
					ra.setPosition(new int[]{mi,mj});
					T value = ra.get().copy();
					if (value.getRealDouble() > threshold_) {
						found.add(new Localization(mi * frame.getPixelDepth(), mj * frame.getPixelDepth(), value.getRealDouble(), frame.getFrameNumber()));
						counter++;
					}
				}
			}			
		}
				
		return new FrameElements<T>(found, frame);
	}
}
