package org.micromanager.AstigPlugin.pipeline;

import java.util.List;

import org.micromanager.AstigPlugin.interfaces.Element;

import ij.ImagePlus;


public abstract class Renderer extends MultiRunModule {
	
	
	protected ImagePlus ip;
	final protected String title = "Histogram Renderer"; // title of the image

	public Renderer() {
		ip = new ImagePlus();
		ip.setTitle(title);
	}
	
	public ImagePlus getImage(){
		return ip;
	}
	
	public void resetInputStore(){
		inputs.clear();
		iterator=null;
	}

	@Override
	public boolean check() {
		return inputs.size()==1;
	}
	
	public abstract void preview(List<Element> previewList);

}
