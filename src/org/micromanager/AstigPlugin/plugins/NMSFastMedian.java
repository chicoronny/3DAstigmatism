package org.micromanager.AstigPlugin.plugins;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.micromanager.AstigPlugin.interfaces.Element;
import org.micromanager.AstigPlugin.interfaces.Frame;
import org.micromanager.AstigPlugin.math.QuickSelect;
import org.micromanager.AstigPlugin.pipeline.FrameElements;
import org.micromanager.AstigPlugin.pipeline.ImgLib2Frame;
import org.micromanager.AstigPlugin.pipeline.Localization;
import org.micromanager.AstigPlugin.pipeline.SingleRunModule;
import org.micromanager.AstigPlugin.tools.LemmingUtils;

import ij.ImagePlus;
import ij.ImageStack;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class NMSFastMedian<T extends RealType<T> & NativeType<T>> extends SingleRunModule {

	public static final String NAME = "NMS Fast Median Filter";
	public static final String KEY = "NMSFASTMEDIAN";
	public static final String INFO_TEXT = "<html>" + "NMS detector with Fast Median Filter with the option to interpolate between blocks" + "</html>";
	private int nFrames;
	private boolean interpolating;
	private Queue<Frame<T>> frameList = new ArrayDeque<Frame<T>>();
	private int counter = 0;
	private int peaksFound = 0;
	private int lastListSize = 0;
	private Frame<T> frameA = null;
	private Frame<T> frameB = null;
	private double threshold;
	private int n_;

	public NMSFastMedian(final int numFrames, final boolean interpolating, final double threshold, final int size) {
		this.nFrames = numFrames;
		this.interpolating = interpolating;
		this.threshold = threshold;
		this.n_ = size;
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
			if (interpolating) 
				if (frameA != null) 
					interpolate(transferList);
			running = false;
			lastListSize = frameList.size() - 1;
			lastFrames(transferList);
			frameList.clear();
			return null;
		}

		if (counter % nFrames == 0) {// make a new list for each Callable
			final Queue<Frame<T>> transferList = new ArrayDeque<Frame<T>>();
			transferList.addAll(frameList);
			frameB = process(transferList, false);
			if (interpolating) {
				if (frameA != null) 
					interpolate(transferList);
				frameA = frameB;
			} else {
				for (int i = 0; i < nFrames; i++) {
					final Frame<T> filtered = substract(frameB, transferList.poll());
					newOutput(detect(filtered));
				}
			}
			frameList.clear();
		}
		return null;
	}

	private Frame<T> substract(Frame<T> framePairA, Frame<T> framePairB) {
		RandomAccessibleInterval<T> intervalA = framePairA.getPixels();
		RandomAccessibleInterval<T> intervalB = framePairB.getPixels();

		Cursor<T> cursorA = Views.flatIterable(intervalA).cursor();
		Cursor<T> cursorB = Views.flatIterable(intervalB).cursor();

		while (cursorA.hasNext()) {
			cursorA.fwd();
			cursorB.fwd(); // move both cursors forward by one pixel
			double val = cursorB.get().getRealDouble() - cursorA.get().getRealDouble();
			val = val < 0 ? 0 : val; // check for negative values
			cursorA.get().setReal(val);
		}
		return new ImgLib2Frame<T>(framePairA.getFrameNumber(), framePairA.getWidth(), framePairA.getHeight(), framePairA.getPixelDepth(), intervalA);
	}

	private Frame<T> process(final Queue<Frame<T>> list, final boolean isLast) {
		Frame<T> newFrame = null;
		if (!list.isEmpty()) {
			final Frame<T> firstFrame= list.peek();
			final RandomAccessibleInterval<T> firstInterval = firstFrame.getPixels();
			final long[] dims = new long[firstInterval.numDimensions()];
			firstInterval.dimensions(dims);
			final T type = Views.iterable(firstInterval).firstElement();
			Img<T> out = new ArrayImgFactory<T>().create(dims, type);

			final FinalInterval shrinked = Intervals.expand(out, -1); // handle borders
			final IntervalView<T> source = Views.interval(out, shrinked);
			final RectangleShape outshape = new RectangleShape(1, false); // 3x3 kernel
			Cursor<T> outcursor = Views.iterable(source).cursor();

			final List<RandomAccess<T>> cursorList = new ArrayList<RandomAccess<T>>();

			for (Frame<T> currentFrame : list) {
				RandomAccessibleInterval<T> currentInterval = currentFrame.getPixels();
				cursorList.add(currentInterval.randomAccess()); // creating neighborhoods
			}

			for (final Neighborhood<T> localNeighborhood : outshape.neighborhoods(source)) {
				outcursor.fwd();
				final Cursor<T> localCursor = localNeighborhood.cursor();
				final List<Double> values = new ArrayList<Double>();
				while (localCursor.hasNext()) {
					localCursor.fwd();
					for (RandomAccess<T> currentCursor : cursorList) {
						currentCursor.setPosition(localCursor);
						values.add(currentCursor.get().getRealDouble());
					}
				}
				final Double median = QuickSelect.fastmedian(values, values.size()); // find the median
				if (median != null) outcursor.get().setReal(median);
			}

			// Borders
			final Cursor<T> top = Views.interval(out, Intervals.createMinMax(0, 0, dims[0] - 1, 0)).cursor();
			while (top.hasNext()) {
				final List<Double> values = new ArrayList<Double>();
				top.fwd();
				for (RandomAccess<T> currentCursor : cursorList) {
					currentCursor.setPosition(top);
					values.add(currentCursor.get().getRealDouble());
				}
				final Double median = QuickSelect.fastmedian(values, values.size()); // find the median
				if (median != null) top.get().setReal(median);
			}
			final Cursor<T> left = Views.interval(out, Intervals.createMinMax(0, 1, 0, dims[1] - 2)).cursor();
			while (left.hasNext()) {
				final List<Double> values = new ArrayList<Double>();
				left.fwd();
				for (RandomAccess<T> currentCursor : cursorList) {
					currentCursor.setPosition(left);
					values.add(currentCursor.get().getRealDouble());
				}
				final Double median = QuickSelect.fastmedian(values, values.size()); // find the median
				if (median != null) left.get().setReal(median);
			}
			final Cursor<T> right = Views.interval(out, Intervals.createMinMax(dims[0] - 1, 1, dims[0] - 1, dims[1] - 2)).cursor();
			while (right.hasNext()) {
				final List<Double> values = new ArrayList<Double>();
				right.fwd();
				for (RandomAccess<T> currentCursor : cursorList) {
					currentCursor.setPosition(right);
					values.add(currentCursor.get().getRealDouble());
				}
				final Double median = QuickSelect.fastmedian(values, values.size()); // find the median
				if (median != null) right.get().setReal(median);
			}
			final Cursor<T> bottom = Views.interval(out, Intervals.createMinMax(0, dims[1] - 1, dims[0] - 1, dims[1] - 1)).cursor();
			while (bottom.hasNext()) {
				final List<Double> values = new ArrayList<Double>();
				bottom.fwd();
				for (RandomAccess<T> currentCursor : cursorList) {
					currentCursor.setPosition(bottom);
					values.add(currentCursor.get().getRealDouble());
				}
				final Double median = QuickSelect.fastmedian(values, values.size()); // find the median
				if (median != null) bottom.get().setReal(median);
			}

			newFrame = new ImgLib2Frame<T>(
				firstFrame.getFrameNumber(), firstFrame.getWidth(), firstFrame.getHeight(), firstFrame.getPixelDepth(), out);
		} else {
			newFrame = new ImgLib2Frame<T>(0, 1, 1, 1, null);
		}
		if (isLast) newFrame.setLast(true);
		return newFrame;
	}

	private void interpolate(Queue<Frame<T>> transferList) {
		RandomAccessibleInterval<T> intervalA = frameA.getPixels();
		RandomAccessibleInterval<T> intervalB = frameB.getPixels();
		double newValue; 

		for (int i = 0; i < nFrames; i++) {
			Img<T> outFrame = new ArrayImgFactory<T>().create(intervalA, Views.iterable(intervalA).firstElement());
			Cursor<T> outCursor = outFrame.cursor();
			Cursor<T> cursorA = Views.iterable(intervalA).cursor();
			Cursor<T> cursorB = Views.iterable(intervalB).cursor();

			while (cursorA.hasNext()) {
				cursorA.fwd();
				cursorB.fwd();
				outCursor.fwd();
				newValue = cursorA.get().getRealDouble()
					+ Math.round((cursorB.get().getRealDouble() - cursorA.get().getRealDouble()) * ((double) i + 1) / nFrames);
				outCursor.get().setReal(newValue);
			}

			final Frame<T> filtered = substract(
				new ImgLib2Frame<T>(frameA.getFrameNumber() + i, frameA.getWidth(), frameA.getHeight(), frameA.getPixelDepth(), outFrame),transferList.poll());

			newOutput(detect(filtered));
		}
	}

	private void lastFrames(Queue<Frame<T>> transferList) {
		// handle the last frames
		for (int i = 0; i < lastListSize; i++) {
			final Frame<T> filtered = substract( frameB, transferList.poll());
			newOutput(detect(filtered));
		}

		// create last frame
		Frame<T> lastFrame = substract(frameB,transferList.poll() );
		lastFrame.setLast(true);
		newOutput(detect(lastFrame));
	}

	public FrameElements<T> detect(Frame<T> frame) {
		final RandomAccessibleInterval<T> interval = frame.getPixels();
		final RandomAccess<T> ra = interval.randomAccess();

		// compute max of the Image
		final T max = LemmingUtils.computeMax(Views.iterable(interval));
		double threshold_ = max.getRealDouble() / 100.0 * threshold;

		int i, j, ii, jj, ll, kk;
		int mi, mj;
		boolean failed = false;
		long width_ = interval.dimension(0);
		long height_ = interval.dimension(1);
		List<Element> found = new ArrayList<Element>();
		T first,second = max,third;

		for (i = 0; i <= width_ - 1 - n_; i += n_ + 1) { // Loop over (n+1)x(n+1)
			for (j = 0; j <= height_ - 1 - n_; j += n_ + 1) {
				mi = i;
				mj = j;
				for (ii = i; ii <= i + n_; ii++) {
					for (jj = j; jj <= j + n_; jj++) {
						ra.setPosition(new int[] { ii, jj });
						first = ra.get().copy();
						ra.setPosition(new int[] { mi, mj });
						second = ra.get().copy();
						if (first.compareTo(second) > 0) {
							mi = ii;
							mj = jj;
						}
					}
				}
				failed = false;

				Outer: for (ll = mi - n_; ll <= mi + n_; ll++) {
					for (kk = mj - n_; kk <= mj + n_; kk++) {
						if ((ll < i || ll > i + n_) || (kk < j || kk > j + n_)) {
							if (ll < width_ && ll > 0 && kk < height_ && kk > 0) {
								ra.setPosition(new int[] { ll, kk });
								third = ra.get().copy();
								//ra.setPosition(new int[] { mi, mj });
								//second = ra.get().copy();
								if (third.compareTo(second) > 0) {
									failed = true;
									break Outer;
								}
							}
						}
					}
				}
				if (!failed) {
					ra.setPosition(new int[] { mi, mj });
					T value = ra.get();
					if (value.getRealDouble() > threshold_) {
						found.add(new Localization(mi * frame.getPixelDepth(), mj * frame.getPixelDepth(), value.getRealDouble(), frame
							.getFrameNumber()));
						peaksFound++;
					}
				}
			}
		}

		return new FrameElements<T>(found, frame);
	}
	
	public FrameElements<T> preview(ImagePlus img, int numberOfFrames, List<Double> cameraSettings){
		final int frameNumber = (int)img.getSlice();
		final ImageStack stack = img.getStack();
		final int stackSize = stack.getSize();
		final Double offset = cameraSettings.get(0);
		final Double em_gain = cameraSettings.get(1);
		final Double conversion = cameraSettings.get(2);
		final Queue<Frame<T>> list = new ArrayDeque<Frame<T>>();
		int start = frameNumber/numberOfFrames*numberOfFrames; // integer devision magic
		final double pixelSize = img.getCalibration().pixelDepth;
		double adu, im2phot;
		Frame<T> origFrame=null;
		if (start<1) start=1;
		
		for (int i = start; i < start + numberOfFrames; i++) {
			if (i < stackSize) {
				Object ip = stack.getPixels(i);
				Img<T> curImage = LemmingUtils.wrap(ip, new long[]{stack.getWidth(), stack.getHeight()});
				final Cursor<T> it = curImage.cursor();
				while(it.hasNext()){
					it.fwd();
					adu = Math.max((it.get().getRealDouble()-offset), 0);
					im2phot = adu*conversion/em_gain;
					it.get().setReal(im2phot);
				}
				Frame<T> curFrame = new ImgLib2Frame<T>(i, (int) curImage.dimension(0), (int) curImage.dimension(1), pixelSize, curImage);
				if (i==frameNumber) origFrame=curFrame;
				list.add(curFrame);
			}
		}
		img.updateAndDraw();
		if (origFrame==null) origFrame=list.peek();
		Frame<T> result = process(list,true);
		
		return detect(substract(result,origFrame));
	}	
	
	@Override
	protected void afterRun() {
		System.out.println("Detector found "
				+ peaksFound + " peaks in "
				+ (System.currentTimeMillis() - start) + "ms.");
		
		counter=0;
	}
}
