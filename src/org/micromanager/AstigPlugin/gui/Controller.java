package org.micromanager.AstigPlugin.gui;

import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.gui.StackWindow;
import ij.measure.Calibration;
import ij.plugin.FolderOpener;
import ij.plugin.frame.ContrastAdjuster;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.JTabbedPane;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JButton;

import java.awt.FlowLayout;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.UIManager;

import org.micromanager.AstigPlugin.factories.FastMedianFilterFactory;
import org.micromanager.AstigPlugin.factories.FitterFactory;
import org.micromanager.AstigPlugin.factories.HistogramRendererFactory;
import org.micromanager.AstigPlugin.factories.NMSDetectorFactory;
import org.micromanager.AstigPlugin.interfaces.Element;
import org.micromanager.AstigPlugin.interfaces.Frame;
import org.micromanager.AstigPlugin.interfaces.Store;
import org.micromanager.AstigPlugin.math.Calibrator;
import org.micromanager.AstigPlugin.pipeline.AbstractModule;
import org.micromanager.AstigPlugin.pipeline.DataTable;
import org.micromanager.AstigPlugin.pipeline.ExtendableTable;
import org.micromanager.AstigPlugin.pipeline.FastStore;
import org.micromanager.AstigPlugin.pipeline.Fitter;
import org.micromanager.AstigPlugin.pipeline.FrameElements;
import org.micromanager.AstigPlugin.pipeline.ImageMath;
import org.micromanager.AstigPlugin.pipeline.ImageMath.operators;
import org.micromanager.AstigPlugin.pipeline.ImgLib2Frame;
import org.micromanager.AstigPlugin.pipeline.ImageLoader;
import org.micromanager.AstigPlugin.pipeline.Manager;
import org.micromanager.AstigPlugin.pipeline.Renderer;
import org.micromanager.AstigPlugin.pipeline.SaveLocalizationPrecision3D;
import org.micromanager.AstigPlugin.pipeline.StoreSaver;
import org.micromanager.AstigPlugin.providers.FitterProvider;
import org.micromanager.AstigPlugin.tools.FileInfoVirtualStack;
import org.micromanager.AstigPlugin.tools.LemmingUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.awt.Component;

import javax.swing.SpinnerNumberModel;

import java.awt.Dimension;
import java.awt.event.KeyAdapter;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JProgressBar;

import java.awt.CardLayout;

import javax.swing.SwingConstants;


public class Controller<T extends NumericType<T> & NativeType<T> & RealType<T>, F extends Frame<T>> extends JFrame implements 
	ActionListener {

	private static final long serialVersionUID = -2596199192028890712L;
	private JTabbedPane tabbedPane;
	private JPanel panelLoc;
	private JPanel panelRecon;
	private JCheckBox chckbxPreprocessing;
	private JCheckBox checkBoxPeakDet;
	private JCheckBox chckbxROI;
	private JLabel lblSkipFrames;
	private JSpinner spinnerSkipFrames;
	private JButton btnSave;
	private JPanel panelMiddle;
	private JButton btnProcess;
	private NMSDetectorFactory detectorFactory;
	private JLabel lblFitter;
	private FitterProvider fitterProvider;
	private JComboBox comboBoxFitter;
	private JCheckBox chkboxRenderer;
	private JCheckBox chkboxFilter;
	private boolean checkPreprocessing;
	private FitterFactory fitterFactory;
	private HistogramRendererFactory rendererFactory;
	private JLabel lblFile;
	private Manager manager;
	private ExtendableTable table;
	private Map<String,Object> settings;
	private StackWindow previewerWindow;
	private AbstractModule detector;
	private Fitter<T,F> fitter;
	protected ContrastAdjuster contrastAdjuster;
	private Renderer renderer;
	private FrameElements<T> detResults;
	private List<Element> fitResults;
	private ImageWindow rendererWindow;
	protected int widgetSelection = 0;
	private boolean processed = false;
	protected ExtendableTable filteredTable = null;
	protected static int DETECTOR = 1;
	protected static int FITTER = 2;
	private JProgressBar progressBar;
	private JButton btnReset;
	private JPanel panelLower;
	private JPanel panelFilter;
	private CardLayout cardsFirst;
	private CardLayout cardsSecond;
	private ImageLoader<T> tif;
	private FastMedianFilterFactory preProcessingFactory;
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
	private Calibrator calibrator;
	private JLabel lblEta;
	private TitledBorder borderFirst;
	private TitledBorder borderSecond;
	private long start;
	private SaveLocalizationPrecision3D saver;

	/**
	 * Create the frame.
	 * @param imp 
	 */
	@SuppressWarnings("serial")
	public Controller() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (previewerWindow != null)
					previewerWindow.close();
				if (contrastAdjuster !=null)
					contrastAdjuster.close();
				if (rendererWindow != null)
					rendererWindow.close();
				dispose();
			}
		});
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			IJ.error(e1.getMessage());
		}
		setTitle("3D Astigmatism Plugin");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 330, 500);
		JPanel contentPane = new JPanel();
		setContentPane(contentPane);

		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] {315, 0};
		gbl_contentPane.rowHeights = new int[] {400, 0, 30, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				saveSettings(panelLower);
				saveSettings(panelFilter);
			}
		});
		tabbedPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		
		panelCalibration = new JPanel();
		tabbedPane.addTab("Calibration", null, panelCalibration, null);
		
		labelStepSize = new JLabel("Step Size");
		labelStepSize.setEnabled(false);
		
		buttonFitBeads = new JButton("Fit beads");
		buttonFitBeads.setToolTipText("Gaussian fitting of beads");
		buttonFitBeads.setEnabled(false);
		buttonFitBeads.addActionListener(this);
		
		labelRange = new JLabel("Range");
		labelRange.setEnabled(false);
		
		buttonFitCurve = new JButton("Fit curve");
		buttonFitCurve.setToolTipText("Fit the two sigmas to obtain the calibration curve");
		buttonFitCurve.addActionListener(this);
		buttonFitCurve.setEnabled(false);
		
		buttonSaveCal = new JButton("Save Calibration");
		buttonSaveCal.setToolTipText("Save Calibration for later use");
		buttonSaveCal.addActionListener(this);
		buttonSaveCal.setEnabled(false);
		
		rangeSlider = new RangeSlider();
		rangeSlider.setToolTipText("Adjust slider to constraint the data range");
		rangeSlider.setValue(0);
		rangeSlider.setUpperValue(100);
		rangeSlider.setPaintTicks(true);
		rangeSlider.setPaintLabels(true);
		rangeSlider.setMinorTickSpacing(50);
		rangeSlider.setMaximum(400);
		rangeSlider.setMajorTickSpacing(200);
		rangeSlider.setEnabled(false);
		
		spinnerStepSize = new JSpinner();
		spinnerStepSize.setToolTipText("Step size for calibration data");
		spinnerStepSize.setEnabled(false);
		spinnerStepSize.setModel(new SpinnerNumberModel(new Integer(10), new Integer(0), null, new Integer(1)));
		
		btnImagesCalibration = new JButton("Calibration Images");
		btnImagesCalibration.setToolTipText("Load Images for 3D calibration");
		btnImagesCalibration.addActionListener(this);
		GroupLayout gl_panelCalibration = new GroupLayout(panelCalibration);
		gl_panelCalibration.setHorizontalGroup(
			gl_panelCalibration.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelCalibration.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelCalibration.createParallelGroup(Alignment.LEADING)
						.addComponent(buttonSaveCal)
						.addComponent(buttonFitCurve)
						.addComponent(rangeSlider, GroupLayout.PREFERRED_SIZE, 289, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_panelCalibration.createSequentialGroup()
							.addGap(9)
							.addComponent(labelRange))
						.addGroup(gl_panelCalibration.createSequentialGroup()
							.addGap(8)
							.addComponent(labelStepSize, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(spinnerStepSize, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE))
						.addComponent(buttonFitBeads)
						.addComponent(btnImagesCalibration))
					.addContainerGap(14, Short.MAX_VALUE))
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
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(rangeSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(buttonFitCurve)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(buttonSaveCal)
					.addContainerGap(122, Short.MAX_VALUE))
		);
		panelCalibration.setLayout(gl_panelCalibration);
		
		panelLoc = new JPanel();
		panelLoc.setBorder(UIManager.getBorder("List.focusCellHighlightBorder"));
		tabbedPane.addTab("Localize", null, panelLoc, null);
		GridBagLayout gbl_panelLoc = new GridBagLayout();
		gbl_panelLoc.columnWidths = new int[] {250};
		gbl_panelLoc.rowHeights = new int[] {135, 22, 205};
		gbl_panelLoc.columnWeights = new double[]{1.0};
		gbl_panelLoc.rowWeights = new double[]{1.0, 0.0, 0.0};
		panelLoc.setLayout(gbl_panelLoc);
		
		JPanel panelUpper = new JPanel();
		
		chckbxPreprocessing = new JCheckBox("Background Substraction");
		chckbxPreprocessing.setToolTipText("Temporal median filter for noise removal");
		chckbxPreprocessing.addActionListener(this);
		
		checkBoxPeakDet = new JCheckBox("Peak Detection");
		checkBoxPeakDet.setToolTipText("Detection of localizations for fitting");
		checkBoxPeakDet.addActionListener(this);
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
		
		JLabel lblDataSource = new JLabel("Data source");
		GroupLayout gl_panelUpper = new GroupLayout(panelUpper);
		gl_panelUpper.setHorizontalGroup(
			gl_panelUpper.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelUpper.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelUpper.createParallelGroup(Alignment.LEADING)
						.addComponent(chckbxPreprocessing)
						.addGroup(gl_panelUpper.createSequentialGroup()
							.addGroup(gl_panelUpper.createParallelGroup(Alignment.LEADING)
								.addComponent(checkBoxPeakDet)
								.addComponent(lblFitter))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(comboBoxFitter, 0, 199, Short.MAX_VALUE))
						.addGroup(gl_panelUpper.createSequentialGroup()
							.addComponent(lblDataSource)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(lblFile, GroupLayout.PREFERRED_SIZE, 187, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap())
		);
		gl_panelUpper.setVerticalGroup(
			gl_panelUpper.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelUpper.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelUpper.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblDataSource)
						.addComponent(lblFile))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(chckbxPreprocessing, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(checkBoxPeakDet, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panelUpper.createParallelGroup(Alignment.BASELINE)
						.addComponent(comboBoxFitter, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblFitter))
					.addGap(21))
		);
		panelUpper.setLayout(gl_panelUpper);
		
		panelMiddle = new JPanel();
		GridBagConstraints gbc_panelMiddle = new GridBagConstraints();
		gbc_panelMiddle.fill = GridBagConstraints.VERTICAL;
		gbc_panelMiddle.insets = new Insets(0, 0, 0, 0);
		gbc_panelMiddle.gridx = 0;
		gbc_panelMiddle.gridy = 1;
		panelLoc.add(panelMiddle, gbc_panelMiddle);
		
		chckbxROI = new JCheckBox("use ROI");
		chckbxROI.setToolTipText("use only that region for further processing");
		chckbxROI.addActionListener(this);
		
		lblSkipFrames = new JLabel("Skip frames");
		
		spinnerSkipFrames = new JSpinner();
		spinnerSkipFrames.setToolTipText("skip frames at the beginning of the image stack");
		spinnerSkipFrames.setPreferredSize(new Dimension(40, 28));
		spinnerSkipFrames.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
		GroupLayout gl_panelMiddle = new GroupLayout(panelMiddle);
		gl_panelMiddle.setHorizontalGroup(
			gl_panelMiddle.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelMiddle.createSequentialGroup()
					.addContainerGap()
					.addComponent(chckbxROI)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(lblSkipFrames)
					.addGap(18)
					.addComponent(spinnerSkipFrames, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		gl_panelMiddle.setVerticalGroup(
			gl_panelMiddle.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelMiddle.createParallelGroup(Alignment.BASELINE)
					.addComponent(spinnerSkipFrames, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(lblSkipFrames)
					.addComponent(chckbxROI))
		);
		panelMiddle.setLayout(gl_panelMiddle);
		
		panelLower = new JPanel();
		borderFirst = new TitledBorder(new LineBorder(new Color(0, 0, 205)), "none", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 205));
		panelLower.setBorder(borderFirst);
		GridBagConstraints gbc_panelLower = new GridBagConstraints();
		gbc_panelLower.fill = GridBagConstraints.BOTH;
		gbc_panelLower.gridx = 0;
		gbc_panelLower.gridy = 2;
		panelLoc.add(panelLower, gbc_panelLower);
		cardsFirst=new CardLayout(0, 0);
		panelLower.setLayout(cardsFirst);
		
		ConfigurationPanel panelFirst = new ConfigurationPanel(){
			@Override
			public void setSettings(Map<String, Object> settings) {
			}

			@Override
			public Map<String, Object> getSettings() {
				return null;
			}
		};
		panelLower.add(panelFirst, "FIRST");
		
		panelRecon = new JPanel();
		panelRecon.setBorder(UIManager.getBorder("List.focusCellHighlightBorder"));
		tabbedPane.addTab("Reconstruct", null, panelRecon, null);
		GridBagLayout gbl_panelRecon = new GridBagLayout();
		gbl_panelRecon.columnWidths = new int[] {300};
		gbl_panelRecon.rowHeights = new int[] {65, 280};
		gbl_panelRecon.columnWeights = new double[]{1.0};
		gbl_panelRecon.rowWeights = new double[]{0.0, 1.0};
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
		btnReset.addActionListener(this);
		GroupLayout gl_panelRenderer = new GroupLayout(panelRenderer);
		gl_panelRenderer.setHorizontalGroup(
			gl_panelRenderer.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelRenderer.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelRenderer.createParallelGroup(Alignment.TRAILING)
						.addComponent(chkboxRenderer, Alignment.LEADING)
						.addGroup(Alignment.LEADING, gl_panelRenderer.createSequentialGroup()
							.addComponent(chkboxFilter)
							.addPreferredGap(ComponentPlacement.RELATED, 159, Short.MAX_VALUE)
							.addComponent(btnReset))))
		);
		gl_panelRenderer.setVerticalGroup(
			gl_panelRenderer.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelRenderer.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelRenderer.createParallelGroup(Alignment.TRAILING)
						.addComponent(btnReset)
						.addGroup(gl_panelRenderer.createSequentialGroup()
							.addComponent(chkboxRenderer)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(chkboxFilter)))
					.addContainerGap(6, Short.MAX_VALUE))
		);
		panelRenderer.setLayout(gl_panelRenderer);
		
		panelFilter = new JPanel();
		borderSecond = new TitledBorder(new LineBorder(new Color(0, 0, 205)), "none", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 205));
		panelFilter.setBorder(borderSecond);
		GridBagConstraints gbc_panelFilter = new GridBagConstraints();
		gbc_panelFilter.fill = GridBagConstraints.BOTH;
		gbc_panelFilter.gridx = 0;
		gbc_panelFilter.gridy = 1;
		panelRecon.add(panelFilter, gbc_panelFilter);
		cardsSecond = new CardLayout(0, 0);
		panelFilter.setLayout(cardsSecond);
		
		ConfigurationPanel panelSecond = new ConfigurationPanel(){
			@Override
			public void setSettings(Map<String, Object> settings) {
			}

			@Override
			public Map<String, Object> getSettings() {
				return null;
			}
		};
		panelFilter.add(panelSecond, "SECOND");
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
		btnProcess.setToolTipText("Process all images");
		btnProcess.addActionListener(this);		
		
		btnSave = new JButton("Save");
		btnSave.setToolTipText("Save localizations in ViSP format");
		btnSave.addActionListener(this);
		
		JPanel panelProgress = new JPanel();
		GridBagConstraints gbc_panelProgress = new GridBagConstraints();
		gbc_panelProgress.fill = GridBagConstraints.BOTH;
		gbc_panelProgress.gridx = 0;
		gbc_panelProgress.gridy = 1;
		contentPane.add(panelProgress, gbc_panelProgress);
		GridBagLayout gbl_panelProgress = new GridBagLayout();
		gbl_panelProgress.columnWidths = new int[]{315, 0, 0};
		gbl_panelProgress.rowHeights = new int[]{0, 0};
		gbl_panelProgress.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_panelProgress.rowWeights = new double[]{0.0, Double.MIN_VALUE};
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
		panelButtons.add(btnSave);
		panelButtons.add(btnProcess);
		GridBagConstraints gbc_panelButtons = new GridBagConstraints();
		gbc_panelButtons.anchor = GridBagConstraints.NORTH;
		gbc_panelButtons.gridx = 0;
		gbc_panelButtons.gridy = 2;
		contentPane.add(panelButtons, gbc_panelButtons);
		init();
	}
	
	private void init(){
		settings = new HashMap<String, Object>();
		manager = new Manager();
		manager.addPropertyChangeListener(new PropertyChangeListener(){
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("progress")){
					int value = (Integer) evt.getNewValue();
					progressBar.setValue(value);
					long current = System.currentTimeMillis();
					long eta = 0;
					if (value > 0)
						eta = Math.round((current-start)/value/1000);
					lblEta.setText(String.valueOf(eta)+"sec");
					start = current;
				}
			}});
		createProvider();
		cardsFirst.show(panelLower, "FIRST");
		cardsSecond.show(panelFilter, "SECOND");
		
		ImagePlus loc_im = null;
		try{
			previewerWindow = (StackWindow) WindowManager.getCurrentWindow();
			loc_im = previewerWindow.getImagePlus();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		if(loc_im == null){ 
			loc_im = loadImages();
			if (loc_im == null){
				setVisible(false);
				return;
			}
			previewerWindow = new StackWindow(loc_im, loc_im.getCanvas());
		}
		
	
		
		tif = new ImageLoader<T>(loc_im);
	    manager.add(tif);
	    
	    previewerWindow.addKeyListener(new KeyAdapter(){	
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == 'C'){
				contrastAdjuster = new ContrastAdjuster();
				contrastAdjuster.run("B&C");
				}
			}
	    });
	    
		lblFile.setText(previewerWindow.getTitle());
		validate();
	}

////Overrides
	@Override
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		
		if (s == this.btnImagesCalibration){
			if(importImages()){
				this.labelStepSize.setEnabled(true);
				this.spinnerStepSize.setEnabled(true);
				this.buttonFitBeads.setEnabled(true);
			}
		}
		
		if (s == this.buttonFitBeads){
			if(fitbeads()){
				this.labelRange.setEnabled(true);
				this.rangeSlider.setEnabled(true);
				this.buttonFitCurve.setEnabled(true);
			}
		}
		
		if (s == this.buttonFitCurve){
			if(fitCurve()){
				this.buttonSaveCal.setEnabled(true);
			}
		}
		
		if (s == this.buttonSaveCal){
			saveCalibration();
		}
		
		if (s == this.chckbxROI){
			setRoi();
		}
		
		if (s == this.checkBoxPeakDet)
			if(checkBoxPeakDet.isSelected())
				chooseDetector();
			else
				this.cardsFirst.show(panelLower, "FIRST");
		
		if (s == this.comboBoxFitter){
			chooseFitter();
		}
		
		if (s == this.chkboxFilter){
			if (this.chkboxFilter.isSelected())
				filterTable();
			validate();
		}
		
		if (s == this.btnReset){ 
			if (rendererFactory!=null){
				Map<String, Object> initialMap = rendererFactory.getInitialSettings();
				rendererShow(initialMap);
			}
		}
		
		if (s== this.chkboxRenderer ){
			if (chkboxRenderer.isSelected())
				chooseRenderer();
			else
				this.cardsSecond.show(panelLower, "SECOND");
		}
		
		if (s == this.chckbxPreprocessing){
			if (chckbxPreprocessing.isSelected())
				choosePP();
			else
				this.cardsFirst.show(panelLower, "FIRST");
		}
		
		if (s == this.btnProcess){ 			
			process(true);		
		}
		
		if (s == this.btnSave){
			saveLocalizations();
		}
	}

	private void process(boolean b) {
		// Manager

		if (tif == null) {
			IJ.error("Please load images first!");
			return;
		}

		if (detector == null) {
			IJ.error("Please choose detector first!");
			return;
		}
		manager.add(detector);

		ImageMath<T,F> math = null;
		if (checkPreprocessing) {
			AbstractModule pp = preProcessingFactory.getModule();
			manager.add(pp);
			manager.linkModules(tif, pp, true);
			math = new ImageMath<T, F>();
			math.setOperator(operators.SUBSTRACTION);
			manager.add(math);
			manager.linkModules(tif, math, true);
			manager.linkModules(pp, math);
			manager.linkModules(math, detector);
		} else {
			manager.linkModules(tif, detector, true);
		}
		
		if (fitter != null) {
			manager.add(fitter);
			manager.linkModules(detector, fitter);
		}
		
		if (saver == null) {
			saveLocalizations();
		}
		
		if (b) {
			if (renderer != null) {
				manager.add(renderer);
				manager.linkModules(fitter, renderer, false);
			}
			start = System.currentTimeMillis();
			manager.execute();
			processed = true;
		}
	}

	
	//// Private Methods
	private ImagePlus loadImages() {
		manager.reset();

		JFileChooser fc = new JFileChooser(System.getProperty("user.home") + "/ownCloud/storm");
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setDialogTitle("Import Images");

		int returnVal = fc.showOpenDialog(this);

		if (returnVal != JFileChooser.APPROVE_OPTION)
			return null;

		File file = fc.getSelectedFile();

		ImagePlus loc_im = null;
		if (file.isDirectory()) {
			FolderOpener fo = new FolderOpener();
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
		ImagePlus curImage = previewerWindow.getImagePlus();
		if (chckbxROI.isSelected()){
			Roi roi = curImage.getRoi();
			if (roi==null){
				Rectangle r = curImage.getProcessor().getRoi();
				int iWidth = r.width / 2;
	            int iHeight = r.height / 2;
	            int iXROI = r.x + r.width / 4;
	            int iYROI = r.y + r.height / 4;
	            curImage.setRoi(iXROI, iYROI, iWidth, iHeight);
			}
		} else {
			curImage.killRoi();
		}
	}
	
	private boolean importImages() {
		JFileChooser fc = new JFileChooser(System.getProperty("user.home"));
		fc.setLocation(getLocation());
    	fc.setDialogTitle("Import Calibration Images");
    	fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    	
    	int returnVal = fc.showOpenDialog(this);
    	 
        if (returnVal != JFileChooser.APPROVE_OPTION)
        	return false;
        
        File file = fc.getSelectedFile();
        
        ImagePlus calibImage = new ImagePlus();
		if (file.isDirectory()){
        	calibImage = FolderOpener.open(file.getAbsolutePath());
        }
        
        if (file.isFile()){
        	calibImage = new ImagePlus(file.getAbsolutePath());
        }
        
        calibWindow = new StackWindow(calibImage);
        calibImage.setRoi(calibImage.getWidth()/2 - 10, calibImage.getHeight()/2 - 10, 20, 20);	
        return calibWindow!=null;
	}
	
	private boolean fitbeads() {
		Roi roitemp = calibWindow.getImagePlus().getRoi();
		Roi calibRoi = null;
		try{																				
			double w = roitemp.getFloatWidth();
			double h = roitemp.getFloatHeight();
			if (w!=h) {
				IJ.showMessage("Needs a quadratic ROI /n(hint: press Shift).");
				return false;
			}
			calibRoi = roitemp;
		} catch (NullPointerException e) {
			calibRoi = new Roi(0, 0, calibWindow.getImagePlus().getWidth(), calibWindow.getImagePlus().getHeight());
		} 
		
		int zstep = (Integer) this.spinnerStepSize.getValue();
		calibrator = new Calibrator(calibWindow.getImagePlus(), zstep, calibRoi);	
		calibrator.fitStack();
		double[] zgrid = calibrator.getCalibration().getZgrid();
		Arrays.sort(zgrid);
		this.rangeSlider.setMinimum((int) zgrid[0]);
		this.rangeSlider.setMaximum((int) zgrid[zgrid.length-1]);
		this.rangeSlider.setValue((int) zgrid[0]);
		this.rangeSlider.setUpperValue((int) zgrid[zgrid.length-1]);

		calibWindow.close();
		return true;
	}
	
	private boolean fitCurve() {
		int rangeMin = this.rangeSlider.getValue();
		int rangeMax = this.rangeSlider.getUpperValue();
		calibrator.fitCalibrationCurve(rangeMin, rangeMax);
		return true;
	}
	
	private void saveCalibration() {
		JFileChooser fc = new JFileChooser(System.getProperty("user.home"));
		fc.setLocation(getLocation());
    	fc.setDialogTitle("Save calibration");   
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Csv files", "csv");
        fc.setFileFilter(filter);
    	 
    	if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
    	    File calibFile = fc.getSelectedFile();
    	    calibrator.saveCalib(calibFile.getAbsolutePath());
    	    settings.put(FitterPanel.KEY_CALIBRATION_FILENAME, calibFile);
    	}		
    	calibrator.getCalibration().closePlotWindows();
    	setVisible(false);
	}
	
	private void choosePP() {
		if (tif == null) {
			if (previewerWindow != null)
				previewerWindow.getImagePlus().killRoi();
			return;
		}
		preProcessingFactory = new FastMedianFilterFactory();
		borderFirst.setTitle(preProcessingFactory.getName());
		cardsFirst.show(panelLower, FastMedianFilterFactory.KEY);
		ConfigurationPanel panelDown = preProcessingFactory.getConfigurationPanel();
		
		panelDown.addPropertyChangeListener(ConfigurationPanel.propertyName, new PropertyChangeListener(){

			@SuppressWarnings("unchecked")
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				Map<String, Object> value = (Map<String, Object>) evt.getNewValue();
				ppPreview(value);
			}
		});
		ppPreview(panelDown.getSettings());
	}

	
	@SuppressWarnings("unchecked")
	private void ppPreview(Map<String, Object> map) {
		preProcessingFactory.setAndCheckSettings(map);
		AbstractModule preProcessor = preProcessingFactory.getModule();
		int frameNumber = previewerWindow.getImagePlus().getSlice();
		List<Element> list = new ArrayList<Element>();
		ImageStack stack = previewerWindow.getImagePlus().getImageStack();
		int stackSize = stack.getSize();
		double pixelSize = previewerWindow.getImagePlus().getCalibration().pixelDepth;
		
		for (int i = frameNumber; i < frameNumber + preProcessingFactory.processingFrames(); i++){
			Object ip = stack.getPixels(i);
			Img<T> curImage = LemmingUtils.wrap(ip, new long[]{stack.getWidth(), stack.getHeight()});
			Frame<T> curFrame = new ImgLib2Frame<T>(frameNumber, (int)curImage.dimension(0), (int)curImage.dimension(1), pixelSize, curImage);
			list.add(curFrame);
		}
		
		FastStore ppResults = new FastStore();;
		if (!list.isEmpty()){
			FastStore previewStore = new FastStore();
	 		Element last = list.remove(list.size()-1);
			for (Element entry : list)	
				previewStore.put(entry);
			last.setLast(true);
			previewStore.put(last);
			preProcessor.setInput(previewStore);
			preProcessor.setOutput(ppResults);
			Thread preProcessingThread = new Thread(preProcessor,preProcessor.getClass().getSimpleName());
			preProcessingThread.start();
			try {
				preProcessingThread.join();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}		
		if(ppResults.isEmpty()) return;
		
		for (int i = frameNumber; i < frameNumber + preProcessingFactory.processingFrames(); i++){
			Frame<T> resFrame = (Frame<T>) ppResults.get();
			if (resFrame == null) continue;
			ImageProcessor ip = ImageJFunctions.wrap(resFrame.getPixels(), "").getProcessor();
			if (i < stackSize){
				ImageProcessor stackip  = stack.getProcessor(i);
				stackip.setPixels(ip.getPixels());
			}
		}
		previewerWindow.repaint();
	}
	
	private static ConfigurationPanel getConfigSettings(JPanel cardPanel){
		ConfigurationPanel s = null;
		for (Component comp : cardPanel.getComponents()) {
	           if (comp.isVisible()) 
	                s = ((ConfigurationPanel) comp);
		}
		return s;
	}

	private void saveSettings(JPanel cardPanel){
	if (cardPanel == null) return;
	for (Component comp : cardPanel.getComponents()) {
		Map<String, Object> s = ((ConfigurationPanel) comp).getSettings();
		if (s!=null)
			for (String key : s.keySet())
				settings.put(key, s.get(key));
			}	
	}
	
	private void chooseDetector() {
		widgetSelection = DETECTOR;
		
		borderFirst.setTitle(detectorFactory.getName());
		cardsFirst.show(panelLower, detectorFactory.getKey());
		ConfigurationPanel panelDown = getConfigSettings(panelLower);
		panelDown.addPropertyChangeListener(ConfigurationPanel.propertyName, new PropertyChangeListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				Map<String, Object> value = (Map<String, Object>) evt.getNewValue();
				detectorPreview(value);
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
				if (ip == previewerWindow.getImagePlus() && widgetSelection == DETECTOR)
					detectorPreview(getConfigSettings(panelLower).getSettings());
				if (ip == previewerWindow.getImagePlus() && widgetSelection == FITTER)
					fitterPreview(getConfigSettings(panelLower).getSettings());
			}
		});
		repaint();
		detectorPreview(panelDown.getSettings());
	}
	
	@SuppressWarnings("unchecked")
	private void detectorPreview(Map<String, Object> map){
		detectorFactory.setAndCheckSettings(map);
		detector = detectorFactory.getDetector();
		int frameNumber = previewerWindow.getImagePlus().getSlice();
		double pixelSize = previewerWindow.getImagePlus().getCalibration().pixelDepth;
		ImageStack stack = previewerWindow.getImagePlus().getStack();
		Object ip = stack.getPixels(frameNumber);
		Img<T> curImage = LemmingUtils.wrap(ip, new long[]{stack.getWidth(), stack.getHeight()});
		ImgLib2Frame<T> curFrame = new ImgLib2Frame<T>(frameNumber, (int)curImage.dimension(0), (int)curImage.dimension(1), pixelSize, curImage);
		
		detResults = (FrameElements<T>) detector.preview(curFrame);
		FloatPolygon points = LemmingUtils.convertToPoints(detResults.getList(), pixelSize);
		PointRoi roi = new PointRoi(points);
		ImagePlus plus = previewerWindow.getImagePlus();
		plus.setRoi(roi);
	}
	
	private void chooseFitter() {
		final int index = comboBoxFitter.getSelectedIndex() - 1;
		if (index<0 || tif == null || detector == null){ 
			fitter = null;
			return;
		}
		widgetSelection = FITTER;
		
		final String key = fitterProvider.getVisibleKeys().get( index );
		fitterFactory = fitterProvider.getFactory(key);
		borderFirst.setTitle(fitterFactory.getName());
		cardsFirst.show(panelLower, key);
		validate();
		System.out.println("AstigPlugin: Fitter_"+index+" : "+key);
		ConfigurationPanel panelDown = getConfigSettings(panelLower);
		panelDown.addPropertyChangeListener(ConfigurationPanel.propertyName, new PropertyChangeListener(){
			@SuppressWarnings("unchecked")
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				Map<String, Object> value = (Map<String, Object>) evt.getNewValue();
				fitterPreview(value);
			}
		});
		
		Map<String, Object> fitterSettings = panelDown.getSettings();
		
		if (fitterSettings != null){ 
			Object calibFile = settings.get(FitterPanel.KEY_CALIBRATION_FILENAME);
			if (calibFile != null)
				fitterSettings.put(FitterPanel.KEY_CALIBRATION_FILENAME, calibFile);
			fitterPreview(fitterSettings);
		}
		repaint();
	}
	
	private void fitterPreview(Map<String, Object> map){
		if (!fitterFactory.setAndCheckSettings(map))
			return;
		fitter = fitterFactory.getFitter();
		
		previewerWindow.getImagePlus().killRoi();
		int frameNumber = previewerWindow.getImagePlus().getSlice();
		double pixelSize = previewerWindow.getImagePlus().getCalibration().pixelDepth;
		ImageStack stack = previewerWindow.getImagePlus().getStack();
		Object ip = stack.getPixels(frameNumber);
		Img<T> curImage = LemmingUtils.wrap(ip, new long[]{stack.getWidth(), stack.getHeight()});	
		ImgLib2Frame<T> curFrame = new ImgLib2Frame<T>(frameNumber, (int)curImage.dimension(0), (int)curImage.dimension(1), pixelSize, curImage);
		
		fitResults = fitter.fit(detResults.getList(), curFrame.getPixels(), fitter.getWindowSize(), frameNumber, pixelSize);
		FloatPolygon points = LemmingUtils.convertToPoints(fitResults, pixelSize);
		PointRoi roi = new PointRoi(points);
		previewerWindow.getImagePlus().setRoi(roi);
		previewerWindow.repaint();
	}
	
	private void chooseRenderer() { 
		borderSecond.setTitle(fitterFactory.getName());
		cardsSecond.show(panelFilter, rendererFactory.getKey());
		ConfigurationPanel panelReconDown = getConfigSettings(panelFilter);
		panelReconDown.addPropertyChangeListener(ConfigurationPanel.propertyName, new PropertyChangeListener(){

			@SuppressWarnings("unchecked")
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				Map<String, Object> map = (Map<String, Object>) evt.getNewValue();
				rendererPreview(map);
			}
		});
		
		Set<String> rendererSettings = panelReconDown.getSettings().keySet();
		for (String key : rendererSettings)
			settings.put(key, panelReconDown.getSettings().get(key));
		rendererFactory.setAndCheckSettings(settings);
		renderer = rendererFactory.getRenderer();
		
		if(rendererWindow == null){
			rendererWindow = new ImageWindow(renderer.getImage()); // set a new Renderer
			rendererWindow.addKeyListener(new KeyAdapter(){
		
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyChar() == 'C'){
						contrastAdjuster = new ContrastAdjuster();
						contrastAdjuster.run("B&C");
					}
				}
			});
		}
		repaint();
		
		if (processed)
			rendererShow(settings);
		else
			rendererPreview(settings);
	}
	
	private void rendererPreview(Map<String, Object> map){
		rendererFactory.setAndCheckSettings(map);
		
		rendererWindow.getCanvas().fitToWindow();
		if (!rendererWindow.isVisible())
			rendererWindow.setVisible(true);
		
		List<Element> list = fitResults;
		if (list != null && !list.isEmpty()){
			renderer.preview(list);
		}
	}
	
	private void rendererShow(Map<String, Object> map){	
		rendererFactory.setAndCheckSettings(map);
		renderer = rendererFactory.getRenderer();
		rendererWindow.setImage(renderer.getImage());
		rendererWindow.getCanvas().fitToWindow();
		ExtendableTable tableToRender = filteredTable == null ? table : filteredTable;	
		
		rendererWindow.getCanvas().addMouseListener(new MouseAdapter(){   // calculate new settings for renderer
			@Override
		    public void mouseClicked(MouseEvent e){
		        if(e.getClickCount()==2){
		        	try{
		        		Rectangle rect = renderer.getImage().getRoi().getBounds();
		        	
			        	final double xmin = (Double) settings.get(HistogramRendererFactory.KEY_xmin);
						final double xmax = (Double) settings.get(HistogramRendererFactory.KEY_xmax);
						final double ymin = (Double) settings.get(HistogramRendererFactory.KEY_ymin);
						final double ymax = (Double) settings.get(HistogramRendererFactory.KEY_ymax);
						final int xbins = (Integer) settings.get(HistogramRendererFactory.KEY_xBins);
						final int ybins = (Integer) settings.get(HistogramRendererFactory.KEY_yBins);
						
						double new_xmin = (xmax-xmin)*rect.getMinX()/xbins+xmin;
						double new_ymin = (ymax-ymin)*rect.getMinY()/ybins+ymin;
						double new_xmax = (xmax-xmin)*rect.getMaxX()/xbins+xmin;
						double new_ymax = (ymax-ymin)*rect.getMaxY()/ybins+ymin;
						double factx = rect.getWidth()/rect.getHeight();
						double facty = rect.getHeight()/rect.getWidth();
						double ar = Math.min(factx, facty);
						int new_xbins =  (int)(Math.round(xbins*ar));
						int new_ybins =  (int)(Math.round(ybins*ar));
						
			        	settings.put(HistogramRendererFactory.KEY_xmin,new_xmin);
			        	settings.put(HistogramRendererFactory.KEY_ymin,new_ymin);
			        	settings.put(HistogramRendererFactory.KEY_xmax,new_xmax);
			        	settings.put(HistogramRendererFactory.KEY_ymax,new_ymax);
			        	settings.put(HistogramRendererFactory.KEY_xBins,new_xbins);
			        	settings.put(HistogramRendererFactory.KEY_yBins,new_ybins);
		        	} catch (NullPointerException ne){}
		        	rendererShow(settings);
		        }
			}
		});
		if (tableToRender != null && tableToRender.columnNames().size()>0){
			Store previewStore = tableToRender.getFIFO();
			System.out.println("AstigPlugin: Rendering " + tableToRender.getNumberOfRows() + " elements");
			renderer.setInput(previewStore);
			renderer.run();	
			renderer.resetInputStore();
			rendererWindow.repaint();
		}
	}
	
	private void filterTable() {
		if (!processed){
			if (IJ.showMessageWithCancel("Filter", "Pipeline not yet processed.\nDo you want to process it now?"))
				process(false);
			else
				return;
			if(fitter == null) return;
			DataTable dt = new DataTable();
			manager.add(dt);
			manager.linkModules(fitter, dt);
			start = System.currentTimeMillis();
			manager.execute();
			processed = true;
			table = dt.getTable();
		}
		if (table!=null || !table.columnNames().isEmpty()){
			FilterPanel panelReconDown = new FilterPanel(table);
			cardsSecond.addLayoutComponent(panelReconDown, FilterPanel.KEY);
			borderSecond.setTitle("FILTER");
			cardsSecond.show(panelFilter, "FILTER");
			panelReconDown.addPropertyChangeListener(ConfigurationPanel.propertyName, new PropertyChangeListener(){
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if (table.filtersCollection.isEmpty()){
						filteredTable = null;
					} else {
						filteredTable = table.filter();
					}
					if (renderer != null)
						rendererShow(settings);
				}
			});
			Map<String, Object> curSet = panelReconDown.getSettings();
			if (curSet != null)
				for (String key : curSet.keySet())
					settings.put(key, curSet.get(key));
			if (renderer != null)
				rendererShow(settings);
			validate();
		}		
	}
	
	private void saveLocalizations() {
		if (fitter != null) {
			JFileChooser fc = new JFileChooser(System.getProperty("user.home") + "/ownCloud/storm");
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setDialogTitle("Save Data");

			if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
				return;
			File file = fc.getSelectedFile();
			if (this.chkboxFilter.isSelected()) { // TODO code check
				ExtendableTable tableToProcess = filteredTable == null ? table : filteredTable;
				Store s = tableToProcess.getFIFO();
				StoreSaver tSaver = new StoreSaver(file);
				tSaver.putMetadata(settings);
				tSaver.setInput(s);
				tSaver.run();
			} else {
				saver = new SaveLocalizationPrecision3D(file);
				manager.add(saver);
				manager.linkModules(fitter, saver, false);
			}
		}
	}
	
	private void createProvider(){
		panelLower.add(new FastMedianPanel(), FastMedianFilterFactory.KEY);
		panelLower.add(new NMSDetectorPanel(), NMSDetectorFactory.KEY);
		createFitterProvider();
		panelFilter.add(new FilterPanel(table), FilterPanel.KEY);
		panelFilter.add(new HistogramRendererPanel(), HistogramRendererFactory.KEY);
		preProcessingFactory = new FastMedianFilterFactory();
		detectorFactory = new NMSDetectorFactory();
		rendererFactory = new HistogramRendererFactory();
	}
	
	private void createFitterProvider() {
		fitterProvider = new FitterProvider();
		final List< String > visibleKeys = fitterProvider.getVisibleKeys();
		final List< String > fitterNames = new ArrayList<String>();
		final List< String > infoTexts = new ArrayList<String>();
		fitterNames.add("none");
		for ( final String key : visibleKeys ){
			FitterFactory factory = fitterProvider.getFactory( key );
			fitterNames.add( factory.getName() );
			infoTexts.add( factory.getInfoText() );
			panelLower.add(factory.getConfigurationPanel(),key);
		}
		String[] names = fitterNames.toArray(new String[] {});
		comboBoxFitter.setModel(new DefaultComboBoxModel(names));
		comboBoxFitter.setRenderer(new ToolTipRenderer(infoTexts));
	}
	
	private class ToolTipRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;
		List<String> tooltips;
		
		public ToolTipRenderer(List<String> tooltips){
			 this.tooltips = tooltips;
		}

	    @Override
	    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
	        JComponent comp = (JComponent) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

	        if (0 < index && null != value && null != tooltips) 
	                    list.setToolTipText(tooltips.get(index-1));
	        return comp;
	    }
		
	}
}
