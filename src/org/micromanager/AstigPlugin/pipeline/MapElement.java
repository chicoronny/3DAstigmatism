package org.micromanager.AstigPlugin.pipeline;

import java.util.HashMap;
import java.util.Map;

import org.micromanager.AstigPlugin.interfaces.Element;

public class MapElement implements Element {
	
	private boolean isLast;
	private Map<String,Object> map = new HashMap<String, Object>();

	public MapElement(Map<String, Object> map) {
		this.map = map;
	}

	@Override
	public boolean isLast() {
		return isLast;
	}

	@Override
	public void setLast(boolean isLast) {
		this.isLast = isLast;
	}


	public Map<String,Object> get(){
		return map;
	}

}
