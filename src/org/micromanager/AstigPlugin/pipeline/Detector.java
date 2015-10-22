package org.micromanager.AstigPlugin.pipeline;

import net.imglib2.type.numeric.RealType;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.micromanager.AstigPlugin.interfaces.Element;
import org.micromanager.AstigPlugin.interfaces.Frame;;

public abstract class Detector<T extends RealType<T>, F extends Frame<T>> extends MultiRunModule {
	
	private long start;

	//private volatile int counter= 0;
	
	private ConcurrentLinkedQueue<Integer> counterList = new ConcurrentLinkedQueue<Integer>();

	public Detector() {
	}
	
	@Override
	protected void beforeRun() {
		start = System.currentTimeMillis();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Element processData(Element data) {
		F frame = (F) data;
		if (frame == null)
			return null;

		if (frame.isLast()) { // make the poison pill
			cancel();
			FrameElements<T> res = detect(frame);
			res.setLast(true);
			//counter += res.getList().size();
			counterList.add(res.getList().size());
			return res;
		}
		FrameElements<T> res = detect(frame);
		//counter += res.getList().size();
		try{
		counterList.add(res.getList().size());
		} catch (NullPointerException e){}
		return res;
	}
	
	public abstract FrameElements<T> detect(F frame);
		
	
	@Override
	protected void afterRun() {
		Integer cc=0;
		for (Integer i : counterList)
			cc+=i;
		System.out.println("Detector found "
				+ cc + " peaks in "
				+ (System.currentTimeMillis() - start) + "ms.");
	}

	@Override
	public boolean check() {
		return inputs.size()==1 && outputs.size()>=1;
	}

}
