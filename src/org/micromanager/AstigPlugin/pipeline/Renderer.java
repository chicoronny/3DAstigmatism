package org.micromanager.AstigPlugin.pipeline;

import java.util.List;

import org.micromanager.AstigPlugin.interfaces.Element;

import ij.ImagePlus;


public abstract class Renderer extends MultiRunModule {
	
	protected final ImagePlus ip;
	private final String title = "Histogram Renderer"; // title of the image

	protected Renderer() {
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
	
	@Override
	public void afterRun(){
		double max = ip.getStatistics().histMax;
		ip.getProcessor().setMinAndMax(0, max);
		ip.updateAndRepaintWindow();
		System.out.println("Rendering done in "
				+ (System.currentTimeMillis() - start) + "ms.");
	}
	
	public void preview(List<Element> previewList) {
		for (Element el : previewList)
			processData(el);
		double max = ip.getStatistics().histMax;
		ip.getProcessor().setMinAndMax(0, max);
		ip.updateAndRepaintWindow();
	}
}
