package org.micromanager.AstigPlugin.factories;

import java.util.HashMap;
import java.util.Map;

import org.micromanager.AstigPlugin.gui.ConfigurationPanel;
import org.micromanager.AstigPlugin.gui.NMSDetectorPanel;
import org.micromanager.AstigPlugin.pipeline.AbstractModule;
import org.micromanager.AstigPlugin.plugins.NMSDetector;

public class NMSDetectorFactory {
	public static final String NAME = "NMS Detector";

	public static final String KEY = "NMSDETECTOR";

	public static final String INFO_TEXT = "<html>"
			+ "NMS Detector Plugin"
			+ "</html>";
	
	private Map<String, Object> settings = new HashMap<String, Object>();
	private NMSDetectorPanel configPanel = new NMSDetectorPanel();

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

	@SuppressWarnings("rawtypes")
	public AbstractModule getDetector() {
		final double threshold = ( Double ) settings.get( NMSDetectorPanel.KEY_NMS_THRESHOLD );
		final int stepSize = ( Integer ) settings.get( NMSDetectorPanel.KEY_NMS_STEPSIZE );
		return new NMSDetector(threshold, stepSize);
	}

	public ConfigurationPanel getConfigurationPanel() {
		configPanel.setName(KEY);
		return configPanel;
	}

}
