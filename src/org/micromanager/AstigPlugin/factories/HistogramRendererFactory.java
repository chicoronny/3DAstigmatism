package org.micromanager.AstigPlugin.factories;

import java.util.Map;

import org.micromanager.AstigPlugin.gui.ConfigurationPanel;
import org.micromanager.AstigPlugin.gui.HistogramRendererPanel;
import org.micromanager.AstigPlugin.pipeline.Renderer;
import org.micromanager.AstigPlugin.plugins.HistogramRenderer;

public class HistogramRendererFactory {
	public static final String NAME = "Histogram Renderer";
	public static final String KEY = "HISTOGRAMRENDERER";
	public static final String INFO_TEXT = "<html>"
											+ "Histogram Renderer Plugin"
											+ "</html>";
	public static final String KEY_xmin = "xmin";
	public static final String KEY_xmax = "xmax";
	public static final String KEY_ymin = "ymin";
	public static final String KEY_ymax = "ymax";
	public static final String KEY_xBins = "xbins";
	public static final String KEY_yBins = "ybins";
	public static final String KEY_zmin = "zmin";
	public static final String KEY_zmax = "zmax";
	
	private HistogramRendererPanel configPanel = new HistogramRendererPanel();
	private Map<String, Object> settings;

	public String getInfoText() {
		return INFO_TEXT;
	}

	public String getKey() {
		return KEY;
	}

	public String getName() {
		return NAME;
	}

	public Renderer getRenderer() {
		final int xBins = (Integer) settings.get(KEY_xBins);
		final int yBins = (Integer) settings.get(KEY_yBins);
		final double xmin = (Double) settings.get(KEY_xmin);
		final double xmax = (Double) settings.get(KEY_xmax);
		final double ymin = (Double) settings.get(KEY_ymin);
		final double ymax = (Double) settings.get(KEY_ymax);
		final double zmin = (Double) settings.get(KEY_zmin);
		final double zmax = (Double) settings.get(KEY_zmax);
		return new HistogramRenderer(xBins, yBins, xmin, xmax, ymin, ymax, zmin, zmax);
	}

	public ConfigurationPanel getConfigurationPanel() {
		configPanel.setName(KEY);
		return configPanel;
	}

	public boolean setAndCheckSettings(Map<String, Object> settings) {
		this.settings = settings;
		configPanel.setSettings(settings);
		return settings != null;
	}

	public Map<String, Object> getInitialSettings() {
		return configPanel.getInitialSettings();
	}
	
}
