package org.micromanager.AstigPlugin.pipeline;

import java.util.List;

import org.micromanager.AstigPlugin.interfaces.Element;
import org.micromanager.AstigPlugin.interfaces.Frame;

public class FrameElements<T> implements Element {
	
	private boolean isLast;
	private List<Element> list;
	private Frame<T> frame;

	public FrameElements(List<Element> list_, Frame<T> f) {
		list = list_;
		frame = f;
	}

	@Override
	public boolean isLast() {
		return isLast;
	}

	@Override
	public void setLast(boolean b) {
		isLast = b;
	}

	public Frame<T> getFrame(){
		return frame;
	}
	
	public List<Element> getList(){
		return list;
	}
}