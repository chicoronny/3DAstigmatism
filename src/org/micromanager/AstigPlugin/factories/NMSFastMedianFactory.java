package org.micromanager.AstigPlugin.factories;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.micromanager.AstigPlugin.gui.ConfigurationPanel;
import org.micromanager.AstigPlugin.gui.FastMedianPanel;
import org.micromanager.AstigPlugin.gui.PanelKeys;
import org.micromanager.AstigPlugin.plugins.NMSFastMedian;

public class NMSFastMedianFactory<T extends RealType<T> & NativeType<T>> {
	
	public static final String NAME = "NMS with Fast Median Filter";

	public static final String KEY = "FASTMEDIAN";

	public static final String INFO_TEXT = "<html>"
			+ "NMS Detector with fast median filter with interpolation between blocks"
			+ "</html>";
	
	private Map<String, Object> settings = new HashMap<String, Object>();
	private ConfigurationPanel configPanel = new FastMedianPanel();

	public String getInfoText() {
		return INFO_TEXT;
	}

	public String getKey() {
		return KEY;
	}

	public String getName() {
		return NAME;
	}

	public boolean setAndCheckSettings(Map<String, Object> settings) {
		this.settings.putAll(settings);
		return settings!=null;
	}

	public NMSFastMedian<T> getModule() {
		int frames  = (Integer) settings.get(PanelKeys.KEY_FRAMES);
		final double threshold = ( Double ) settings.get( PanelKeys.KEY_THRESHOLD );
		final int stepSize = ( Integer ) settings.get( PanelKeys.KEY_WINDOWSIZE );
		return new NMSFastMedian<T>(frames, true, threshold, stepSize);
	}

	public ConfigurationPanel getConfigurationPanel() {
		configPanel.setName(KEY);
		return configPanel;
	}

	public int processingFrames() {
		int procFrames = ((Integer) settings.get(PanelKeys.KEY_FRAMES) == 0 ? 1 : (Integer) settings.get(PanelKeys.KEY_FRAMES)); 
		return procFrames;
	}

}
