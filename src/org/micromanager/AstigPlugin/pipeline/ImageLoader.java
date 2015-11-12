package org.micromanager.AstigPlugin.pipeline;

import org.micromanager.AstigPlugin.interfaces.Element;
import org.micromanager.AstigPlugin.tools.LemmingUtils;

import ij.ImagePlus;
import ij.ImageStack;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;

public class ImageLoader<T extends NumericType<T> & NativeType<T>> extends SingleRunModule{
	
	private int curSlice = 0;
	private ImageStack img;
	private int stackSize;
	private double pixelDepth;
	
	public ImageLoader(ImagePlus loc_im) {
		this.img = loc_im.getStack();
		stackSize = loc_im.getNSlices()*loc_im.getNFrames()*loc_im.getNChannels();
		pixelDepth = loc_im.getCalibration().pixelDepth == 0 ? 1 : loc_im.getCalibration().pixelDepth;
	}
	
	@Override
	public void beforeRun() {
		iterator = outputs.keySet().iterator().next();
		start = System.currentTimeMillis();
	}

	@Override
	public Element processData(Element data) {
		Object ip = img.getPixels(++curSlice);
		
		Img<T> theImage = LemmingUtils.wrap(ip, new long[]{img.getWidth(), img.getHeight()});
		ImgLib2Frame<T> frame = new ImgLib2Frame<T>(curSlice, img.getWidth(), img.getHeight(), pixelDepth, theImage);
		
		if (curSlice >= stackSize){
			frame.setLast(true);
			cancel(); 
			return frame; 
		}
		return frame;
	}
	
	@Override
	public void afterRun(){
		System.out.println("Loading of " + stackSize +" done in " + (System.currentTimeMillis()-start) + "ms.");
	}
	
	public void show(){
		new ImagePlus("",img).show();
	}

	@Override
	public boolean check() {
		return outputs.size()>=1;
	}
}
