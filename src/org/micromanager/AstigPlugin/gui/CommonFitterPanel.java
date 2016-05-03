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

public class CommonFitterPanel extends ConfigurationPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3081886846323191618L;
	private final JLabel lblCalibration;
	private File calibFile;
	private final ChangeEvent CHANGE_EVENT = new ChangeEvent(this);

	public CommonFitterPanel() {
		setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 205)), "Astigmatism Fitter", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 205)));
		
		lblCalibration = new JLabel("File");
		lblCalibration.setAlignmentX(0.5f);

		JButton btnCalibration = new JButton("Calib. File");
		btnCalibration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadCalibrationFile();
				fireChanged( CHANGE_EVENT );
			}
		});
		
		GroupLayout gl_panelFitter = new GroupLayout(this);
		gl_panelFitter.setHorizontalGroup(
			gl_panelFitter.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelFitter.createSequentialGroup()
					.addComponent(btnCalibration)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblCalibration, GroupLayout.PREFERRED_SIZE, 174, GroupLayout.PREFERRED_SIZE)
					.addGap(252))
		);
		gl_panelFitter.setVerticalGroup(
			gl_panelFitter.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelFitter.createSequentialGroup()
					.addGroup(gl_panelFitter.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnCalibration)
						.addComponent(lblCalibration, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(251, Short.MAX_VALUE))
		);
		setLayout(gl_panelFitter);
	}

	@Override
	public void setSettings(Map<String, Object> settings) {
		try{
			calibFile = new File((String) settings.get(PanelKeys.KEY_CALIBRATION_FILENAME));
			lblCalibration.setText(calibFile.getName());
		} catch (Exception e){e.printStackTrace();}
	}

	@Override
	public Map<String, Object> getSettings() {
		final Map< String, Object > settings = new HashMap<String, Object>( 4 );
		if (calibFile == null){
			return settings;
		}
		settings.put(PanelKeys.KEY_CALIBRATION_FILENAME, calibFile.getAbsolutePath());
		return settings;
	}
	
	void loadCalibrationFile(){
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
