package org.swing;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.StackWindow;
import ij.plugin.FolderOpener;

import javax.swing.JFileChooser;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.calibration.Calibrator;


public class CalibrationPanel extends javax.swing.JPanel {

	private static final long serialVersionUID = 637884858291063556L;

    // Variables
    String file_path;
    ImagePlus im;
    double zstep;
    boolean isRoi = false;
    boolean rangeRight = false;
    boolean rangeLeft = false;
    double rangeMin, rangeMax;
    int nSlice;
    Roi roi;
    Calibrator cal;
    
    // Swing variables                    
    private javax.swing.JButton button_FitCurve;
    private javax.swing.JButton button_FitStack;
    private javax.swing.JButton button_Import;
    private javax.swing.JButton button_ROI;
    private javax.swing.JLabel label_RangeNm;
    private javax.swing.JLabel label_HyphRange;
    private javax.swing.JLabel label_RangeCalib;
    private javax.swing.JLabel label_Zstep;
    private javax.swing.JPanel panel_FitCalib;
    private javax.swing.JPanel panel_FitStack;
    private javax.swing.JPanel panel_Import;
    private javax.swing.JPanel panel_ROI;
    private javax.swing.JPanel panel_RangeCalib;
    private javax.swing.JPanel panel_Zstep;
    private javax.swing.JProgressBar progress_FitCurve;
    private javax.swing.JProgressBar progress_FitStack;
    private javax.swing.JTextField textfield_Import;
    private javax.swing.JTextField textfield_ROI;
    private javax.swing.JTextField textfield_RangeMax;
    private javax.swing.JTextField textfield_RangeMin;
    private javax.swing.JTextField textfield_Zstep;   
    private javax.swing.JPanel panel_save;
    private javax.swing.JButton jbutton_savetext;	
    
    public CalibrationPanel() {
        initComponents();
    }
    private void initComponents() {

        panel_Import = new javax.swing.JPanel();
        button_Import = new javax.swing.JButton();
        textfield_Import = new javax.swing.JTextField();
        panel_Zstep = new javax.swing.JPanel();
        label_Zstep = new javax.swing.JLabel();
        textfield_Zstep = new javax.swing.JTextField();
        panel_ROI = new javax.swing.JPanel();
        button_ROI = new javax.swing.JButton();
        textfield_ROI = new javax.swing.JTextField();
        panel_FitStack = new javax.swing.JPanel();
        button_FitStack = new javax.swing.JButton();
        progress_FitStack = new javax.swing.JProgressBar(0, 100);
        panel_FitCalib = new javax.swing.JPanel();
        button_FitCurve = new javax.swing.JButton();
        progress_FitCurve = new javax.swing.JProgressBar(0, 100);
        panel_RangeCalib = new javax.swing.JPanel();
        label_RangeCalib = new javax.swing.JLabel();
        label_HyphRange = new javax.swing.JLabel();
        textfield_RangeMax = new javax.swing.JTextField();
        textfield_RangeMin = new javax.swing.JTextField();
        label_RangeNm = new javax.swing.JLabel();
        panel_save = new javax.swing.JPanel();
		jbutton_savetext = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Calibration"));
        setToolTipText("");
        setPreferredSize(new java.awt.Dimension(240, 183));

        panel_Import.setMaximumSize(new java.awt.Dimension(265, 45));
        panel_Import.setMinimumSize(new java.awt.Dimension(265, 45));

        button_Import.setText("Import images");
        button_Import.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_ImportActionPerformed(evt);
            }
        });

        textfield_Import.setText("No image imported");
        textfield_Import.setEditable(false);

        javax.swing.GroupLayout panel_ImportLayout = new javax.swing.GroupLayout(panel_Import);
        panel_Import.setLayout(panel_ImportLayout);
        panel_ImportLayout.setHorizontalGroup(
            panel_ImportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_ImportLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(button_Import)
                .addGap(18, 18, 18)
                .addComponent(textfield_Import, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panel_ImportLayout.setVerticalGroup(
            panel_ImportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_ImportLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel_ImportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(button_Import)
                    .addComponent(textfield_Import, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panel_Zstep.setMaximumSize(new java.awt.Dimension(265, 45));
        panel_Zstep.setMinimumSize(new java.awt.Dimension(265, 45));
        panel_Zstep.setPreferredSize(new java.awt.Dimension(265, 45));

        label_Zstep.setText("z-step (nm):");

        textfield_Zstep.setText("10");
        textfield_Zstep.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textfield_ZstepActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panel_ZstepLayout = new javax.swing.GroupLayout(panel_Zstep);
        panel_Zstep.setLayout(panel_ZstepLayout);
        panel_ZstepLayout.setHorizontalGroup(
            panel_ZstepLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_ZstepLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(label_Zstep)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 68, Short.MAX_VALUE)
                .addComponent(textfield_Zstep, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(55, 55, 55))
        );
        panel_ZstepLayout.setVerticalGroup(
            panel_ZstepLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_ZstepLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel_ZstepLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(label_Zstep)
                    .addComponent(textfield_Zstep, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panel_ROI.setMaximumSize(new java.awt.Dimension(265, 45));
        panel_ROI.setMinimumSize(new java.awt.Dimension(265, 45));
        panel_ROI.setPreferredSize(new java.awt.Dimension(265, 45));

        button_ROI.setText("Select ROI");
        button_ROI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_ROIActionPerformed(evt);
            }
        });

        textfield_ROI.setText("No ROI selected");
        textfield_ROI.setEditable(false);

        javax.swing.GroupLayout panel_ROILayout = new javax.swing.GroupLayout(panel_ROI);
        panel_ROI.setLayout(panel_ROILayout);
        panel_ROILayout.setHorizontalGroup(
            panel_ROILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_ROILayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(button_ROI)
                .addGap(26, 26, 26)
                .addComponent(textfield_ROI, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                .addContainerGap())
        );
        panel_ROILayout.setVerticalGroup(
            panel_ROILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_ROILayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel_ROILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(button_ROI)
                    .addComponent(textfield_ROI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panel_FitStack.setMaximumSize(new java.awt.Dimension(265, 45));
        panel_FitStack.setMinimumSize(new java.awt.Dimension(265, 45));
        panel_FitStack.setPreferredSize(new java.awt.Dimension(265, 45));

        progress_FitStack.setValue(0);
        progress_FitStack.setStringPainted(true);
        progress_FitStack.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				 progress_FitStackChange();
			}
            });

        
        button_FitStack.setText("Fit Images");
        button_FitStack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_FitStackActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panel_FitStackLayout = new javax.swing.GroupLayout(panel_FitStack);
        panel_FitStack.setLayout(panel_FitStackLayout);
        panel_FitStackLayout.setHorizontalGroup(
            panel_FitStackLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_FitStackLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(button_FitStack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addComponent(progress_FitStack, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        panel_FitStackLayout.setVerticalGroup(
            panel_FitStackLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_FitStackLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel_FitStackLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(button_FitStack, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(progress_FitStack, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panel_FitCalib.setMaximumSize(new java.awt.Dimension(265, 45));
        panel_FitCalib.setMinimumSize(new java.awt.Dimension(265, 45));
        panel_FitCalib.setPreferredSize(new java.awt.Dimension(265, 45));

        button_FitCurve.setText("Fit Curves");
        button_FitCurve.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_FitCurveActionPerformed(evt);
            }
        });

        progress_FitCurve.setValue(0);
        progress_FitCurve.setStringPainted(true);

        javax.swing.GroupLayout panel_FitCalibLayout = new javax.swing.GroupLayout(panel_FitCalib);
        panel_FitCalib.setLayout(panel_FitCalibLayout);
        panel_FitCalibLayout.setHorizontalGroup(
            panel_FitCalibLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_FitCalibLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(button_FitCurve)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addComponent(progress_FitCurve, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        panel_FitCalibLayout.setVerticalGroup(
            panel_FitCalibLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_FitCalibLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel_FitCalibLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(button_FitCurve, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(progress_FitCurve, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panel_RangeCalib.setMaximumSize(new java.awt.Dimension(265, 45));
        panel_RangeCalib.setMinimumSize(new java.awt.Dimension(265, 45));
        panel_RangeCalib.setPreferredSize(new java.awt.Dimension(265, 45));

        label_RangeCalib.setText("Range calibration:");

        label_HyphRange.setText("-");

        textfield_RangeMax.setText("100");
        textfield_RangeMax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textfield_RangeMaxActionPerformed(evt);
            }
        });

        textfield_RangeMin.setText("-100");
        textfield_RangeMin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textfield_RangeMinActionPerformed(evt);
            }
        });

        label_RangeNm.setText("nm");

        javax.swing.GroupLayout panel_RangeCalibLayout = new javax.swing.GroupLayout(panel_RangeCalib);
        panel_RangeCalib.setLayout(panel_RangeCalibLayout);
        panel_RangeCalibLayout.setHorizontalGroup(
            panel_RangeCalibLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_RangeCalibLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(label_RangeCalib, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(textfield_RangeMin, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(label_HyphRange)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(textfield_RangeMax, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(label_RangeNm)
                .addContainerGap(18, Short.MAX_VALUE))
        );
        panel_RangeCalibLayout.setVerticalGroup(
            panel_RangeCalibLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_RangeCalibLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel_RangeCalibLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(label_RangeCalib, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
                    .addComponent(label_HyphRange)
                    .addComponent(textfield_RangeMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textfield_RangeMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label_RangeNm))
                .addContainerGap())
        );

        jbutton_savetext.setText("Save .txt");
		jbutton_savetext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_SaveTextActionPerformed(evt);
            }
        });
        javax.swing.GroupLayout panel_saveLayout = new javax.swing.GroupLayout(panel_save);
        panel_save.setLayout(panel_saveLayout);
        panel_saveLayout.setHorizontalGroup(
            panel_saveLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_saveLayout.createSequentialGroup()
                .addGap(82, 82, 82)
                .addComponent(jbutton_savetext)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panel_saveLayout.setVerticalGroup(
            panel_saveLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_saveLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jbutton_savetext)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setIncrementalLayout(0);
    }// </editor-fold>                        

    /**
    * Incremental layout function allows the apparition of the panels one by one.
    * 
    */
    private void setIncrementalLayout(int step) {
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        
        ParallelGroup gp = layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(panel_Import, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
        SequentialGroup sg = layout.createSequentialGroup()
                .addComponent(panel_Import, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
        
        switch (step) {
	        case 1:  
                	gp.addComponent(panel_Zstep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
                	sg.addComponent(panel_Zstep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
                    sg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
	                break;
	        case 2:  
            		gp.addComponent(panel_Zstep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
            		sg.addComponent(panel_Zstep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
            		sg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
	        		gp.addComponent(panel_ROI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
	                sg.addComponent(panel_ROI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
	                sg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
	                break;
	        case 3: 
	        		gp.addComponent(panel_Zstep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
	        		sg.addComponent(panel_Zstep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
	        		sg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
	        		gp.addComponent(panel_ROI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
	                sg.addComponent(panel_ROI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
	                sg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
	        		gp.addComponent(panel_FitStack, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
	                sg.addComponent(panel_FitStack, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
	                sg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
	        		break;
	        case 4:
			        gp.addComponent(panel_Zstep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
		    		sg.addComponent(panel_Zstep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
		    		sg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
		    		gp.addComponent(panel_ROI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
		            sg.addComponent(panel_ROI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
		            sg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
		    		gp.addComponent(panel_FitStack, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
		            sg.addComponent(panel_FitStack, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
		            sg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
	        		gp.addComponent(panel_RangeCalib, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
	                sg.addComponent(panel_RangeCalib, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
	        		break;
	        case 5:
			        gp.addComponent(panel_Zstep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
		    		sg.addComponent(panel_Zstep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
		    		sg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
		    		gp.addComponent(panel_ROI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
		            sg.addComponent(panel_ROI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
		            sg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
		    		gp.addComponent(panel_FitStack, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
		            sg.addComponent(panel_FitStack, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
		            sg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
	        		gp.addComponent(panel_RangeCalib, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
	                sg.addComponent(panel_RangeCalib, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
	                sg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
	        		gp.addComponent(panel_FitCalib, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
	                sg.addComponent(panel_FitCalib, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
	        		break;
	        case 6:
			        gp.addComponent(panel_Zstep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
		    		sg.addComponent(panel_Zstep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
		    		sg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
		    		gp.addComponent(panel_ROI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
		            sg.addComponent(panel_ROI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
		            sg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
		    		gp.addComponent(panel_FitStack, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
		            sg.addComponent(panel_FitStack, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
		            sg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
	        		gp.addComponent(panel_RangeCalib, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
	                sg.addComponent(panel_RangeCalib, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
	                sg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
	        		gp.addComponent(panel_FitCalib, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
	                sg.addComponent(panel_FitCalib, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
					sg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
					gp.addComponent(panel_save, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
	        		sg.addComponent(panel_save, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
	        		break;
        }
        
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(gp)));
        		
        		
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,sg));
		this.repaint();
	}

    
    /////////////////////////////////////////////////////////////////////////////////
    //// Misc functions
    
    public static boolean isNumber(String string) {
        if (string == null || string.isEmpty()) {
            return false;
        }
        int i = 0;
        if (string.charAt(0) == '-') {
            if (string.length() > 1) {
                i++;
            } else {
                return false;
            }
        }
        for (; i < string.length(); i++) {
            if (!Character.isDigit(string.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isNumeric(String str)  
    {  
      try  
      {  
        @SuppressWarnings("unused")
		double d = Double.parseDouble(str);  
      }  
      catch(NumberFormatException nfe)  
      {  
        return false;  
      }  
      return true;  
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    //// Action handlers
	private void button_ImportActionPerformed(java.awt.event.ActionEvent evt) {
    	JFileChooser fc = new JFileChooser();
    	fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    	
    	int returnVal = fc.showOpenDialog(this);
    	 
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fc.getSelectedFile();
            textfield_Import.setText(file.getName());
            file_path = file.getAbsolutePath();
            im = FolderOpener.open(file_path);
            nSlice = im.getNSlices();
            @SuppressWarnings("unused")
			StackWindow sw = new StackWindow(im);
    		setIncrementalLayout(1);
        }
    }                                             

    private void textfield_ZstepActionPerformed(java.awt.event.ActionEvent evt) {  
    	String s = textfield_Zstep.getText();
    	if(isNumeric(s)){
    		zstep = Double.parseDouble(s);
    		setIncrementalLayout(2);
    	}
    }                
    

	private void button_ROIActionPerformed(java.awt.event.ActionEvent evt) {
    	Roi roitemp = im.getRoi();
		try{																				
			double w = roitemp.getFloatWidth();
			double h = roitemp.getFloatHeight();
			textfield_ROI.setText("Roi ("+w+","+h+")");
			isRoi = true;
			roi = roitemp;
		} catch (NullPointerException e) {
			roi = new Roi(0, 0, im.getWidth(), im.getHeight());
			textfield_ROI.setText("Whole image");
		} 
		setIncrementalLayout(3);
	}                     

	private void button_FitStackActionPerformed(java.awt.event.ActionEvent evt) {
		cal = new Calibrator(im, zstep, roi);
		cal.fitStack(new ProgressDisplay(progress_FitStack,nSlice));
		//setIncrementalLayout(4);
	}
	
	private void progress_FitStackChange(){
		if(progress_FitStack.getValue()==100){
			 setIncrementalLayout(4);
		 }
	}
    
    private void textfield_RangeMaxActionPerformed(java.awt.event.ActionEvent evt) {     
    	String s = textfield_RangeMax.getText();
    	if(isNumeric(s)){
	    	rangeMax = Double.parseDouble(s);
	    	
	    	if(rangeMax > cal.getMaxZ()){
	    		rangeMax = cal.getMaxZ();
	    	}
	    	
	    	rangeLeft = true;
	    	if(rangeRight && rangeLeft){
	    	setIncrementalLayout(5);
	    	}
    	}
    }                                                  

    private void textfield_RangeMinActionPerformed(java.awt.event.ActionEvent evt) {
    	String s = textfield_RangeMin.getText();
    	if(isNumeric(s)){
	    	rangeMin = Double.parseDouble(s);

	    	if(rangeMin < cal.getMinZ()){
	    		rangeMin = cal.getMinZ();
	    	}
	    	
	    	rangeRight = true;
	    	if(rangeRight && rangeLeft){
	    	setIncrementalLayout(5);
	    	}     
    	}
    }                                                  

	private void button_FitCurveActionPerformed(java.awt.event.ActionEvent evt) {
		cal.fitCalibrationCurve(new ProgressDisplay(progress_FitCurve, 100),rangeMin, rangeMax);
		setIncrementalLayout(6);	
	}           
	
	private void button_SaveTextActionPerformed(java.awt.event.ActionEvent evt) {
		SaveTextFrame stf = new SaveTextFrame(cal);
		stf.setVisible(true);
	}
}
