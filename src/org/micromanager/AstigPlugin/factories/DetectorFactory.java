package org.micromanager.AstigPlugin.factories;

import java.util.Map;

import org.micromanager.AstigPlugin.gui.ConfigurationPanel;
import org.micromanager.AstigPlugin.interfaces.PluginInterface;
import org.micromanager.AstigPlugin.pipeline.AbstractModule;


public interface DetectorFactory extends PluginInterface{

	/**
	 * Check that the given settings map is suitable for target detector.
	 *
	 * @param settings 
	 * the map to test.
	 * @return <code>true</code> if the settings map is valid.
	 */
	public boolean setAndCheckSettings( final Map< String, Object > settings );
	
	/**
	 *  @return  Module to process
	 */
	public AbstractModule getDetector();
	
	/**
	 * Returns a new GUI panel able to configure the settings suitable for this
	 * specific detector factory.
	 */
	public ConfigurationPanel getConfigurationPanel();

}
