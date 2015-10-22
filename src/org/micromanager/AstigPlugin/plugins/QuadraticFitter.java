package org.micromanager.AstigPlugin.plugins;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.micromanager.AstigPlugin.factories.FitterFactory;
import org.micromanager.AstigPlugin.gui.ConfigurationPanel;
import org.micromanager.AstigPlugin.gui.QuadraticFitterPanel;
import org.micromanager.AstigPlugin.interfaces.Element;
import org.micromanager.AstigPlugin.interfaces.Frame;
import org.micromanager.AstigPlugin.math.SubpixelLocalization;
import org.micromanager.AstigPlugin.pipeline.Fitter;
import org.scijava.plugin.Plugin;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class QuadraticFitter<T extends RealType<T>, F extends Frame<T>> extends Fitter<T, F> {
	
	public static final String NAME = "Quadratic Fitter";

	public static final String KEY = "QUADRATICFITTER";

	public static final String INFO_TEXT = "<html>"
			+ "Quadratic Fitter Plugin (without z-direction)"
			+ "</html>";


	public QuadraticFitter(int windowSize) {
		super(windowSize);
	}

	@Override
	public List<Element> fit(List<Element> sliceLocs, RandomAccessibleInterval<T> pixels, long windowSize, long frameNumber) {
		final RandomAccessible<T> ra = Views.extendBorder(pixels);
		final boolean[] allowedToMoveInDim = new boolean[ ra.numDimensions() ];
		Arrays.fill( allowedToMoveInDim, true );
		
		final List<Element> refined = SubpixelLocalization.refinePeaks(sliceLocs, ra, pixels, true, size, true, 0.01f, allowedToMoveInDim);

		return refined;
	}
	
	@Plugin( type = FitterFactory.class, visible = true )
	public static class Factory implements FitterFactory{

		
		private Map<String, Object> settings;
		private QuadraticFitterPanel configPanel = new QuadraticFitterPanel();

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
			return settings!=null;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Fitter getFitter() {
			final int windowSize = (Integer) settings.get( QuadraticFitterPanel.KEY_QUAD_WINDOW_SIZE );
			return new QuadraticFitter(windowSize);
		}

		@Override
		public ConfigurationPanel getConfigurationPanel() {
			configPanel.setName(KEY);
			return configPanel;
		}
		
	}

}
