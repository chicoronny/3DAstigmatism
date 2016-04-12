package org.micromanager.AstigPlugin.gui;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;

import org.micromanager.AstigPlugin.tools.WaitForKeyListener;

public class HistogramRendererPanel extends ConfigurationPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3031663211936690561L;
	private final ChangeEvent CHANGE_EVENT = new ChangeEvent( this );
	private JTextField textXBins;
	private JTextField textYBins;
	private JLabel lblX;
	private JLabel lblY;
	private JLabel labelX2;
	private JLabel labelY2;
	private double zmin,zmax;

	public HistogramRendererPanel() {
		
		setBorder(null);
		
		lblX = new JLabel(String.format("%.4f",0d));
		
		lblY = new JLabel(String.format("%.4f",0d));
		
		JLabel lblXBins = new JLabel("X Bins");
		
		JLabel lblYBins = new JLabel("Y Bins");
		
		textXBins = new JTextField();
		textXBins.setHorizontalAlignment(SwingConstants.TRAILING);
		textXBins.setText("500");
		textXBins.addKeyListener(new WaitForKeyListener(1000, new Runnable(){
			@Override
			public void run() {
				fireChanged( CHANGE_EVENT );
			}
		}));
		
		textYBins = new JTextField();
		textYBins.setHorizontalAlignment(SwingConstants.TRAILING);
		textYBins.setText("500");
		textYBins.addKeyListener(new WaitForKeyListener(1000, new Runnable(){
			@Override
			public void run() {
				fireChanged( CHANGE_EVENT );
			}
		}));
		
		labelX2 = new JLabel(String.format("%.4f",100d));
		
		labelY2 = new JLabel(String.format("%.4f",100d));
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblYBins)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(textYBins))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblXBins)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(textXBins, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(330, Short.MAX_VALUE))
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblX, GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
							.addGap(29))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblY, GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
						.addComponent(labelY2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(labelX2, GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE))
					.addGap(314))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(labelX2)
						.addComponent(lblX))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(labelY2)
						.addComponent(lblY))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblXBins)
						.addComponent(textXBins, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(textYBins, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblYBins))
					.addContainerGap(182, Short.MAX_VALUE))
		);
		setLayout(groupLayout);
		zmin=0;
		zmax=255;
	}

	@Override
	public void setSettings(Map<String, Object> settings_) {
		lblX.setText(String.format("%.4f",settings_.get(PanelKeys.KEY_xmin)));
		lblY.setText(String.format("%.4f",settings_.get(PanelKeys.KEY_ymin)));
		labelX2.setText(String.format("%.4f",settings_.get(PanelKeys.KEY_xmax)));
		labelY2.setText(String.format("%.4f",settings_.get(PanelKeys.KEY_ymax)));
		textXBins.setText(String.valueOf(settings_.get(PanelKeys.KEY_xBins)));
		textYBins.setText(String.valueOf(settings_.get(PanelKeys.KEY_yBins)));
		zmin = (Double) settings_.get(PanelKeys.KEY_zmin);
		zmax = (Double) settings_.get(PanelKeys.KEY_zmax);
	}

	@Override
	public Map<String, Object> getSettings() {
		final Map<String, Object> settings = new HashMap<String, Object>(8);
		settings.put(PanelKeys.KEY_xmin, Double.parseDouble(lblX.getText()));
		settings.put(PanelKeys.KEY_ymin, Double.parseDouble(lblY.getText()));
		settings.put(PanelKeys.KEY_xmax, Double.parseDouble(labelX2.getText()));
		settings.put(PanelKeys.KEY_ymax, Double.parseDouble(labelY2.getText()));
		settings.put(PanelKeys.KEY_xBins, Integer.parseInt(textXBins.getText()));
		settings.put(PanelKeys.KEY_yBins, Integer.parseInt(textYBins.getText()));
		settings.put(PanelKeys.KEY_zmin, zmin);
		settings.put(PanelKeys.KEY_zmax, zmax);
		return settings;
	}
	
	public Map<String, Object> getInitialSettings(){
		final Map<String, Object> settings = new HashMap<String, Object>(8);
		settings.put(PanelKeys.KEY_xmin, new Double(0));
		settings.put(PanelKeys.KEY_ymin, new Double(0));
		settings.put(PanelKeys.KEY_xmax, new Double(100));
		settings.put(PanelKeys.KEY_ymax, new Double(100));
		settings.put(PanelKeys.KEY_xBins,new Integer(500));
		settings.put(PanelKeys.KEY_yBins,new Integer(500));
		settings.put(PanelKeys.KEY_zmin,new Double(0));
		settings.put(PanelKeys.KEY_zmax,new Double(255));
		return settings;
	}
	/**
	 * Display this JPanel inside a new JFrame.
	 */
	public static void main( final String[] args )
	{
		// Create GUI
		final Locale usLocale = new Locale("en", "US"); // setting us locale
		Locale.setDefault(usLocale);
		final ConfigurationPanel tp = new HistogramRendererPanel( );
		final JFrame frame = new JFrame();
		frame.getContentPane().add( tp );
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		frame.pack();
		frame.setVisible( true );
		Map<String, Object> cur = tp.getSettings();
		cur.put(PanelKeys.KEY_xmax, 200d);
		tp.setSettings(cur);
	}

}
