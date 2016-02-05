package org.micromanager.AstigPlugin.pipeline;

import java.util.Iterator;

import org.micromanager.AstigPlugin.interfaces.Element;
import org.micromanager.AstigPlugin.interfaces.Frame;
import org.micromanager.AstigPlugin.interfaces.Store;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;

public class ImageMath<T extends RealType<T>> extends SingleRunModule {
	
	public enum operators {
		ADDITION, SUBSTRACTION, MULTIPLICATION, DIVISION, NONE
	}

	private operators operator;
	private int counter;
	private long start;
	private Store inputA;
	private Store inputB;
	private Store output;
	
	public ImageMath(){
	}
	
	public void setOperator(operators op){
		operator = op;
	}
	
	@Override
	protected void beforeRun(){ 
		Iterator<Integer> it = inputs.keySet().iterator();
		try {
			iterator = it.next();							// first input
			inputB = inputs.get(iterator);
			inputA = inputs.get(it.next());
			output = outputs.values().iterator().next(); 	// output
		} catch (Exception ex){
			System.err.println("Input provided not correct!");
			Thread.currentThread().interrupt();
		}
	
		boolean loop = true;
		while(loop){
			int length = 0;									// check for equal number in the two input stores
			for ( Integer key : inputs.keySet()){
				int value = inputs.get(key).getLength();
				if (length == inputs.get(key).getLength())
					loop = false;
				length = value;
			}
			pause(10);
		}
		System.out.println("Image Math - Input ready");
		start = System.currentTimeMillis();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Element processData(Element data) {
		Frame<T> frameB = (Frame<T>) data;
		if (frameB==null){ 
			return null;
		}
		Frame<T> frameA = (Frame<T>) inputA.get();
		if (frameA==null){ 
			inputB.put(frameB);
			return null;
		}
		
		// if no match put it back to inputs
		if (frameA.getFrameNumber() != frameB.getFrameNumber()){
			inputB.put(frameB);
			inputA.put(frameA);
			return null;
		}		
		
		Pair<Frame<T>,Frame<T>> framePair= new ValuePair<Frame<T>, Frame<T>>(frameA,frameB);
		
		if (frameA.isLast()){ // make the poison pill
			ImgLib2Frame<T> lastFrame = process1(framePair);
			lastFrame.setLast(true);
			output.put(lastFrame);
			cancel();
			counter++;
			return null;
		}

		output.put(process1(framePair));
		counter++;		
		
		//if (counter % 100 == 0)
		//	System.out.println("Frames calculated:" + counter);
		return null;
	}

	private ImgLib2Frame<T> process1(Pair<Frame<T>, Frame<T>> framePair) {
		
		RandomAccessibleInterval<T> intervalA = framePair.getA().getPixels();
		RandomAccessibleInterval<T> intervalB = framePair.getB().getPixels();
		
		Cursor<T> cursorA = Views.flatIterable(intervalA).cursor();
		Cursor<T> cursorB = Views.flatIterable(intervalB).cursor();
		
		switch (operator){
		case ADDITION:			
			while ( cursorA.hasNext()){
	            cursorA.fwd();  cursorB.fwd(); // move both cursors forward by one pixel
	            cursorA.get().add(cursorB.get());
	        }			
			break;
		case SUBSTRACTION:		
			while ( cursorA.hasNext()){
	            cursorA.fwd();  cursorB.fwd(); // move both cursors forward by one pixel
	            double val = cursorB.get().getRealDouble() - cursorA.get().getRealDouble();
	            val = val<0?0:val; 				// check for negative values
	            cursorA.get().setReal(val);
	        }
			break;
		case MULTIPLICATION:
			while ( cursorA.hasNext()){
	            cursorA.fwd();  cursorB.fwd(); // move both cursors forward by one pixel
	            cursorA.get().mul(cursorB.get());
	        }
			break;
		case DIVISION:
			while ( cursorA.hasNext()){
	            cursorA.fwd();  cursorB.fwd(); // move both cursors forward by one pixel
	            cursorA.get().div(cursorB.get());
	        }
			break;
		default:
		}
		
		return new ImgLib2Frame<T>(framePair.getA().getFrameNumber(), framePair.getA().getWidth(), framePair.getA().getHeight(), 
				framePair.getA().getPixelDepth(),intervalA);
	}
	
	@Override
	protected void afterRun(){
		System.out.println("Math done with " + counter + " frames in " + (System.currentTimeMillis()-start) + "ms.");
	}

	@Override
	public boolean check() {
		return inputs.size()==2 && outputs.size()>=1;
	}

}
