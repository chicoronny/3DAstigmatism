package org.micromanager.AstigPlugin.factories;

import java.util.Map;

import org.micromanager.AstigPlugin.interfaces.PluginInterface;
import org.micromanager.AstigPlugin.pipeline.Renderer;
import org.micromanager.AstigPlugin.gui.ConfigurationPanel;

public interface RendererFactory extends PluginInterface {
	
	/**
	 * Check that the given settings map is suitable for target detector.
	 *
	 * @param settings 
	 * the map to test.
	 * @return <code>true</code> if the settings map is valid.
	 */
	public boolean setAndCheckSettings( final Map< String, Object > settings );
	
	/**
	 *  @return  Renderer to process
	 */
	public Renderer getRenderer();
	
	/**
	 * Returns a new GUI panel able to configure the settings suitable for this
	 * specific factory.
	 */
	public ConfigurationPanel getConfigurationPanel();
	
	public Map<String, Object> getInitialSettings();
	
	public static final String KEY_xmin = "xmin";
	public static final String KEY_xmax = "xmax";
	public static final String KEY_ymin = "ymin";
	public static final String KEY_ymax = "ymax";
	public static final String KEY_pSizeX = "pSizeX";
	public static final String KEY_pSizeY = "pSizeY";
	public static final String KEY_xBins = "xbins";
	public static final String KEY_yBins = "ybins";
}
