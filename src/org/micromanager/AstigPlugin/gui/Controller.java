package org.micromanager.AstigPlugin.gui;

import ij.*;
import ij.gui.*;
import ij.measure.Calibration;
import ij.plugin.FolderOpener;
import ij.plugin.frame.ContrastAdjuster;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FontUIResource;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.micromanager.AstigPlugin.factories.FitterFactory;
import org.micromanager.AstigPlugin.factories.HistogramRendererFactory;
import org.micromanager.AstigPlugin.factories.NMSDetectorFactory;
import org.micromanager.AstigPlugin.factories.NMSFastMedianFactory;
import org.micromanager.AstigPlugin.interfaces.Element;
import org.micromanager.AstigPlugin.interfaces.Store;
import org.micromanager.AstigPlugin.math.Calibrator;
import org.micromanager.AstigPlugin.pipeline.*;
import org.micromanager.AstigPlugin.pipeline.Renderer;
import org.micromanager.AstigPlugin.plugins.AstigFitter;
import org.micromanager.AstigPlugin.plugins.NMSFastMedian;
import org.micromanager.AstigPlugin.providers.FitterProvider;
import org.micromanager.AstigPlugin.tools.FileInfoVirtualStack;
import org.micromanager.AstigPlugin.tools.LemmingUtils;

public class Controller<T extends NativeType<T> & RealType<T>> extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JTabbedPane tabbedPane;
	private JPanel panelLoc;
	private JPanel panelRecon;
	private JCheckBox chckbxPreprocessing;
	private JButton btnPeakDet;
	private JCheckBox chckbxROI;
	private JLabel lblSkipFrames;
	private JSpinner spinnerSkipFrames;
	private JPanel panelMiddle;
	private JButton btnProcess;
	private NMSDetectorFactory detectorFactory;
	private JLabel lblFitter;
	private FitterProvider fitterProvider;
	private JComboBox comboBoxFitter;
	private JCheckBox chkboxRenderer;
	private JCheckBox chkboxFilter;
	private FitterFactory fitterFactory;
	private HistogramRendererFactory rendererFactory;
	private JLabel lblFile;
	private Manager manager;
	private ExtendableTable table;
	private Map<String, Object> settings;
	private StackWindow previewerWindow;
	private AbstractModule detector;
	private Fitter<T> fitter;
	private ContrastAdjuster contrastAdjuster;
	private Renderer renderer;
	private FrameElements<T> detResults;
	private List<Element> fitResults;
	private ImageWindow rendererWindow;
	private int widgetSelection = 0;
	private boolean processed = false;
	private ExtendableTable filteredTable = null;
	private static int DETECTOR = 1;
	private static int FITTER = 2;
	private static int PREPROCESSOR = 3;
	private JProgressBar progressBar;
	private JButton btnReset;
	private JPanel panelFilter;
	private ImageLoader<T> tif;
	private JPanel panelCalibration;
	private JLabel labelStepSize;
	private JButton buttonFitBeads;
	private JLabel labelRange;
	private JButton buttonFitCurve;
	private JButton buttonSaveCal;
	private RangeSlider rangeSlider;
	private JButton btnImagesCalibration;
	private JSpinner spinnerStepSize;
	private StackWindow calibWindow;
	private Calibrator<T> calibrator;
	private JLabel lblEta;
	private long start;
	private SaveLocalizations saver;
	private CommonFitterPanel panelFitter;
	private JSpinner spinnerSkipLastFrames;
	private JPanel panelPeakDet;
	private JLabel lblMinrange;
	private JLabel lblMaxrange;
	private JButton btnDataSource;
	private Locale curLocale;
	private List<Double> cameraProps;
	private NMSFastMedianFactory<T> preProcessingFactory;
	public static String lastDir = System.getProperty("user.home"); 

	/**
	 * Create the frame.
	 */
	public Controller() {
		this.curLocale = Locale.getDefault();
		final Locale usLocale = new Locale("en", "US"); // setting us locale
		Locale.setDefault(usLocale);
		changeFontRecursive();
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (previewerWindow != null) previewerWindow.close();
				if (contrastAdjuster != null) contrastAdjuster.close();
				if (rendererWindow != null) rendererWindow.close();
				setVisible(false);
				Locale.setDefault(curLocale);
			}
		});
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e1) {
			IJ.error(e1.getMessage());
		}

		setTitle("3D Astigmatism Plugin");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 330, 500);
		JPanel contentPane = new JPanel();
		setContentPane(contentPane);

		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 315, 0 };
		gbl_contentPane.rowHeights = new int[] { 390, 22, 30, 0 };
		gbl_contentPane.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 1.0, 0.0, 0.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setFont(UIManager.getFont("TabbedPane.font"));
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (btnProcess == null) return;
				if (tabbedPane.getSelectedIndex() > 0) {
					btnProcess.setEnabled(true);
				} else {
					btnProcess.setEnabled(false);
				}
				saveSettings(panelPeakDet);
				saveSettings(panelFilter);
			}
		});

		panelCalibration = new JPanel();
		tabbedPane.addTab("Calibration", null, panelCalibration, null);

		labelStepSize = new JLabel("Step Size");

		buttonFitBeads = new JButton("Fit beads");
		buttonFitBeads.setToolTipText("Gaussian fitting of beads");
		buttonFitBeads.setEnabled(false);
		buttonFitBeads.addActionListener(this);

		labelRange = new JLabel("Range");

		buttonFitCurve = new JButton("Fit curve");
		buttonFitCurve.setToolTipText("Fit the two sigmas to obtain the calibration curve");
		buttonFitCurve.addActionListener(this);
		buttonFitCurve.setEnabled(false);

		buttonSaveCal = new JButton("Save Calibration");
		buttonSaveCal.setToolTipText("Save Calibration for later use");
		buttonSaveCal.addActionListener(this);
		buttonSaveCal.setEnabled(false);

		rangeSlider = new RangeSlider();
		rangeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if(lblMinrange == null || lblMaxrange == null) return;
				lblMinrange.setText(String.valueOf(rangeSlider.getValue()));
				lblMaxrange.setText(String.valueOf(rangeSlider.getUpperValue()));
			}
		});
		rangeSlider.setToolTipText("Adjust slider to constraint the data range");
		rangeSlider.setValue(200);
		rangeSlider.setUpperValue(1000);
		rangeSlider.setPaintTicks(true);
		rangeSlider.setPaintLabels(true);
		rangeSlider.setMinorTickSpacing(50);
		rangeSlider.setMaximum(1200);
		rangeSlider.setMajorTickSpacing(200);

		spinnerStepSize = new JSpinner();
		spinnerStepSize.setToolTipText("Step size for calibration data");
		spinnerStepSize.setModel(new SpinnerNumberModel(new Integer(10), new Integer(0), null, new Integer(1)));

		btnImagesCalibration = new JButton("Calibration Images");
		btnImagesCalibration.setToolTipText("Load Images for 3D calibration");
		btnImagesCalibration.addActionListener(this);

		lblMinrange = new JLabel(" ");
		lblMinrange.setHorizontalAlignment(SwingConstants.TRAILING);

		lblMaxrange = new JLabel(" ");
		lblMaxrange.setHorizontalAlignment(SwingConstants.TRAILING);
		GroupLayout gl_panelCalibration = new GroupLayout(panelCalibration);
		gl_panelCalibration.setHorizontalGroup(
			gl_panelCalibration.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelCalibration.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelCalibration.createParallelGroup(Alignment.LEADING, false)
						.addGroup(gl_panelCalibration.createSequentialGroup()
							.addGroup(gl_panelCalibration.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panelCalibration.createSequentialGroup()
									.addGap(8)
									.addComponent(labelStepSize, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(spinnerStepSize, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE))
								.addComponent(buttonFitBeads)
								.addComponent(btnImagesCalibration)
								.addComponent(buttonSaveCal)
								.addComponent(buttonFitCurve))
							.addPreferredGap(ComponentPlacement.RELATED, 190, Short.MAX_VALUE))
						.addGroup(gl_panelCalibration.createParallelGroup(Alignment.TRAILING)
							.addGroup(gl_panelCalibration.createSequentialGroup()
								.addGap(9)
								.addGroup(gl_panelCalibration.createParallelGroup(Alignment.LEADING, false)
									.addComponent(lblMinrange, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
									.addComponent(labelRange, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addPreferredGap(ComponentPlacement.RELATED, 192, Short.MAX_VALUE)
								.addComponent(lblMaxrange, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
								.addGap(65))
							.addComponent(rangeSlider, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 288, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap())
		);
		gl_panelCalibration.setVerticalGroup(
			gl_panelCalibration.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelCalibration.createSequentialGroup()
					.addGap(7)
					.addComponent(btnImagesCalibration)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panelCalibration.createParallelGroup(Alignment.BASELINE)
						.addComponent(spinnerStepSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(labelStepSize))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(buttonFitBeads)
					.addGap(4)
					.addComponent(labelRange)
					.addGap(18)
					.addGroup(gl_panelCalibration.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblMinrange)
						.addComponent(lblMaxrange))
					.addGap(3)
					.addComponent(rangeSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(buttonFitCurve)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(buttonSaveCal)
					.addContainerGap(173, Short.MAX_VALUE))
		);
		panelCalibration.setLayout(gl_panelCalibration);

		panelLoc = new JPanel();
		panelLoc.setBorder(UIManager.getBorder("List.focusCellHighlightBorder"));
		tabbedPane.addTab("Localize", null, panelLoc, null);
		GridBagLayout gbl_panelLoc = new GridBagLayout();
		gbl_panelLoc.columnWidths = new int[] { 250 };
		gbl_panelLoc.rowHeights = new int[] { 135, 30, 30, 30, 0 };
		gbl_panelLoc.columnWeights = new double[] { 1.0 };
		gbl_panelLoc.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 1.0 };
		panelLoc.setLayout(gbl_panelLoc);

		JPanel panelUpper = new JPanel();

		chckbxPreprocessing = new JCheckBox("Background Substraction");
		chckbxPreprocessing.setToolTipText("Temporal median filter for noise removal");
		chckbxPreprocessing.addActionListener(this);

		btnPeakDet = new JButton("Peak Detection");
		btnPeakDet.addActionListener(this);
		btnPeakDet.setToolTipText("Detection of localizations for fitting");
		GridBagConstraints gbc_panelUpper = new GridBagConstraints();
		gbc_panelUpper.fill = GridBagConstraints.VERTICAL;
		gbc_panelUpper.anchor = GridBagConstraints.WEST;
		gbc_panelUpper.gridx = 0;
		gbc_panelUpper.gridy = 0;
		panelLoc.add(panelUpper, gbc_panelUpper);

		lblFitter = new JLabel("Fitter");

		comboBoxFitter = new JComboBox();
		comboBoxFitter.setPreferredSize(new Dimension(32, 26));
		comboBoxFitter.addActionListener(this);

		lblFile = new JLabel("File");

		btnDataSource = new JButton("Load Images");
		btnDataSource.addActionListener(this);
		GroupLayout gl_panelUpper = new GroupLayout(panelUpper);
		gl_panelUpper.setHorizontalGroup(
			gl_panelUpper.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelUpper.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelUpper.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panelUpper.createSequentialGroup()
							.addComponent(btnDataSource, GroupLayout.PREFERRED_SIZE, 107, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lblFile, GroupLayout.PREFERRED_SIZE, 187, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_panelUpper.createSequentialGroup()
							.addComponent(lblFitter)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(comboBoxFitter, GroupLayout.PREFERRED_SIZE, 186, GroupLayout.PREFERRED_SIZE))
						.addComponent(chckbxPreprocessing)
						.addComponent(btnPeakDet))
					.addGap(22))
		);
		gl_panelUpper.setVerticalGroup(
			gl_panelUpper.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelUpper.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelUpper.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnDataSource)
						.addComponent(lblFile))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(chckbxPreprocessing, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnPeakDet, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panelUpper.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblFitter)
						.addComponent(comboBoxFitter, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(27))
		);
		panelUpper.setLayout(gl_panelUpper);

		panelMiddle = new JPanel();
		GridBagConstraints gbc_panelMiddle = new GridBagConstraints();
		gbc_panelMiddle.anchor = GridBagConstraints.WEST;
		gbc_panelMiddle.gridx = 0;
		gbc_panelMiddle.gridy = 1;
		panelLoc.add(panelMiddle, gbc_panelMiddle);

		chckbxROI = new JCheckBox("use ROI");
		chckbxROI.setToolTipText("use only that region for further processing");
		chckbxROI.addActionListener(this);

		lblSkipFrames = new JLabel(" | Skip frames");

		spinnerSkipFrames = new JSpinner();
		spinnerSkipFrames.setToolTipText("skip frames at the beginning of the image stack");
		spinnerSkipFrames.setPreferredSize(new Dimension(40, 28));
		spinnerSkipFrames.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));

		spinnerSkipLastFrames = new JSpinner();
		GroupLayout gl_panelMiddle = new GroupLayout(panelMiddle);
		gl_panelMiddle.setHorizontalGroup(
			gl_panelMiddle.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelMiddle.createSequentialGroup()
					.addComponent(chckbxROI)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblSkipFrames)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(spinnerSkipFrames, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(spinnerSkipLastFrames, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		gl_panelMiddle.setVerticalGroup(
			gl_panelMiddle.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelMiddle.createParallelGroup(Alignment.BASELINE)
					.addComponent(chckbxROI)
					.addComponent(lblSkipFrames)
					.addGroup(gl_panelMiddle.createSequentialGroup()
						.addGap(2)
						.addComponent(spinnerSkipFrames, GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE))
					.addGroup(gl_panelMiddle.createSequentialGroup()
						.addGap(3)
						.addComponent(spinnerSkipLastFrames)))
		);
		panelMiddle.setLayout(gl_panelMiddle);

		panelPeakDet = new JPanel();
		GridBagConstraints gbc_panelPeakDet = new GridBagConstraints();
		gbc_panelPeakDet.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelPeakDet.gridx = 0;
		gbc_panelPeakDet.gridy = 2;
		panelLoc.add(panelPeakDet, gbc_panelPeakDet);
		panelPeakDet.setLayout(new CardLayout(0, 0));

		panelFitter = new CommonFitterPanel();
		GridBagConstraints gbc_panelFitter = new GridBagConstraints();
		gbc_panelFitter.fill = GridBagConstraints.BOTH;
		gbc_panelFitter.gridx = 0;
		gbc_panelFitter.gridy = 3;
		panelLoc.add(panelFitter, gbc_panelFitter);

		panelRecon = new JPanel();
		panelRecon.setBorder(UIManager.getBorder("List.focusCellHighlightBorder"));
		tabbedPane.addTab("Reconstruct", null, panelRecon, null);
		GridBagLayout gbl_panelRecon = new GridBagLayout();
		gbl_panelRecon.columnWidths = new int[] { 300 };
		gbl_panelRecon.rowHeights = new int[] { 65, 280 };
		gbl_panelRecon.columnWeights = new double[] { 1.0 };
		gbl_panelRecon.rowWeights = new double[] { 0.0, 1.0 };
		panelRecon.setLayout(gbl_panelRecon);

		JPanel panelRenderer = new JPanel();
		GridBagConstraints gbc_panelRenderer = new GridBagConstraints();
		gbc_panelRenderer.fill = GridBagConstraints.BOTH;
		gbc_panelRenderer.gridx = 0;
		gbc_panelRenderer.gridy = 0;
		panelRecon.add(panelRenderer, gbc_panelRenderer);

		chkboxRenderer = new JCheckBox("HistogramRenderer");
		chkboxRenderer.setToolTipText("Zoomable Renderer with color code");
		chkboxRenderer.addActionListener(this);

		chkboxFilter = new JCheckBox("Filter");
		chkboxFilter.setToolTipText("filter results");
		chkboxFilter.addActionListener(this);

		btnReset = new JButton("Reset");
		btnReset.setToolTipText("Zoom out to see all data");
		btnReset.addActionListener(this);
		GroupLayout gl_panelRenderer = new GroupLayout(panelRenderer);
		gl_panelRenderer.setHorizontalGroup(
			gl_panelRenderer.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelRenderer.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelRenderer.createParallelGroup(Alignment.LEADING)
						.addComponent(chkboxRenderer)
						.addComponent(chkboxFilter))
					.addPreferredGap(ComponentPlacement.RELATED, 90, Short.MAX_VALUE)
					.addComponent(btnReset)
					.addContainerGap())
		);
		gl_panelRenderer.setVerticalGroup(
			gl_panelRenderer.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelRenderer.createSequentialGroup()
					.addContainerGap()
					.addComponent(chkboxRenderer)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(chkboxFilter)
					.addContainerGap(7, Short.MAX_VALUE))
				.addGroup(Alignment.TRAILING, gl_panelRenderer.createSequentialGroup()
					.addContainerGap(24, Short.MAX_VALUE)
					.addComponent(btnReset)
					.addGap(21))
		);
		panelRenderer.setLayout(gl_panelRenderer);

		panelFilter = new JPanel();
		panelFilter.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 205)), "none", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(
			0, 0, 205)));
		GridBagConstraints gbc_panelFilter = new GridBagConstraints();
		gbc_panelFilter.fill = GridBagConstraints.BOTH;
		gbc_panelFilter.gridx = 0;
		gbc_panelFilter.gridy = 1;
		panelRecon.add(panelFilter, gbc_panelFilter);
		panelFilter.setLayout(new CardLayout(0, 0));

		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.anchor = GridBagConstraints.NORTHWEST;
		gbc_tabbedPane.fill = GridBagConstraints.VERTICAL;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 0;
		contentPane.add(tabbedPane, gbc_tabbedPane);

		JPanel panelButtons = new JPanel();
		panelButtons.setBorder(null);
		panelButtons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		btnProcess = new JButton("Process");
		btnProcess.setEnabled(false);
		btnProcess.setToolTipText("Process all images");
		btnProcess.addActionListener(this);

		JPanel panelProgress = new JPanel();
		GridBagConstraints gbc_panelProgress = new GridBagConstraints();
		gbc_panelProgress.fill = GridBagConstraints.BOTH;
		gbc_panelProgress.gridx = 0;
		gbc_panelProgress.gridy = 1;
		contentPane.add(panelProgress, gbc_panelProgress);
		GridBagLayout gbl_panelProgress = new GridBagLayout();
		gbl_panelProgress.columnWidths = new int[] { 315, 0, 0 };
		gbl_panelProgress.rowHeights = new int[] { 0, 0 };
		gbl_panelProgress.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		gbl_panelProgress.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelProgress.setLayout(gbl_panelProgress);

		progressBar = new JProgressBar();
		progressBar.setMinimumSize(new Dimension(255, 20));
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.insets = new Insets(1, 10, 1, 10);
		gbc_progressBar.gridx = 0;
		gbc_progressBar.gridy = 0;
		panelProgress.add(progressBar, gbc_progressBar);
		progressBar.setStringPainted(true);

		lblEta = new JLabel("  0 sec");
		lblEta.setMinimumSize(new Dimension(50, 16));
		lblEta.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_lblEta = new GridBagConstraints();
		gbc_lblEta.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblEta.insets = new Insets(0, 0, 0, 10);
		gbc_lblEta.gridx = 1;
		gbc_lblEta.gridy = 0;
		panelProgress.add(lblEta, gbc_lblEta);
		panelButtons.add(btnProcess);
		GridBagConstraints gbc_panelButtons = new GridBagConstraints();
		gbc_panelButtons.anchor = GridBagConstraints.NORTH;
		gbc_panelButtons.gridx = 0;
		gbc_panelButtons.gridy = 2;
		contentPane.add(panelButtons, gbc_panelButtons);
		init();
	}
	
	private void changeFontRecursive() {
		UIManager.put("Label.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	    UIManager.put("Button.font", new FontUIResource(new Font("Dialog", Font.BOLD, 11)));
	    UIManager.put("TextField.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	    UIManager.put("ToggleButton.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	    UIManager.put("RadioButton.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	    UIManager.put("CheckBox.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	    UIManager.put("ColorChooser.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	    UIManager.put("ComboBox.font", new FontUIResource(new Font("Dialog", Font.BOLD, 11)));
	    UIManager.put("List.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	    UIManager.put("MenuBar.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	    UIManager.put("MenuItem.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	    UIManager.put("RadioButtonMenuItem.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	    UIManager.put("CheckBoxMenuItem.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	    UIManager.put("Menu.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	    UIManager.put("PopupMenu.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	    UIManager.put("OptionPane.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	    UIManager.put("Panel.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	    UIManager.put("ProgressBar.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	    UIManager.put("ScrollPane.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	    UIManager.put("Viewport.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	    UIManager.put("TabbedPane.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	    UIManager.put("Table.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	    UIManager.put("TableHeader.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	    UIManager.put("PasswordField.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	    UIManager.put("TextArea.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	    UIManager.put("TextPane.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	    UIManager.put("EditorPane.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	    UIManager.put("TitledBorder.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	    UIManager.put("ToolBar.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	    UIManager.put("ToolTip.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	    UIManager.put("Tree.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	    UIManager.put("Spinner.font", new FontUIResource(new Font("Dialog", Font.PLAIN, 11)));
	}

	private void init() {
		System.err.println("Using IJ: "+ IJ.getFullVersion());
		createProvider();
		createFitterProvider();
		createOtherPanels();
		settings = new HashMap<String, Object>();
		manager = new Manager(Executors.newCachedThreadPool());
		manager.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("progress")) {
					Integer value = (Integer) evt.getNewValue();
					progressBar.setValue(value);
					long current = System.currentTimeMillis();
					long eta = 0;
					if (value > 0) eta = Math.round((current - start) * value / 1000);
					lblEta.setText(String.valueOf(eta) + "sec");
					start = current;
				}
				if (evt.getPropertyName().equals("state")) {
					Integer value = (Integer) evt.getNewValue();
					if (value == Manager.STATE_DONE) {
						processed = true;
						lblEta.setText("0sec");
						if (rendererWindow != null) rendererWindow.repaint();
					}
				}
			}
		});
		
		ImagePlus.addImageListener(new ImageListener() {

			@Override
			public void imageClosed(ImagePlus ip) {
			}

			@Override
			public void imageOpened(ImagePlus ip) {
			}

			@Override
			public void imageUpdated(ImagePlus ip) {
				if (previewerWindow == null) return;
				if (ip == previewerWindow.getImagePlus()){
					if (widgetSelection == DETECTOR) detectorPreview(settings);
					if (widgetSelection == PREPROCESSOR) ppPreview(settings);
					if (widgetSelection == FITTER) fitterPreview(settings);
				}
			}
		});
		cameraProps = LemmingUtils.readCameraSettings(System.getProperty("user.home")+"/camera.props");
	}
	
	private void resetManager(){
		manager.reset();
		saver = null;
	}

	// //Overrides
	@Override
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();

		if (s == this.btnDataSource) {
			if (previewerWindow != null) {
				ImagePlus loc_im = loadImages();
				if (loc_im == null) {
					IJ.showMessage(getTitle(), "Loading images failed!");
					return;
				}
				tif = new ImageLoader<T>(loc_im, cameraProps);
				previewerWindow.setImage(loc_im);
				previewerWindow.getCanvas().fitToWindow();
				previewerWindow.repaint();
				lblFile.setText(previewerWindow.getTitle());
				repaint();
			}else{
				ImagePlus loc_im = null;
				try {
					previewerWindow = (StackWindow) WindowManager.getCurrentWindow();
					loc_im = previewerWindow.getImagePlus();
				} catch (Exception ex) {
					System.err.println("No current window opened: loading from file");
				}
				if (loc_im == null) {
					loc_im = loadImages();
					if (loc_im == null) {
						IJ.showMessage(getTitle(), "Loading images failed!");
						return;
					}
					previewerWindow = new StackWindow(loc_im, loc_im.getCanvas());
				}
				tif = new ImageLoader<T>(loc_im, cameraProps);

				previewerWindow.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if (e.getKeyChar() == 'C') {
							contrastAdjuster = new ContrastAdjuster();
							contrastAdjuster.run("B&C");
						}
					}
				});

				lblFile.setText(previewerWindow.getTitle());
				repaint();
			}
		}

		if (s == this.btnImagesCalibration) {
			if (importImages()) {
				this.buttonFitBeads.setEnabled(true);
			}
		}

		if (s == this.buttonFitBeads) {
			if (fitbeads()) {
				this.buttonFitCurve.setEnabled(true);
			}
		}

		if (s == this.buttonFitCurve) {
			if (fitCurve()) {
				this.buttonSaveCal.setEnabled(true);
			}
		}

		if (s == this.buttonSaveCal) {
			saveCalibration();
			this.buttonFitBeads.setEnabled(false);
			this.buttonFitCurve.setEnabled(false);
			this.buttonSaveCal.setEnabled(false);
		}

		if (s == this.chckbxROI) {
			setRoi();
		}

		if (s == this.btnPeakDet){
			chooseDetector();
		}

		if (s == this.comboBoxFitter) {
			chooseFitter();
		}

		if (s == this.chkboxFilter) {
			if (this.chkboxFilter.isSelected()){
				filterTable();
			} else {
				((TitledBorder) panelFilter.getBorder()).setTitle("none");
				((CardLayout) panelFilter.getLayout()).first(panelFilter);
				repaint();
			}
		}

		if (s == this.btnReset) {
			if (rendererFactory != null) {
				Map<String, Object> initialMap = rendererFactory.getInitialSettings();
				if (previewerWindow != null) {
					initialMap.put(PanelKeys.KEY_xmax, previewerWindow.getImagePlus().getWidth()
						* previewerWindow.getImagePlus().getCalibration().pixelDepth);
					initialMap.put(PanelKeys.KEY_ymax, previewerWindow.getImagePlus().getHeight()
						* previewerWindow.getImagePlus().getCalibration().pixelDepth);
				}
				filteredTable = null;
				settings.putAll(initialMap);
				rendererShow(settings);
			}
		}

		if (s == this.chkboxRenderer) {
			if (chkboxRenderer.isSelected()) {
				chooseRenderer();
			} else {
				((CardLayout) panelFilter.getLayout()).first(panelFilter);
				if (rendererWindow != null) {
					rendererWindow.close();
					rendererWindow = null;
				}
			}
		}
		
		if (s == this.chckbxPreprocessing){
			if (chckbxPreprocessing.isSelected())
				((CardLayout) panelPeakDet.getLayout()).show(panelPeakDet, preProcessingFactory.getKey());
			else
				((CardLayout) panelPeakDet.getLayout()).show(panelPeakDet, detectorFactory.getKey());
		}

		if (s == this.btnProcess) {
			process(true);
		}
	}

	private void process(boolean b) {// Manager
		final int elements = previewerWindow != null ? previewerWindow.getImagePlus().getStackSize() : 100;

		if (tif == null) {
			IJ.error("Please load images first!");
			return;
		}
		manager.add(tif);

		if (detector == null) {
			IJ.error("Please choose detector first!");
			return;
		}
		manager.add(detector);
		manager.linkModules(tif, detector, true, elements);
		
		if (fitter == null) 
			chooseFitter();
		manager.add(fitter);
		manager.linkModules(detector, fitter);
		DataTable dt = new DataTable();
		manager.add(dt);
		manager.linkModules(fitter, dt);
		table = dt.getTable();
		
		if (b) {
			if (saver == null) 
				saveLocalizations();
		}
		if (renderer != null) {
			manager.add(renderer);
			manager.linkModules(fitter, renderer, false, elements);
		}
		start = System.currentTimeMillis();
		manager.startAndJoin();
		resetManager();
	}

	// // Private Methods
	private ImagePlus loadImages() {
	
		final JFileChooser fc = new JFileChooser(lastDir);
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setDialogTitle("Import Images");

		final int returnVal = fc.showOpenDialog(this);

		if (returnVal != JFileChooser.APPROVE_OPTION) return null;

		final File file = fc.getSelectedFile();
		lastDir = file.getAbsolutePath();

		ImagePlus loc_im = null;
		if (file.isDirectory()) {
			final FolderOpener fo = new FolderOpener();
			fo.openAsVirtualStack(true);
			loc_im = fo.openFolder(file.getAbsolutePath());
		}

		if (file.isFile()) {
			loc_im = FileInfoVirtualStack.openVirtual(file.getAbsolutePath());
			loc_im.setOpenAsHyperStack(true);
			final int[] dims = loc_im.getDimensions();
			if (dims[4] == 1 && dims[3] > 1) { // swap Z With T
				loc_im.setDimensions(dims[2], dims[4], dims[3]);
				final Calibration calibration = loc_im.getCalibration();
				calibration.frameInterval = 1;
				calibration.setTimeUnit("frame");
			}
		}
		return loc_im;
	}

	private void setRoi() {
		if (previewerWindow == null) return;
		final ImagePlus curImage = previewerWindow.getImagePlus();
		if (chckbxROI.isSelected()) {
			Roi roi = curImage.getRoi();
			if (roi == null) {
				final Rectangle r = curImage.getProcessor().getRoi();
				final int iWidth = r.width / 2;
				final int iHeight = r.height / 2;
				final int iXROI = r.x + r.width / 4;
				final int iYROI = r.y + r.height / 4;
				curImage.setRoi(iXROI, iYROI, iWidth, iHeight);
			}
		} else {
			curImage.killRoi();
		}
	}

	private boolean importImages() {
		final JFileChooser fc = new JFileChooser(lastDir);
		fc.setLocation(getLocation());
		fc.setDialogTitle("Import Calibration Images");
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		final int returnVal = fc.showOpenDialog(this);

		if (returnVal != JFileChooser.APPROVE_OPTION) return false;

		final File file = fc.getSelectedFile();
		lastDir = file.getAbsolutePath();
		
		ImagePlus calibImage = new ImagePlus();
		if (file.isDirectory()) 
			calibImage = FolderOpener.open(file.getAbsolutePath());
		
		if (file.isFile()) 
			calibImage = new ImagePlus(file.getAbsolutePath());

		calibWindow = new StackWindow(calibImage);
		final int halfkernel = (Math.min(Math.min(calibImage.getWidth(),calibImage.getHeight()), 20)-1)/2;
		calibImage.setRoi(calibImage.getWidth() / 2 - halfkernel, calibImage.getHeight() / 2 - halfkernel, 2*halfkernel+1, 2*halfkernel+1);
		return calibWindow != null;
	}

	private boolean fitbeads() {
		final Roi roitemp = calibWindow.getImagePlus().getRoi();
		Roi calibRoi = null;
		try {
			final double w = roitemp.getFloatWidth();
			final double h = roitemp.getFloatHeight();
			if (w != h) {
				IJ.showMessage("Needs a square ROI /n(hint: press Shift).");
				return false;
			}
			calibRoi = roitemp;
		} catch (NullPointerException e) {
			calibRoi = new Roi(0, 0, calibWindow.getImagePlus().getWidth(), calibWindow.getImagePlus().getHeight());
		}
		
		calibWindow.getImagePlus().killRoi();
		final int zstep = (Integer) this.spinnerStepSize.getValue();
		calibrator = new Calibrator<T>(calibWindow.getImagePlus(), LemmingUtils.readCameraSettings(System.getProperty("user.home")+"/camera.props"), zstep, calibRoi);
		calibrator.fitStack();
		final double[] zgrid = calibrator.getZgrid();
		Arrays.sort(zgrid);
		
		int spacing = (int) Math.round((zgrid[zgrid.length-1]-zgrid[0])/500)*100;
		this.rangeSlider.setMajorTickSpacing(spacing);
		this.rangeSlider.setMinorTickSpacing(spacing/4);
		final Hashtable<?, ?> ht = this.rangeSlider.createStandardLabels(spacing, (int) zgrid[0]);
		this.rangeSlider.setMinimum((int) zgrid[0]);
		this.rangeSlider.setMaximum((int) zgrid[zgrid.length - 1]);
		this.rangeSlider.setValue((int) zgrid[0]);
		this.rangeSlider.setUpperValue((int) zgrid[zgrid.length - 1]);
		this.rangeSlider.setLabelTable(ht);
		return true;
	}

	private boolean fitCurve() {
		final int rangeMin = rangeSlider.getValue();
		final int rangeMax = rangeSlider.getUpperValue();
		calibrator.fitBSplines(rangeMin, rangeMax);
		return true;
	}

	private void saveCalibration() {
		final JFileChooser fc = new JFileChooser(lastDir);
		fc.setLocation(getLocation());
		fc.setDialogTitle("Save calibration");
		final FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV files", "csv", "CSV");
		fc.setFileFilter(filter);

		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File calibFile = fc.getSelectedFile();
			lastDir = calibFile.getAbsolutePath();
			calibrator.saveCalib(calibFile.getAbsolutePath());
			settings.put(PanelKeys.KEY_CALIBRATION_FILENAME, calibFile);
		}
		calibrator.closePlotWindows();
		calibWindow.close();
	}

	private void choosePP() {
		if (tif == null) {
			if (previewerWindow != null) previewerWindow.getImagePlus().killRoi();
			return;
		}
		ConfigurationPanel panelDown = preProcessingFactory.getConfigurationPanel();
		settings.putAll(panelDown.getSettings());
		ppPreview(panelDown.getSettings());
		widgetSelection = PREPROCESSOR;
	}
	
	private void ppPreview(Map<String, Object> map){
		preProcessingFactory.setAndCheckSettings(map);
		final NMSFastMedian<T> preProcessor = preProcessingFactory.getModule();
		final ImagePlus img = previewerWindow.getImagePlus();
		detResults = preProcessor.preview(previewerWindow.getImagePlus(), preProcessingFactory.processingFrames(), cameraProps);
		if (detResults.getList().isEmpty()) return;
		final FloatPolygon points = LemmingUtils.convertToPoints(detResults.getList(), new Rectangle(0,0,img.getWidth(),img.getHeight()), 1);
		final PointRoi roi = new PointRoi(points);
		previewerWindow.getImagePlus().setRoi(roi);
	}
	
	private void saveSettings(JPanel cardPanel) {
		if (cardPanel == null) return;
		for (Component comp : cardPanel.getComponents()) {
			if (comp instanceof ConfigurationPanel) {
				final Map<String, Object> s = ((ConfigurationPanel) comp).getSettings();
				if (s != null) for (String key : s.keySet())
					settings.put(key, s.get(key));
			}
		}
	}

	private void chooseDetector() {		
		if (chckbxPreprocessing.isSelected()) {
			choosePP(); 
			return;
		}
		ConfigurationPanel panelDown = detectorFactory.getConfigurationPanel();
		settings.putAll(panelDown.getSettings());
		detectorPreview(panelDown.getSettings());
		widgetSelection = DETECTOR;
	}

	@SuppressWarnings("unchecked")
	private void detectorPreview(Map<String, Object> map) {
		previewerWindow.getImagePlus().killRoi();
		detectorFactory.setAndCheckSettings(map);
		detector = detectorFactory.getDetector();
		final int frameNumber = previewerWindow.getImagePlus().getFrame();
		final double pixelSize = previewerWindow.getImagePlus().getCalibration().pixelDepth;
		Roi currentRoi = previewerWindow.getImagePlus().getRoi();
		ImageProcessor ip = previewerWindow.getImagePlus().getStack().getProcessor(frameNumber);
		final double offset = cameraProps.get(0);
		final double em_gain = cameraProps.get(1);
		final double conversion = cameraProps.get(2);
		if (currentRoi != null){
			ip.setRoi(currentRoi.getBounds());
			ip = ip.crop();
		} else{
			currentRoi = new Roi(0,0,ip.getWidth(),ip.getHeight());
		}

		final Img<T> curImage = LemmingUtils.wrap(ip.getPixels(), new long[] { ip.getWidth(), ip.getHeight() });
		final Cursor<T> it = curImage.cursor();
		while(it.hasNext()){
			it.fwd();
			final double adu = Math.max((it.get().getRealDouble()-offset), 0);
			final double im2phot = adu*conversion/em_gain;
			it.get().setReal(im2phot);
		}
		final ImgLib2Frame<T> curFrame = new ImgLib2Frame<T>(
			frameNumber, (int) curImage.dimension(0), (int) curImage.dimension(1), pixelSize, curImage);

		detResults = (FrameElements<T>) detector.preview(curFrame);
		if (detResults.getList().isEmpty()) return;
		final FloatPolygon points = LemmingUtils.convertToPoints(detResults.getList(), currentRoi.getBounds() ,pixelSize);
		final PointRoi roi = new PointRoi(points);
		previewerWindow.getImagePlus().setRoi(roi);
	}

	private void chooseFitter() {
		final int index = comboBoxFitter.getSelectedIndex();
		if (index < 0 || tif == null || detector == null) {
			fitter = null;
			return;
		}

		final String key = fitterProvider.getVisibleKeys().get(index);
		fitterFactory = fitterProvider.getFactory(key);
		System.out.println("AstigPlugin: Fitter_" + index + " : " + key);
		final ConfigurationPanel panelDown = fitterFactory.getConfigurationPanel();
		final Map<String, Object> fitterSettings = panelDown.getSettings();
		
		settings.putAll(fitterSettings);
		final Object calibFile = settings.get(PanelKeys.KEY_CALIBRATION_FILENAME);
		if (calibFile == null && key == AstigFitter.KEY){
			panelFitter.loadCalibrationFile();
			settings.put(PanelKeys.KEY_CALIBRATION_FILENAME, panelDown.getSettings().get(PanelKeys.KEY_CALIBRATION_FILENAME));
		}
		repaint();
		fitterPreview(settings);
		widgetSelection = FITTER;
	}

	private void fitterPreview(Map<String, Object> map) {
		if (fitterFactory == null) return;
		if (!fitterFactory.setAndCheckSettings(map)) return;
		fitter = fitterFactory.getFitter();

		previewerWindow.getImagePlus().killRoi();
		final int frameNumber = previewerWindow.getImagePlus().getFrame();
		final double pixelSize = previewerWindow.getImagePlus().getCalibration().pixelDepth;
		Roi currentRoi = previewerWindow.getImagePlus().getRoi();
		ImageProcessor ip = previewerWindow.getImagePlus().getStack().getProcessor(frameNumber);
		final double offset = cameraProps.get(0);
		final double em_gain = cameraProps.get(1);
		final double conversion = cameraProps.get(2);
		if (currentRoi != null){
			ip.setRoi(currentRoi.getBounds());
			ip = ip.crop();
		} else{
			ip = previewerWindow.getImagePlus().getStack().getProcessor(frameNumber);
			currentRoi = new Roi(0,0,ip.getWidth(),ip.getHeight());
		}
		
		final Img<T> curImage = LemmingUtils.wrap(ip.getPixels(), new long[] { ip.getWidth(), ip.getHeight() });
		final Cursor<T> it = curImage.cursor();
		while(it.hasNext()){
			it.fwd();
			final double adu = Math.max((it.get().getRealDouble()-offset), 0);
			final double im2phot = adu*conversion/em_gain;
			it.get().setReal(im2phot);
		}
		final ImgLib2Frame<T> curFrame = new ImgLib2Frame<T>(
			frameNumber, (int) curImage.dimension(0), (int) curImage.dimension(1), pixelSize, curImage);

		fitResults = fitter.fit(detResults.getList(), curFrame.getPixels(), fitter.getWindowSize(), frameNumber, pixelSize);
		final FloatPolygon points = LemmingUtils.convertToPoints(fitResults, currentRoi.getBounds(), pixelSize);
		final PointRoi roi = new PointRoi(points);
		previewerWindow.getImagePlus().setRoi(roi);
	}

	// sets the previewer size as initials
	private void chooseRenderer() {
		((TitledBorder) panelFilter.getBorder()).setTitle(rendererFactory.getName());
		((CardLayout) panelFilter.getLayout()).show(panelFilter, rendererFactory.getKey());

		Map<String, Object> rendererSettings = rendererFactory.getSettings();
		if (previewerWindow != null) {
			rendererSettings.put(PanelKeys.KEY_xmax, previewerWindow.getImagePlus().getWidth()
				* previewerWindow.getImagePlus().getCalibration().pixelDepth);
			rendererSettings.put(PanelKeys.KEY_ymax, previewerWindow.getImagePlus().getHeight()
				* previewerWindow.getImagePlus().getCalibration().pixelDepth);
		}
		settings.putAll(rendererSettings);
		rendererFactory.setAndCheckSettings(rendererSettings);
		renderer = rendererFactory.getRenderer();

		initRenderer();

		if (processed)
			rendererShow(rendererSettings);
		else
			rendererPreview(rendererSettings);
	}

	// set a new Renderer
	private void initRenderer() {
		rendererWindow = new ImageWindow(renderer.getImage());
		rendererWindow.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == 'C') {
					contrastAdjuster = new ContrastAdjuster();
					contrastAdjuster.run("B&C");
				}
			}
		});
		rendererWindow.getCanvas().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					if (table == null) {
						IJ.showMessage(getTitle(), "Zoom only works if you press Process first!");
						return;
					}
					try {
						Rectangle rect = renderer.getImage().getRoi().getBounds();

						final double xmin = (Double) settings.get(PanelKeys.KEY_xmin);
						final double xmax = (Double) settings.get(PanelKeys.KEY_xmax);
						final double ymin = (Double) settings.get(PanelKeys.KEY_ymin);
						final double ymax = (Double) settings.get(PanelKeys.KEY_ymax);
						final int xbins = (Integer) settings.get(PanelKeys.KEY_xBins);
						final int ybins = (Integer) settings.get(PanelKeys.KEY_yBins);

						final double new_xmin = (xmax - xmin) * rect.getMinX() / xbins + xmin;
						final double new_ymin = (ymax - ymin) * rect.getMinY() / ybins + ymin;
						final double new_xmax = (xmax - xmin) * rect.getMaxX() / xbins + xmin;
						final double new_ymax = (ymax - ymin) * rect.getMaxY() / ybins + ymin;
						final double factx = rect.getWidth() / rect.getHeight();
						final double facty = rect.getHeight() / rect.getWidth();
						final double ar = Math.min(factx, facty);
						final int new_xbins = (int) (Math.round(xbins * ar));
						final int new_ybins = (int) (Math.round(ybins * ar));

						settings.put(PanelKeys.KEY_xmin, new_xmin);
						settings.put(PanelKeys.KEY_ymin, new_ymin);
						settings.put(PanelKeys.KEY_xmax, new_xmax);
						settings.put(PanelKeys.KEY_ymax, new_ymax);
						settings.put(PanelKeys.KEY_xBins, new_xbins);
						settings.put(PanelKeys.KEY_yBins, new_ybins);
						rendererShow(settings);
					} catch (NullPointerException ne) {
					}
				}
			}
		});
	}

	private void rendererPreview(Map<String, Object> map) {
		rendererFactory.setAndCheckSettings(map);
		final List<Element> list = fitResults;
		if (list != null && !list.isEmpty()) {
			renderer.preview(list);
		}
	}

	private void rendererShow(Map<String, Object> map) {
		rendererFactory.setAndCheckSettings(map);
		renderer = rendererFactory.getRenderer();
		rendererWindow.setImage(renderer.getImage());
		rendererWindow.getCanvas().fitToWindow();
		final ExtendableTable tableToRender = filteredTable == null ? table : filteredTable;

		if (tableToRender != null && tableToRender.columnNames().size() > 0) {
			final Store previewStore = tableToRender.getFIFO();
			System.out.println("AstigPlugin: Rendering " + tableToRender.getNumberOfRows() + " elements");
			renderer.setInput(previewStore);
			renderer.run();
			renderer.resetInputStore();
			rendererWindow.repaint();
		}
	}

	private void filterTable() {
		if (!processed) {
			if (IJ.showMessageWithCancel("Filter", "Pipeline not yet processed.\nDo you want to process it now?"))
				process(false);
			else
				return;
			if (fitter == null) return;
			start = System.currentTimeMillis();
			manager.startAndJoin();
			return;
		}
		if (table == null) return;
		if (table.getNames().isEmpty()) return;
		FilterPanel fp = new FilterPanel(table);
		fp.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (table.filtersCollection.isEmpty()) {
					filteredTable = null;
				} else {
					filteredTable = table.filter();
				}
				if (renderer != null) rendererShow(settings);
			}
		});
		panelFilter.add(fp, FilterPanel.KEY);
		((TitledBorder) panelFilter.getBorder()).setTitle("Filter");
		((CardLayout) panelFilter.getLayout()).show(panelFilter, FilterPanel.KEY);
		repaint();
		if (renderer != null)
			rendererShow(settings);
		else
			IJ.showMessage(getTitle(), "No renderer chosen!\n No data will be displayed.");
	}

	private void saveLocalizations() {
		final JFileChooser fc = new JFileChooser(lastDir);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDialogTitle("Save Data");
		final FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV files", "csv", "CSV");
		fc.setFileFilter(filter);

		if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
		final File file = fc.getSelectedFile();
		lastDir = file.getAbsolutePath();
		if (this.chkboxFilter.isSelected()) {
			ExtendableTable tableToProcess = filteredTable == null ? table : filteredTable;
			Store s = tableToProcess.getFIFO();
			StoreSaver tSaver = new StoreSaver(file);
			tSaver.putMetadata(settings);
			tSaver.setInput(s);
			tSaver.run();
		} else {
			if (fitter != null) {
				saver = new SaveLocalizations(file);
				manager.add(saver);
				manager.linkModules(fitter, saver, false, 100);
			} else {
				IJ.showMessage(getTitle(), "No Fitter chosen!");
			}
		}
	}

	private void createProvider() {
		preProcessingFactory = new NMSFastMedianFactory<T>();
		detectorFactory = new NMSDetectorFactory();
		rendererFactory = new HistogramRendererFactory();

		final ConfigurationPanel ndp = detectorFactory.getConfigurationPanel();
		ndp.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				Map<String, Object> value = ndp.getSettings();
				detectorPreview(value);
			}
		});
		panelPeakDet.add(ndp, detectorFactory.getKey());
		
		final ConfigurationPanel nfp = preProcessingFactory.getConfigurationPanel();
		nfp.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				Map<String, Object> value = nfp.getSettings();
				ppPreview(value);
			}
		});
		panelPeakDet.add(nfp, preProcessingFactory.getKey());
	}

	private void createOtherPanels() {
		final ConfigurationPanel panelSecond = new ConfigurationPanel() {
			private static final long serialVersionUID = -614688452106890922L;

			@Override
			public void setSettings(Map<String, Object> settings) {
			}

			@Override
			public Map<String, Object> getSettings() {
				return null;
			}
		};
		panelFilter.add(panelSecond, "SECOND");
		final ConfigurationPanel hrp = rendererFactory.getConfigurationPanel();
		hrp.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent evt) {
				final Map<String, Object> map = hrp.getSettings();
				if (processed)
					rendererShow(map);
				else
					rendererPreview(map);
			}
		});
		panelFilter.add(hrp, HistogramRendererFactory.KEY);
	}

	private void createFitterProvider() {
		fitterProvider = new FitterProvider();
		final List<String> visibleKeys = fitterProvider.getVisibleKeys();
		final List<String> fitterNames = new ArrayList<String>();
		final List<String> infoTexts = new ArrayList<String>();
		for (final String key : visibleKeys) {
			final FitterFactory factory = fitterProvider.getFactory(key);
			factory.setConfigurationPanel((ConfigurationPanel) panelFitter);
			final ConfigurationPanel cfp = factory.getConfigurationPanel();
			cfp.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					fitterPreview(settings);
				}
			});
			fitterNames.add(factory.getName());
			infoTexts.add(factory.getInfoText());
		}
		final String[] names = fitterNames.toArray(new String[] {});
		comboBoxFitter.setModel(new DefaultComboBoxModel(names));
		comboBoxFitter.setRenderer(new ToolTipRenderer(infoTexts));

	}

	private class ToolTipRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;
		List<String> tooltips;

		public ToolTipRenderer(List<String> tooltips) {
			this.tooltips = tooltips;
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			JComponent comp = (JComponent) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (0 < index && null != value && null != tooltips) list.setToolTipText(tooltips.get(index - 1));
			return comp;
		}

	}
}
