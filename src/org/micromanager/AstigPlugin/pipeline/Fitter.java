package org.micromanager.AstigPlugin.pipeline;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

import org.micromanager.AstigPlugin.interfaces.Element;

public abstract class Fitter<T extends RealType<T>> extends MultiRunModule {

	protected int size;
	private ConcurrentLinkedQueue<Integer> counterList = new ConcurrentLinkedQueue<Integer>();

	public Fitter(int windowSize) {
		this.size = windowSize;
	}

	public int getWindowSize(){
		return size;
	}	

	@SuppressWarnings("unchecked")
	@Override
	public Element processData(Element data) {
		FrameElements<T> fe = (FrameElements<T>) data;

		if (fe.isLast()) {
			cancel();
			process1(fe);	
			return null;
		}
		
		process1(fe);
		return null;
	}

	private void process1(FrameElements<T> data) {
		List<Element> res = fit( data.getList(), data.getFrame().getPixels(), size, data.getFrame().getFrameNumber(), data.getFrame().getPixelDepth());
		counterList.add(res.size());
		for (Element el : res)
			newOutput(el);
	}
	
	public abstract List<Element> fit(List<Element> sliceLocs, RandomAccessibleInterval<T> pixels, long windowSize, 
			long frameNumber, final double pixelDepth);

	@Override
	protected void afterRun() {
		Integer cc=0;
		for (Integer i : counterList)
			cc+=i;
		Localization lastLoc = new Localization(0, 0, 1, 1) ;
		lastLoc.setLast(true);
		newOutput(lastLoc);
		System.out.println("Fitting of "+ cc +" elements done in " + (System.currentTimeMillis() - start)+"ms.");
		counterList.clear();
	}

	@Override
	public boolean check() {
		return inputs.size()==1;
	}
	
//	protected static Roi cropRoi(Rectangle imageRoi, Rectangle curRect) {
//		double x1 = curRect.getMinX() < imageRoi.getMinX() ? imageRoi.getMinX() : curRect.getMinX();
//		double y1 = curRect.getMinY() < imageRoi.getMinY() ? imageRoi.getMinY() : curRect.getMinY();
//		double x2 = curRect.getMaxX() > imageRoi.getMaxX() ? imageRoi.getMaxX() : curRect.getMaxX();
//		double y2 = curRect.getMaxY() > imageRoi.getMaxY() ? imageRoi.getMaxY() : curRect.getMaxY();
//		return new Roi(x1,y1,x2-x1,y2-y1);
//	}
	
	protected static Interval cropInterval(long[] imageMin, long[] imageMax, long[] curMin, long[] curMax ){
		long x1 = curMin[0] < imageMin[0] ? imageMin[0] : curMin[0];
		long y1 = curMin[1] < imageMin[1] ? imageMin[1] : curMin[1];
		long x2 = curMax[0] > imageMax[0] ? imageMax[0] : curMax[0];
		long y2 = curMax[1] > imageMax[1] ? imageMax[1] : curMax[1];	
		return new FinalInterval(new long[] { x1, y1 }, new long[]{ x2, y2 });
	}
}
