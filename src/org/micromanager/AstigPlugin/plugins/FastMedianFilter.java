package org.micromanager.AstigPlugin.plugins;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

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

public class FastMedianFilter<T extends IntegerType<T> & NativeType<T>>	extends SingleRunModule {

	private int nFrames, counter = 0;
	private Queue<Frame<T>> frameList = new ArrayDeque<Frame<T>>();
	private long start;
	private List<Callable<Frame<T>>> callables = new ArrayList<Callable<Frame<T>>>();
	private int lastListSize = 0;
	private boolean interpolating;

	public FastMedianFilter(final int numFrames, boolean interpolating) {
		nFrames = numFrames;
		this.interpolating = interpolating;
	}

	@Override
	protected void beforeRun() {
		start = System.currentTimeMillis();
											
	}

	@SuppressWarnings("unchecked")
	@Override
	public Element processData(Element data) {
		final Frame<T> frame = (Frame<T>) data;

		frameList.add(frame);
		counter++;

		if (frame.isLast()) {// process the rest;
			callables.add(new FrameCallable(frameList, true));
			running = false;
			lastListSize = frameList.size() - 1;
			return null;
		}

		if (counter % nFrames == 0) {// make a new list for each callable
			Queue<Frame<T>> transferList = new ArrayDeque<Frame<T>>();
			transferList.addAll(frameList);
			callables.add(new FrameCallable(transferList, false));
			frameList.clear();
		}
		return null;
	}

	private class FrameCallable implements Callable<Frame<T>> {

		private Queue<Frame<T>> list;
		private boolean isLast;

		public FrameCallable(final Queue<Frame<T>> list, final boolean isLast) {
			this.list = list;
			this.isLast = isLast;
		}

		@Override
		public Frame<T> call() throws Exception {
			return process(list, isLast);
		}

	}

	private Frame<T> process(final Queue<Frame<T>> list, final boolean isLast) {
		Frame<T> newFrame = null;
		if (!list.isEmpty()){
			final Frame<T> firstFrame = list.peek();
			final RandomAccessibleInterval<T> firstInterval = firstFrame.getPixels();
	
			Img<T> out = new ArrayImgFactory<T>().create(firstInterval, Views
					.iterable(firstInterval).firstElement());
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
				
				Integer median = QuickSelect.fastmedian(values, values.size());   // find the median
				//Integer median = QuickSelect.select(values, middle); 
				if (median != null)
					cursor.get().setInteger(median);
			}
				newFrame = new ImgLib2Frame<T>(firstFrame.getFrameNumber(), firstFrame.getWidth(), 
				firstFrame.getHeight(), firstFrame.getPixelDepth(), out);
		} else {
			newFrame = new ImgLib2Frame<T>(0, 0, 0, 0, null);
		}
		if (isLast)
			newFrame.setLast(true);
		return newFrame;
	}

	@Override
	protected void afterRun() {

		List<Frame<T>> results = new ArrayList<Frame<T>>();

		try {
			List<Future<Frame<T>>> futures = service.invokeAll(callables);

			for (final Future<Frame<T>> f : futures) {
				Frame<T> val = f.get();
				if (val != null)
					results.add(val);
			}
		} catch (final Exception e) {
			System.err.println(e.getMessage());
		}

		Collections.sort(results);

		if (interpolating) {
			// Prepare frame pairs in order
			final Iterator<Frame<T>> frameIterator = results.iterator();
			Frame<T> frameA = frameIterator.next();
			Frame<T> frameB = null;

			while (frameIterator.hasNext()) {
				frameB = frameIterator.next();

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
										+ Math.round((cursorB.get()
												.getInteger() - cursorA.get()
												.getInteger())
												* ((float) i + 1) / nFrames));
					}

					newOutput(new ImgLib2Frame<T>(
							frameA.getFrameNumber() + i, frameA.getWidth(),
							frameA.getHeight(), frameA.getPixelDepth(), outFrame));
				}
				frameA = frameB;
			}
			if (frameB == null) return;
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
		} else {
			Frame<T> lastElements = results.remove(results.size()-1);
			for (Frame<T> element : results) {
				for (int i = 0; i < nFrames; i++)
					newOutput(new ImgLib2Frame<T>(element.getFrameNumber()
							+ i, element.getWidth(), element.getWidth(), element.getPixelDepth(), 
							element.getPixels()));
			}
			// handle the last frames
			for (int i = 0; i < lastListSize; i++) {
				newOutput(new ImgLib2Frame<T>(lastElements.getFrameNumber() + i,
						lastElements.getWidth(), lastElements.getHeight(), 
						lastElements.getPixelDepth(), lastElements.getPixels()));
			}
			// create last frame
			ImgLib2Frame<T> lastFrame = new ImgLib2Frame<T>(
					lastElements.getFrameNumber() + lastListSize, lastElements.getWidth(),
					lastElements.getHeight(), lastElements.getPixelDepth(), lastElements.getPixels());
			lastFrame.setLast(true);
			newOutput(lastFrame);
		}

		System.out.println("Filtering of " + counter + " images done in "
				+ (System.currentTimeMillis() - start) + "ms.");
	}
	
	@Override
	protected boolean check() {
		return inputs.size()==1 && outputs.size()>=1;
	}

}
