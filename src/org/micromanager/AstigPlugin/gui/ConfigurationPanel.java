package org.micromanager.AstigPlugin.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public abstract class ConfigurationPanel extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3160662804934210143L;
	private final List< ChangeListener > changeListeners = new ArrayList< ChangeListener >();
	
	/**
	 * Echo the parameters of the given settings on this panel.  
	 */
	public abstract void setSettings(final Map<String, Object> settings);
	
	/**
	 * @return  a new settings map object with its values set
	 * by this panel.
	 */
	public abstract Map<String, Object> getSettings();

	/**
	 * Called when one of the Panel is changed by the user.
	 */
	void fireChanged(ChangeEvent e) {
		for ( final ChangeListener cl : changeListeners )
		{
			cl.stateChanged( e );
		}
	}
	
	/**
	 * Add an {@link ChangeListener} to this panel. The {@link ChangeListener}
	 * will be notified when a change happens to the thresholds displayed by
	 * this panel, whether due to the slider being move, the auto-threshold
	 * button being pressed, or the combo-box selection being changed.
	 */
	public void addChangeListener( final ChangeListener listener )
	{
		changeListeners.add( listener );
	}

	/**
	 * Remove a ChangeListener from this panel.
	 *
	 * @return true if the listener was in listener collection of this instance.
	 */
	public boolean removeChangeListener( final ChangeListener listener )
	{
		return changeListeners.remove( listener );
	}

	public Collection< ChangeListener > getChangeListeners()
	{
		return changeListeners;
	}
}
