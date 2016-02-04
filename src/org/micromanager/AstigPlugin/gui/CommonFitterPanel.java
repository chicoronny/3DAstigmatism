package org.micromanager.AstigPlugin.gui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.io.File;
import java.awt.event.ActionEvent;

import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;

import org.micromanager.AstigPlugin.tools.WaitForKeyListener;

import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class CommonFitterPanel extends ConfigurationPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3081886846323191618L;
	private JButton btnCalibration;
	private JLabel lblCalibration;
	private File calibFile;
	private ChangeEvent CHANGE_EVENT = new ChangeEvent(this);
	private JLabel lblGain;
	private JLabel lblOffset;
	private JTextField textFieldGain;
	private JTextField textFieldOffset;

	public CommonFitterPanel() {
		setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 205)), "Astigmatism Fitter", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 205)));
		
		lblCalibration = new JLabel("File");
		lblCalibration.setAlignmentX(0.5f);
		
		btnCalibration = new JButton("Calib. File");
		btnCalibration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadCalibrationFile();
				fireChanged( CHANGE_EVENT );
			}
		});
		
		lblGain = new JLabel("Gain");
		
		lblOffset = new JLabel("Offset");
		
		textFieldGain = new JTextField();
		textFieldGain.addKeyListener(new WaitForKeyListener(500, new Runnable(){
			@Override
			public void run() {
				fireChanged( CHANGE_EVENT );
			}
		}));
		textFieldGain.setHorizontalAlignment(SwingConstants.TRAILING);
		textFieldGain.setText("100");
		textFieldGain.setColumns(10);
		
		textFieldOffset = new JTextField();
		textFieldOffset.addKeyListener(new WaitForKeyListener(500, new Runnable(){
			@Override
			public void run() {
				fireChanged( CHANGE_EVENT );
			}
		}));
		textFieldOffset.setHorizontalAlignment(SwingConstants.TRAILING);
		textFieldOffset.setText("0");
		textFieldOffset.setColumns(10);
		
		GroupLayout gl_panelFitter = new GroupLayout(this);
		gl_panelFitter.setHorizontalGroup(
			gl_panelFitter.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelFitter.createSequentialGroup()
					.addGroup(gl_panelFitter.createParallelGroup(Alignment.TRAILING, false)
						.addGroup(gl_panelFitter.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblGain)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(textFieldGain, 0, 0, Short.MAX_VALUE))
						.addComponent(btnCalibration, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE))
					.addGroup(gl_panelFitter.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panelFitter.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lblCalibration, GroupLayout.PREFERRED_SIZE, 174, GroupLayout.PREFERRED_SIZE)
							.addContainerGap(170, Short.MAX_VALUE))
						.addGroup(Alignment.TRAILING, gl_panelFitter.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lblOffset)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(textFieldOffset, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE)
							.addGap(326))))
		);
		gl_panelFitter.setVerticalGroup(
			gl_panelFitter.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelFitter.createSequentialGroup()
					.addGroup(gl_panelFitter.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnCalibration)
						.addComponent(lblCalibration, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panelFitter.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblGain)
						.addComponent(textFieldGain, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblOffset)
						.addComponent(textFieldOffset, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(214, Short.MAX_VALUE))
		);
		setLayout(gl_panelFitter);
	}

	@Override
	public void setSettings(Map<String, Object> settings) {
		try{
			calibFile = (File) settings.get(PanelKeys.KEY_CALIBRATION_FILENAME);
			lblCalibration.setText(calibFile.getName());
			textFieldGain.setText(String.valueOf(settings.get(PanelKeys.KEY_GAIN)));
			textFieldOffset.setText(String.valueOf(settings.get(PanelKeys.KEY_OFFSET)));
		} catch (Exception e){}
	}

	@Override
	public Map<String, Object> getSettings() {
		final Map< String, Object > settings = new HashMap<String, Object>( 4 );
		try{
			settings.put(PanelKeys.KEY_GAIN, Double.parseDouble(textFieldGain.getText()));
			settings.put(PanelKeys.KEY_OFFSET, Double.parseDouble(textFieldOffset.getText()));
		} catch (Exception ex){}
		if (calibFile == null){
			return settings;
		}
		settings.put(PanelKeys.KEY_CALIBRATION_FILENAME, calibFile.getAbsolutePath());
		return settings;
	}
	
	protected void loadCalibrationFile(){
		JFileChooser fc = new JFileChooser(Controller.lastDir);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDialogTitle("Import Calibration File");
		int returnVal = fc.showOpenDialog(null);
		 
	    if (returnVal != JFileChooser.APPROVE_OPTION)
	    	return;
	    calibFile = fc.getSelectedFile();
	    Controller.lastDir = calibFile.getAbsolutePath();
	    lblCalibration.setText(calibFile.getName());
	}
}
