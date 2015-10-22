package org.micromanager.AstigPlugin.plugins;

import java.util.Map;

import org.micromanager.AstigPlugin.gui.ConfigurationPanel;
import org.micromanager.AstigPlugin.gui.FastMedianPanel;
import org.micromanager.AstigPlugin.pipeline.AbstractModule;
import org.micromanager.AstigPlugin.pipeline.ImageMath.operators;

public class FastMedianFilterFactory {
	
	public static final String NAME = "Fast Median Filter";

	public static final String KEY = "FASTMEDIAN";

	public static final String INFO_TEXT = "<html>"
			+ "Fast Median Filter with the option to interpolate between blocks"
			+ "</html>";
	
	private Map<String, Object> settings;
	private FastMedianPanel configPanel = new FastMedianPanel();

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
		this.settings = settings;
		return true;
	}

	@SuppressWarnings("rawtypes")
	public AbstractModule getModule() {
		boolean interpolating  = (Boolean) settings.get(FastMedianPanel.KEY_INTERPOLATING);
		int frames  = (Integer) settings.get(FastMedianPanel.KEY_FRAMES);
		return new FastMedianFilter(frames, interpolating);
	}

	public ConfigurationPanel getConfigurationPanel() {
		configPanel.setName(KEY);
		return configPanel;
	}

	public operators getOperator() {
		return operators.SUBSTRACTION;
	}

	public int processingFrames() {
		int procFrames = (Integer) ((Integer) settings.get(FastMedianPanel.KEY_FRAMES) == 0 ? 1 : (Integer) settings.get(FastMedianPanel.KEY_FRAMES) == 0); 
		return procFrames;
	}

}
