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
	
	private HistogramRendererPanel configPanel;
	private Map<String, Object> settings;
	
	public HistogramRendererFactory(){
		configPanel = new HistogramRendererPanel();
		settings = configPanel.getInitialSettings();
	}

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
		final int xBins = (Integer) settings.get(HistogramRendererPanel.KEY_xBins);
		final int yBins = (Integer) settings.get(HistogramRendererPanel.KEY_yBins);
		final double xmin = (Double) settings.get(HistogramRendererPanel.KEY_xmin);
		final double xmax = (Double) settings.get(HistogramRendererPanel.KEY_xmax);
		final double ymin = (Double) settings.get(HistogramRendererPanel.KEY_ymin);
		final double ymax = (Double) settings.get(HistogramRendererPanel.KEY_ymax);
		final double zmin = (Double) settings.get(HistogramRendererPanel.KEY_zmin);
		final double zmax = (Double) settings.get(HistogramRendererPanel.KEY_zmax);
		return new HistogramRenderer(xBins, yBins, xmin, xmax, ymin, ymax, zmin, zmax);
	}

	public ConfigurationPanel getConfigurationPanel() {
		configPanel.setName(KEY);
		return configPanel;
	}

	public boolean setAndCheckSettings(Map<String, Object> settings) {
		configPanel.setSettings(settings);
		this.settings.putAll(settings);
		return settings != null;
	}

	public Map<String, Object> getInitialSettings() {
		return configPanel.getInitialSettings();
	}
	
	public Map<String, Object> getSettings() {
		return settings;
	}
	
}
