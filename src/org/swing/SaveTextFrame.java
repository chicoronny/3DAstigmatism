package org.swing;

import java.io.File;

import javax.swing.JFileChooser;

import org.calibration.Calibrator;

public class SaveTextFrame extends javax.swing.JFrame {

	private static final long serialVersionUID = 674609648736047813L;


    // Variables declaration - do not modify                     
    private javax.swing.JButton jButton_Save;
    private javax.swing.JCheckBox jCheckBox_Calib;
    private javax.swing.JCheckBox jCheckBox_Exp;
    private javax.swing.JCheckBox jCheckBox_Fit;
    private javax.swing.JPanel jPanel_checkbox;
    private javax.swing.JPanel jPanel_save;
    
    private boolean bexp, bfit, bcalib;
    Calibrator cal;
    // End of variables declaration        
    
    
    public SaveTextFrame(Calibrator calib) {
    	cal = calib;
    	bexp = false;
    	bfit = false;
    	bcalib = false;
        initComponents();
    }
    private void initComponents() {

        jPanel_checkbox = new javax.swing.JPanel();
        jCheckBox_Exp = new javax.swing.JCheckBox();
        jCheckBox_Fit = new javax.swing.JCheckBox();
        jCheckBox_Calib = new javax.swing.JCheckBox();
        jPanel_save = new javax.swing.JPanel();
        jButton_Save = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jCheckBox_Exp.setText("Experimental points");
        jCheckBox_Exp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_ExpActionPerformed(evt);
            }
        });

        jCheckBox_Fit.setText("Fitted curves");
        jCheckBox_Fit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_FitActionPerformed(evt);
            }
        });

        jCheckBox_Calib.setText("Calibration curve");
        jCheckBox_Calib.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_CalibActionPerformed(evt);
            }
        });

        jButton_Save.setText("Save");
        jButton_Save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_SaveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel_saveLayout = new javax.swing.GroupLayout(jPanel_save);
        jPanel_save.setLayout(jPanel_saveLayout);
        jPanel_saveLayout.setHorizontalGroup(
            jPanel_saveLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_saveLayout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addComponent(jButton_Save)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel_saveLayout.setVerticalGroup(
            jPanel_saveLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_saveLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton_Save)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel_checkboxLayout = new javax.swing.GroupLayout(jPanel_checkbox);
        jPanel_checkbox.setLayout(jPanel_checkboxLayout);
        jPanel_checkboxLayout.setHorizontalGroup(
            jPanel_checkboxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_checkboxLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel_checkboxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox_Exp)
                    .addComponent(jCheckBox_Fit)
                    .addComponent(jCheckBox_Calib))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jPanel_save, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel_checkboxLayout.setVerticalGroup(
            jPanel_checkboxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_checkboxLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jCheckBox_Exp)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox_Fit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox_Calib)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel_save, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel_checkbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel_checkbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }        
    
    // Action handlers
    private void jCheckBox_ExpActionPerformed(java.awt.event.ActionEvent evt){
    	if(jCheckBox_Exp.isSelected()){
    		bexp = true;
    	} else {
    		bexp = false;
    	}
    }
    
    private void jCheckBox_FitActionPerformed(java.awt.event.ActionEvent evt){
    	if(jCheckBox_Fit.isSelected()){
    		bfit = true;
    	} else {
    		bfit = false;
    	}
    }
    
    private void jCheckBox_CalibActionPerformed(java.awt.event.ActionEvent evt){
    	if(jCheckBox_Calib.isSelected()){
    		bcalib = true;
    	} else {
    		bcalib = false;
    	}
    }
    
    private void jButton_SaveActionPerformed(java.awt.event.ActionEvent evt){
        //Handle save button action.
    	JFileChooser fc = new JFileChooser();
    	int returnVal = fc.showSaveDialog(this);
    	if (returnVal == JFileChooser.APPROVE_OPTION) {
    		File file = fc.getSelectedFile();
    		if(bexp){
    			cal.saveExp(file.getAbsolutePath());
    		}
    		if(bfit){
    			cal.saveFit(file.getAbsolutePath());
    		}
    		if(bcalib){
    			cal.saveCalib(file.getAbsolutePath());	
    		}
    	} else {
        }    
    	this.dispose();;
    }
}
