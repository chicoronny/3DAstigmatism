package org.micromanager.AstigPlugin.plugins;

import ij.process.ShortProcessor;

import java.util.List;

import org.micromanager.AstigPlugin.interfaces.Element;
import org.micromanager.AstigPlugin.pipeline.ElementMap;
import org.micromanager.AstigPlugin.pipeline.LocalizationPrecision3D;
import org.micromanager.AstigPlugin.pipeline.Renderer;
import org.micromanager.AstigPlugin.tools.LemmingUtils;

public class HistogramRenderer extends Renderer {
	
	private int xBins;
	private double xmin;
	private double ymin;
	private long start;
	private double xwidth;
	private double ywidth;
	private double x;
	private double y;
	private double z;
	private int index;
	private long xindex;
	private long yindex;
	private volatile short[] values; // volatile keyword keeps the array on the heap available
	private double xmax;
	private double ymax;
	private double zmin;
	private double zmax;

	public HistogramRenderer(){
		this(256,256,0,256,0,256,0,255);
	}

	public HistogramRenderer(final int xBins, final int yBins, final double xmin, final double xmax, 
			final double ymin, final double ymax, final double zmin, final double zmax) {
		this.xBins = xBins;
		this.xmin = xmin;
		this.ymin = ymin;
		this.xmax = xmax;
		this.ymax = ymax;
		this.xwidth = (xmax - xmin) / xBins;
    	this.ywidth = (ymax - ymin) / yBins;
    	this.zmin = zmin;
    	this.zmax = zmax;
    	if (Runtime.getRuntime().freeMemory()<(xBins*yBins*4)){ 
    		cancel(); return;
    	}
    	values = new short[xBins*yBins];
		ShortProcessor sp = new ShortProcessor(xBins, yBins,values, LemmingUtils.Fire());
		ip.setProcessor(sp);
		ip.updateAndRepaintWindow();
	}
	
	@Override
	protected void beforeRun() {
		start = System.currentTimeMillis();
	}

	@Override
	public Element processData(Element data) {
		if(data == null) return null;
		if(data.isLast())
				cancel();
		if (data instanceof LocalizationPrecision3D){
			LocalizationPrecision3D loc = (LocalizationPrecision3D) data;
			x = (float) loc.getX();
			y = (float) loc.getY();
			z = (float) loc.getZ();
		}
		if (data instanceof ElementMap){
			ElementMap map = (ElementMap) data;
			try{
				x = map.get("x").doubleValue();
				y = map.get("y").doubleValue();
				z = map.get("z").doubleValue();
			} catch (NullPointerException ne) {return null;}
		}
		long rz = StrictMath.round((z - zmin) / (zmax-zmin) * 256) + 1;
		
        if ( (x >= xmin) && (x <= xmax) && (y >= ymin) && (y <= ymax)) {
        	synchronized(this){
		    	xindex = StrictMath.round((x - xmin) / xwidth);
		    	yindex = StrictMath.round((y - ymin) / ywidth);
		    	index = (int) (xindex+yindex*xBins);
		    	if (index>=0 && index<values.length){
		    		if (values[index] > 0)
		    			values[index] = (short)((values[index]+rz+1)/2);
		    		else
		    			values[index] = (short) rz;
		    	}
			} 
        }   
		return null;
	}
	
	@Override
	public void afterRun(){
		double max = ip.getStatistics().histMax;
		ip.getProcessor().setMinAndMax(0, max);
		ip.updateAndDraw();
		System.out.println("Rendering done in "
				+ (System.currentTimeMillis() - start) + "ms.");
	}

	@Override
	public void preview(List<Element> previewList) {
		for(Element el : previewList)
			processData(el);
		ip.updateAndDraw();
	}
	
}
