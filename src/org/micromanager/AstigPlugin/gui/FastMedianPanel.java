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
import javax.swing.event.ChangeEvent;

import org.micromanager.AstigPlugin.tools.WaitForChangeListener;
import org.micromanager.AstigPlugin.tools.WaitForKeyListener;

import ij.IJ;

import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.JTextField;

public class FastMedianPanel extends ConfigurationPanel {

	private static final long serialVersionUID = 1L;
	private JSpinner spinnerFrames;
	private JTextField textFieldThreshold;
	private JSpinner spinnerWindowSize;
	private final ChangeEvent CHANGE_EVENT = new ChangeEvent( this );

	public FastMedianPanel() {
		setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 205)), "Detector with Background Substraction", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 205)));
		
		JLabel labelFrames = new JLabel("Frames");
		labelFrames.setHorizontalAlignment(SwingConstants.LEFT);
		
		spinnerFrames = new JSpinner();
		spinnerFrames.setToolTipText("frames used for background substraction");
		spinnerFrames.setModel(new SpinnerNumberModel(new Integer(50), null, null, new Integer(1)));
		spinnerFrames.addChangeListener(new WaitForChangeListener(500, new Runnable() {
			@Override
			public void run() {
				fireChanged( CHANGE_EVENT );
			}
		}));
		
		JLabel labelThreshold = new JLabel("Threshold [%]");
		
		textFieldThreshold = new JTextField();
		textFieldThreshold.setText("1");
		textFieldThreshold.setHorizontalAlignment(SwingConstants.RIGHT);
		textFieldThreshold.addKeyListener(new WaitForKeyListener(500, new Runnable(){
			@Override
			public void run() {
				fireChanged( CHANGE_EVENT );
			}
		}));
		
		JLabel labelWindowSize = new JLabel("WindowSize");
		
		spinnerWindowSize = new JSpinner();
		spinnerWindowSize.setModel(new SpinnerNumberModel(new Integer(15), new Integer(1), null, new Integer(1)));
		spinnerWindowSize.addChangeListener(new WaitForChangeListener(500, new Runnable() {
			@Override
			public void run() {
				fireChanged( CHANGE_EVENT );
			}
		}));
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(labelFrames)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(spinnerFrames, GroupLayout.PREFERRED_SIZE, 67, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(labelThreshold, GroupLayout.PREFERRED_SIZE, 84, GroupLayout.PREFERRED_SIZE)
							.addGap(12)
							.addComponent(textFieldThreshold, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE)
							.addGap(12)
							.addComponent(labelWindowSize, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE)
							.addGap(12)
							.addComponent(spinnerWindowSize, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)))
					.addGap(132))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(spinnerFrames, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(labelFrames))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(14)
							.addComponent(labelThreshold, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(19)
							.addComponent(textFieldThreshold, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(19)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(labelWindowSize)
								.addComponent(spinnerWindowSize))))
					.addContainerGap(224, Short.MAX_VALUE))
		);
		setLayout(groupLayout);
	}

	@Override
	public void setSettings(Map<String, Object> settings) {
		spinnerFrames.setValue(settings.get(PanelKeys.KEY_FRAMES));
		textFieldThreshold.setText(String.valueOf(settings.get(PanelKeys.KEY_THRESHOLD)));
		spinnerWindowSize.setValue(settings.get(PanelKeys.KEY_WINDOWSIZE));
	}

	@Override
	public Map<String, Object> getSettings() {
		final Map<String, Object> settings = new HashMap<String, Object>(3);
		try {
			final int frames = (Integer) spinnerFrames.getValue();
			final int stepsize = (Integer) spinnerWindowSize.getValue();
			final double threshold = Double.parseDouble(textFieldThreshold.getText());
			settings.put(PanelKeys.KEY_WINDOWSIZE, stepsize);
			settings.put(PanelKeys.KEY_THRESHOLD, threshold);
			settings.put(PanelKeys.KEY_FRAMES, frames);
		} catch (Exception ex) {
			IJ.showMessage(getClass().getSimpleName(), "Parse error!\n" + ex.getMessage());
		}
		return settings;
	}
}
