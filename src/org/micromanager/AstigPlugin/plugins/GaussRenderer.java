package org.micromanager.AstigPlugin.plugins;

import java.awt.image.IndexColorModel;
import java.util.List;
import java.util.Map;

import ij.process.ImageProcessor;
import ij.process.FloatProcessor;

import org.apache.commons.math3.special.Erf;
import org.apache.commons.math3.util.FastMath;
import org.micromanager.AstigPlugin.factories.RendererFactory;
import org.micromanager.AstigPlugin.gui.ConfigurationPanel;
import org.micromanager.AstigPlugin.gui.GaussRendererPanel;
import org.micromanager.AstigPlugin.interfaces.Element;
import org.micromanager.AstigPlugin.pipeline.ElementMap;
import org.micromanager.AstigPlugin.pipeline.FittedLocalization;
import org.micromanager.AstigPlugin.pipeline.Localization;
import org.micromanager.AstigPlugin.pipeline.Renderer;
import org.scijava.plugin.Plugin;

public class GaussRenderer extends Renderer {
	
	public static final String NAME = "GaussRenderer";
	public static final String KEY = "GAUSSRENDERER";
	public static final String INFO_TEXT = "<html>"
											+ "Gauss Renderer Plugin"
											+ "</html>";
	private double xmin;
	private double xmax;
	private double ymin;
	private double ymax;
//	private double maxVal = -Float.MAX_VALUE;
//	private int counter = 0;
	private long start;
	private double x;
	private double y;
	private double sigmaX;
	private double sigmaY;
	private double xwidth;
	private double ywidth;
	private double xindex;
	private double yindex;
	private volatile float[] pixels;
	private double[] template;
	private static double sqrt2 = FastMath.sqrt(2);
	private static int sizeGauss = 500;
	private static double roiks = 2.5;
	private double sigmaTemplate = sizeGauss/(8*roiks);
	private int xbins;
	private int ybins;

	
	public GaussRenderer(final double xmin, final double xmax, final double ymin, final double ymax, final double pSizeX, final double pSizeY) {
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
		this.xwidth = pSizeX;
		this.ywidth = pSizeY;
		this.xbins = (int) Math.ceil((xmax - xmin) / pSizeX);
    	this.ybins = (int) Math.ceil((ymax - ymin) / pSizeY);
    	if (Runtime.getRuntime().freeMemory()<(pSizeX*pSizeY*4)){ 
    		cancel(); return;
    	}
    	pixels = new float[xbins*ybins];
		ImageProcessor fp = new FloatProcessor(xbins, ybins, pixels, getDefaultColorModel());
		ip.setProcessor(fp);
		ip.updateAndRepaintWindow();
	}
	
	@Override
	protected void beforeRun() {
		start = System.currentTimeMillis();
		template = createGaussTemplate();
	}

	@Override
	public Element processData(Element data) {
		if (data==null) return null;
		if (data.isLast()) {
			cancel();
		}
		if (data instanceof Localization){
			FittedLocalization loc = (FittedLocalization) data;
			x = loc.getX();
			y = loc.getY();
			sigmaX = loc.getsX();
			sigmaY = loc.getsY();
		}
		if (data instanceof ElementMap){
			ElementMap map = (ElementMap) data;
			try{
				x = map.get("x").doubleValue();
				y = map.get("y").doubleValue();
				sigmaX = map.get("sx").doubleValue();
				sigmaY = map.get("sy").doubleValue();
			} catch (NullPointerException ne) {return null;}
		}
		
		if ( (x >= xmin) && (x <= xmax) && (y >= ymin) && (y <= ymax)) {
        	xindex = (x - xmin) / xwidth;
        	yindex = (y - ymin) / ywidth;
        	doWork(xindex, yindex, sigmaX / xwidth, sigmaY / xwidth);
		}
		
		return null;
	}
	
	@Override
	public void afterRun(){
		ip.updateAndDraw();
		System.out.println("Rendering done in "	+ (System.currentTimeMillis() - start) + "ms.");
	}
	
	private void doWork(final double xpix, final double ypix, double sigmaX_, double sigmaY_){
		int w = 2 * sizeGauss + 1;
		double sigmaX = FastMath.max(sigmaX_, sigmaTemplate/sizeGauss);
		double sigmaY = FastMath.max(sigmaY_, sigmaTemplate/sizeGauss);
		long dnx = (long) FastMath.ceil(roiks*sigmaX);
	    long dny = (long) FastMath.ceil(roiks*sigmaY);
	    long xr = FastMath.round(xpix);
	    long yr = FastMath.round(ypix);
	    double dx = xpix-xr;
	    double dy = ypix-yr;
	    double intcorrectionx = Erf.erf(dnx/sigmaX/sqrt2);
	    double intcorrectiony = Erf.erf(dny/sigmaY/sqrt2);
	    double gaussnorm = 1/(2*FastMath.PI*sigmaX*sigmaY*intcorrectionx*intcorrectiony);
	    int idx, t_idx;
		long xt,yt,xax,yax,yp,xp;
		
	    for(xax = -dnx;xax<=dnx;xax++){
	    	xt=FastMath.round((xax+dx)*sigmaTemplate/sigmaX)+sizeGauss;
	    	for(yax=-dny;yax<=dny;yax++){
	    		yt=FastMath.round((yax+dy)*sigmaTemplate/sigmaY)+sizeGauss;
	    		xp=xr+xax; 
	            yp=yr+yax;
	            if (xp>=0 && yp>=0 && xt>=0 && yt>=0 && xt<w && yt<w && xp<xbins && yp<ybins){
	            	idx = (int) (xp + yp * xbins);
	            	t_idx = (int) (xt + yt * w);
	            	double value = template[t_idx] * gaussnorm;
	            	pixels[idx] += value;
	            }
	    	}
	    }
		
	}
	
	private double[] createGaussTemplate(){
		int w = 2 * sizeGauss + 1;
		double[] T = new double[w*w];
		int index = 0;
		double value = 0;
		double factor = 0.5/Math.pow(sigmaTemplate, 2);
		for (int yg = -sizeGauss ; yg<=sizeGauss; yg++)
			for(int xg = -sizeGauss; xg <= sizeGauss; xg++){
				index = (xg+sizeGauss) + (yg+sizeGauss) * w;
				value = Math.exp(-(xg*xg+yg*yg)*factor);
				T[index] = value;
			}
		return T;
	}

	@Override
	public boolean check() {
		return inputs.size()==1;
	}
	
	@Override
	public void preview(List<Element> previewList) {
		for(Element el : previewList)
			processData(el);
		ip.updateAndDraw();
	}
	
	@Plugin( type = RendererFactory.class, visible = true )
	public static class Factory implements RendererFactory{

		private Map<String, Object> settings;
		private GaussRendererPanel configPanel = new GaussRendererPanel();

		@Override
		public String getInfoText() {
			return INFO_TEXT;
		}

		@Override
		public String getKey() {
			return KEY;
		}

		@Override
		public String getName() {
			return NAME;
		}
		@Override
		public boolean setAndCheckSettings(Map<String, Object> settings) {
			this.settings = settings;
			configPanel.setSettings(settings);
			return settings!= null;
		}

		@Override
		public Renderer getRenderer() {
			final int pSizeX = (Integer) settings.get(RendererFactory.KEY_pSizeX);
			final int pSizeY = (Integer) settings.get(RendererFactory.KEY_pSizeY);
			final double xmin = (Double) settings.get(RendererFactory.KEY_xmin);
			final double xmax = (Double) settings.get(RendererFactory.KEY_xmax);
			final double ymin = (Double) settings.get(RendererFactory.KEY_ymin);
			final double ymax = (Double) settings.get(RendererFactory.KEY_ymax);
			return new GaussRenderer(xmin, xmax, ymin, ymax, pSizeX, pSizeY);
		}

		@Override
		public ConfigurationPanel getConfigurationPanel() {
			configPanel.setName(KEY);
			return configPanel;
		}

		@Override
		public Map<String, Object> getInitialSettings() {
			return configPanel.getInitialSettings();
		}
		
	}
	
	private static IndexColorModel getDefaultColorModel() {
		byte[] r = new byte[256];
		byte[] g = new byte[256];
		byte[] b = new byte[256];
		for(int i=0; i<256; i++) {
			r[i]=(byte)i;
			g[i]=(byte)i;
			b[i]=(byte)i;
		}
		return new IndexColorModel(8, 256, r, g, b);
	}

}
