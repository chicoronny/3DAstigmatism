package org.micromanager.AstigPlugin.pipeline;

import org.micromanager.AstigPlugin.interfaces.Element;
import org.micromanager.AstigPlugin.tools.LemmingUtils;
import org.micromanager.imagedisplay.MMImagePlus;

import ij.process.ImageProcessor;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;

public class MMImageLoader<T extends NumericType<T> & NativeType<T>> extends SingleRunModule{
	
	private int curSlice = 0;
	private MMImagePlus img;
	private int stackSize;
	private long start;
	
	public MMImageLoader(MMImagePlus img) {
		this.img = img;
		stackSize = img.getImageStackSize();
	}
	
	@Override
	public void beforeRun() {
		start = System.currentTimeMillis();
		iterator = outputs.keySet().iterator().next();
	}

	@Override
	public Element processData(Element data) {	
		ImageProcessor ip = img.getStack().getProcessor(++curSlice);
		
		Img<T> theImage = LemmingUtils.wrap(ip);
		
		ImgLib2Frame<T> frame = new ImgLib2Frame<T>(curSlice, ip.getWidth(), ip.getHeight(), theImage);
		if (curSlice >= stackSize){
			frame.setLast(true);
			cancel(); 
			return frame; 
		}
		return frame;
	}
	
	@Override
	public void afterRun(){
		System.out.println("Loading done in " + (System.currentTimeMillis()-start) + "ms.");
	}
	
	public void show(){
		img.show();
	}

	@Override
	public boolean check() {
		return outputs.size()>=1;
	}
}
