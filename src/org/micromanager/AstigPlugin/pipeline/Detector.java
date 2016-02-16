package org.micromanager.AstigPlugin.pipeline;

import net.imglib2.type.numeric.RealType;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.micromanager.AstigPlugin.interfaces.Element;
import org.micromanager.AstigPlugin.interfaces.Frame;;

public abstract class Detector<T extends RealType<T>, F extends Frame<T>> extends MultiRunModule {
		
	private ConcurrentLinkedQueue<Integer> counterList = new ConcurrentLinkedQueue<Integer>();

	public Detector() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public Element processData(Element data) {
		F fe = (F) data;

		if (fe.isLast()) {
			cancel();
			FrameElements<T> res = detect(fe);
			if (res!=null){
				res.setLast(true);
				counterList.add(res.getList().size());
				return res;
			} else {
				res = new FrameElements<T>(null, fe);
				res.setLast(true);
				return res;
			}
		}
		
		final FrameElements<T> res = detect(fe);
		if (res != null)
			counterList.add(res.getList().size());
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
