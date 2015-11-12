package org.micromanager.AstigPlugin.gui;

import java.util.HashMap;
import java.util.Map;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;

public class FastMedianPanel extends ConfigurationPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3273186486718647271L;
	public static final String KEY_FRAMES = "FRAMES";
	private JSpinner spinnerFrames;

	public FastMedianPanel() {
		setBorder(null);
		
		JLabel lblFrames = new JLabel("Frames");
		
		spinnerFrames = new JSpinner();
		spinnerFrames.setModel(new SpinnerNumberModel(new Integer(50), null, null, new Integer(1)));
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblFrames)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(spinnerFrames, GroupLayout.PREFERRED_SIZE, 67, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(326, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblFrames)
						.addComponent(spinnerFrames, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(266, Short.MAX_VALUE))
		);
		setLayout(groupLayout);
	}

	@Override
	public void setSettings(Map<String, Object> settings) {
		spinnerFrames.setValue(settings.get(KEY_FRAMES));
	}

	@Override
	public Map<String, Object> getSettings() {
		final Map< String, Object > settings = new HashMap<String, Object>( 2 );
		final int frames = (Integer) spinnerFrames.getValue();
		settings.put(KEY_FRAMES, frames);
		return settings;
	}
}
