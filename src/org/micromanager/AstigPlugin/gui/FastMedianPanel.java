package org.micromanager.AstigPlugin.gui;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

public class FastMedianPanel extends ConfigurationPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3273186486718647271L;
	private JSpinner spinnerFrames;

	public FastMedianPanel() {
		setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 205)), "Background Substraction", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 205)));
		
		JLabel labelFrames = new JLabel("Frames");
		labelFrames.setHorizontalAlignment(SwingConstants.LEFT);
		
		spinnerFrames = new JSpinner();
		spinnerFrames.setToolTipText("frames used for background substraction");
		spinnerFrames.setModel(new SpinnerNumberModel(new Integer(50), null, null, new Integer(1)));
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(labelFrames)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(spinnerFrames, GroupLayout.PREFERRED_SIZE, 67, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(316, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(spinnerFrames, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(labelFrames))
					.addContainerGap(243, Short.MAX_VALUE))
		);
		setLayout(groupLayout);
	}

	@Override
	public void setSettings(Map<String, Object> settings) {
		spinnerFrames.setValue(settings.get(PanelKeys.KEY_FRAMES));
	}

	@Override
	public Map<String, Object> getSettings() {
		final Map< String, Object > settings = new HashMap<String, Object>( 2 );
		final int frames = (Integer) spinnerFrames.getValue();
		settings.put(PanelKeys.KEY_FRAMES, frames);
		return settings;
	}
}
