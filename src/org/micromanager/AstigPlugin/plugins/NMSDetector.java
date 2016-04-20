package org.micromanager.AstigPlugin.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.micromanager.AstigPlugin.interfaces.Element;
import org.micromanager.AstigPlugin.interfaces.Frame;
import org.micromanager.AstigPlugin.pipeline.FrameElements;
import org.micromanager.AstigPlugin.pipeline.Localization;
import org.micromanager.AstigPlugin.pipeline.MultiRunModule;
import org.micromanager.AstigPlugin.tools.LemmingUtils;

public class NMSDetector <T extends RealType<T>> extends MultiRunModule {
	
	private final double threshold;
	private final int n_;
	private final ConcurrentLinkedQueue<Integer> counterList = new ConcurrentLinkedQueue<Integer>();

	public NMSDetector(final double threshold, final int size) {
		this.threshold = threshold;
		this.n_ = size;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Element processData(Element data) {
		final Frame<T> fe = (Frame<T>) data;

		if (fe.isLast()) {
			cancel();
			final FrameElements<T> res = detect(fe);
			counterList.add(res.getList().size());
			res.setLast(true);
			return res;
		}
		
		final FrameElements<T> res = detect(fe);
		if (res != null)
			counterList.add(res.getList().size());
		return res;
	}
	
	@Override
	protected void afterRun() {
		Integer cc=0;
		for (Integer i : counterList)
			cc+=i;
		System.out.println("Detector found "
				+ cc + " peaks in "
				+ (System.currentTimeMillis() - start) + "ms.");
		
		counterList.clear();
	}

	@Override
	public boolean check() {
		return inputs.size()==1 && outputs.size()>=1;
	}
		
	private FrameElements<T> detect(Frame<T> frame) {
		final RandomAccessibleInterval<T> interval = frame.getPixels();
		final RandomAccess<T> ra = interval.randomAccess();
        
        // compute max of the Image
        final T max = LemmingUtils.computeMax(Views.iterable(interval));
        double threshold_ = max.getRealDouble() / 100.0 * threshold;
		
		int i,j,ii,jj,ll,kk;
		int mi,mj;
		boolean failed;
		long width_ = interval.dimension(0);
		long height_ = interval.dimension(1);
		List<Element> found = new ArrayList<Element>();
	
		T first,second = max,third;

		for (i = 0; i <= width_ - 1 - n_; i += n_ + 1) { // Loop over (n+1)x(n+1)
			for (j = 0; j <= height_ - 1 - n_; j += n_ + 1) {
				mi = i;
				mj = j;
				for (ii = i; ii <= i + n_; ii++) {
					for (jj = j; jj <= j + n_; jj++) {
						ra.setPosition(new int[] { ii, jj });
						first = ra.get().copy();
						ra.setPosition(new int[] { mi, mj });
						second = ra.get().copy();
						if (first.compareTo(second) > 0) {
							mi = ii;
							mj = jj;
						}
					}
				}
				failed = false;

				Outer: for (ll = mi - n_; ll <= mi + n_; ll++) {
					for (kk = mj - n_; kk <= mj + n_; kk++) {
						if ((ll < i || ll > i + n_) || (kk < j || kk > j + n_)) {
							if (ll < width_ && ll > 0 && kk < height_ && kk > 0) {
								ra.setPosition(new int[] { ll, kk });
								third = ra.get().copy();
								//ra.setPosition(new int[] { mi, mj });
								//second = ra.get().copy();
								if (third.compareTo(second) > 0) {
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
					}
				}
			}			
		}
				
		return new FrameElements<T>(found, frame);
	}
}
