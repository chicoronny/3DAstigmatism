package org.micromanager.AstigPlugin.gui;

import ij.IJ;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;

import org.micromanager.AstigPlugin.tools.WaitForChangeListener;
import org.micromanager.AstigPlugin.tools.WaitForKeyListener;


public class NMSDetectorPanel extends ConfigurationPanel {
	private JTextField textFieldThreshold;
	private JSpinner spinnerWindowSize;
	private final ChangeEvent CHANGE_EVENT = new ChangeEvent( this );

	public NMSDetectorPanel() {
		setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 205)), "Peak Detection & Fitter", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 205)));
		
		JLabel labelThreshold = new JLabel("Threshold [%]");
		
		textFieldThreshold = new JTextField();
		textFieldThreshold.addKeyListener(new WaitForKeyListener(1000, new Runnable(){
			@Override
			public void run() {
				fireChanged( CHANGE_EVENT );
			}
		}));
		textFieldThreshold.setHorizontalAlignment(SwingConstants.RIGHT);
		textFieldThreshold.setText("1");
		
		JLabel labelWindowSize = new JLabel("WindowSize");
		
		spinnerWindowSize = new JSpinner();
		spinnerWindowSize.addChangeListener(new WaitForChangeListener(500, new Runnable() {
			@Override
			public void run() {
				fireChanged( CHANGE_EVENT );
			}
		}));
		spinnerWindowSize.setModel(new SpinnerNumberModel(new Integer(10), new Integer(1), null, new Integer(1)));
		GroupLayout gl_panelPeakDet = new GroupLayout(this);
		gl_panelPeakDet.setHorizontalGroup(
			gl_panelPeakDet.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelPeakDet.createSequentialGroup()
					.addContainerGap()
					.addComponent(labelThreshold, GroupLayout.PREFERRED_SIZE, 84, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(textFieldThreshold, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(labelWindowSize, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(spinnerWindowSize, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(156, Short.MAX_VALUE))
		);
		gl_panelPeakDet.setVerticalGroup(
			gl_panelPeakDet.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelPeakDet.createSequentialGroup()
					.addGroup(gl_panelPeakDet.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panelPeakDet.createParallelGroup(Alignment.BASELINE)
							.addComponent(labelThreshold, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
							.addComponent(textFieldThreshold, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_panelPeakDet.createParallelGroup(Alignment.BASELINE)
							.addComponent(labelWindowSize)
							.addComponent(spinnerWindowSize, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(241, Short.MAX_VALUE))
		);
		setLayout(gl_panelPeakDet);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -4601480448696314069L;

	@Override
	public void setSettings(Map<String, Object> settings) {
		spinnerWindowSize.setValue(settings.get(PanelKeys.KEY_WINDOWSIZE));
		textFieldThreshold.setText("" + settings.get(PanelKeys.KEY_THRESHOLD));
	}

	@Override
	public Map<String, Object> getSettings() {
		final Map<String, Object> settings = new HashMap<String, Object>(2);
		try {
			final int stepsize = (Integer) spinnerWindowSize.getValue();
			final double threshold = Double.parseDouble(textFieldThreshold.getText());
			settings.put(PanelKeys.KEY_WINDOWSIZE, stepsize);
			settings.put(PanelKeys.KEY_THRESHOLD, threshold);
		} catch (Exception ex) {
			IJ.showMessage(getClass().getSimpleName(), "Parse error!\n" + ex.getMessage());
		}
		return settings;
	}
}
