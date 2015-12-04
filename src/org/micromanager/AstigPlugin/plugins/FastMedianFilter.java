package org.micromanager.AstigPlugin.plugins;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.micromanager.AstigPlugin.interfaces.Element;
import org.micromanager.AstigPlugin.interfaces.Frame;
import org.micromanager.AstigPlugin.math.QuickSelect;
import org.micromanager.AstigPlugin.pipeline.ImgLib2Frame;
import org.micromanager.AstigPlugin.pipeline.SingleRunModule;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.view.Views;

public class FastMedianFilter<T extends IntegerType<T> & NativeType<T>> extends SingleRunModule {
	
	public static final String NAME = "Fast Median Filter";

	public static final String KEY = "FASTMEDIAN";

	public static final String INFO_TEXT = "<html>" + "Fast Median Filter with the option to interpolate between blocks" + "</html>";

	private int nFrames;

	private boolean interpolating;
	
	private Queue<Frame<T>> frameList = new ArrayDeque<Frame<T>>();

	private int counter = 0;

	private int lastListSize = 0;

	private Frame<T> frameA = null;
	
	private Frame<T> frameB = null;

	public FastMedianFilter(final int numFrames, boolean interpolating) {
		this.nFrames = numFrames;
		this.interpolating = interpolating;
	}

	@Override
	public boolean check() {
		return inputs.size() == 1 && outputs.size() >= 1;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Element processData(Element data) {
		final Frame<T> frame = (Frame<T>) data;
		
		frameList.add(frame);
		counter++;
		
		if (frame.isLast()) {// process the rest;
			Queue<Frame<T>> transferList = new ArrayDeque<Frame<T>>();
			transferList.addAll(frameList);
			frameB = process(transferList, true);
			running = false;
			lastListSize = frameList.size() - 1;
			lastFrames();
			frameList.clear();
			return null;
		}
		
		if (counter % nFrames == 0) {// make a new list for each Callable
			Queue<Frame<T>> transferList = new ArrayDeque<Frame<T>>();
			transferList.addAll(frameList);
			frameB = process(transferList, false);
			if(interpolating){
				if (frameA != null){
					interpolate();
				}
				frameA = frameB;
			} else {
				for (int i = 0; i < nFrames; i++)
					newOutput(new ImgLib2Frame<T>(frameB.getFrameNumber()
							+ i, frameB.getWidth(), frameB.getWidth(), frameB.getPixelDepth(), 
							frameB.getPixels()));				
			}
			frameList.clear();
		}
		return null;
	}
	
	private Frame<T> process(final Queue<Frame<T>> list, final boolean isLast) {
		if (list.isEmpty())
			return null;

		final Frame<T> firstFrame = list.peek();
		final RandomAccessibleInterval<T> firstInterval = firstFrame.getPixels();

		Img<T> out = new ArrayImgFactory<T>().create(firstInterval, Views.iterable(firstInterval).firstElement());
		Cursor<T> cursor = Views.iterable(out).cursor();

		List<Cursor<T>> cursorList = new ArrayList<Cursor<T>>();

		for (Frame<T> currentFrame : list)
			cursorList.add(Views.iterable(currentFrame.getPixels()).cursor());

		while (cursor.hasNext()) {
			List<Integer> values = new ArrayList<Integer>();

			cursor.fwd();
			for (Cursor<T> currentCursor : cursorList) {
				currentCursor.fwd();
				values.add(currentCursor.get().getInteger());
			}
			// find the median

			Integer median = QuickSelect.fastmedian(values, values.size());
			// Integer median = QuickSelect.select(values, middle);
			if (median != null)
				cursor.get().setInteger(median);
		}
		Frame<T> newFrame = new ImgLib2Frame<T>(firstFrame.getFrameNumber(), firstFrame.getWidth(), firstFrame.getHeight(), 
				firstFrame.getPixelDepth(), out);
		if (isLast)
			newFrame.setLast(true);
		return newFrame;
	}
	
	private void interpolate(){
		RandomAccessibleInterval<T> intervalA = frameA.getPixels();
		RandomAccessibleInterval<T> intervalB = frameB.getPixels();

		for (int i = 0; i < nFrames; i++) {
			Img<T> outFrame = new ArrayImgFactory<T>()
					.create(intervalA, Views.iterable(intervalA)
							.firstElement());
			Cursor<T> outCursor = outFrame.cursor();
			Cursor<T> cursorA = Views.iterable(intervalA).cursor();
			Cursor<T> cursorB = Views.iterable(intervalB).cursor();

			while (cursorA.hasNext()) {
				cursorA.fwd();
				cursorB.fwd();
				outCursor.fwd();
				outCursor.get().setInteger(
					cursorA.get().getInteger()
					+ Math.round((cursorB.get().getInteger() - cursorA.get().getInteger()) * ((float) i + 1) / nFrames));
			}

			newOutput(new ImgLib2Frame<T>(
					frameA.getFrameNumber() + i, frameA.getWidth(),
					frameA.getHeight(), frameA.getPixelDepth(), outFrame));
		}
	}
	
	private void lastFrames(){
		// handle the last frames
		for (int i = 0; i < lastListSize; i++) {
			newOutput(new ImgLib2Frame<T>(frameB.getFrameNumber() + i,
					frameB.getWidth(), frameB.getHeight(), 
					frameB.getPixelDepth(), frameB.getPixels()));
		}

		// create last frame
		ImgLib2Frame<T> lastFrame = new ImgLib2Frame<T>(
				frameB.getFrameNumber() + lastListSize, frameB.getWidth(),
				frameB.getHeight(), frameB.getPixelDepth(), frameB.getPixels());
		lastFrame.setLast(true);
		newOutput(lastFrame);
	}
}

