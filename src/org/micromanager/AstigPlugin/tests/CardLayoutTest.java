package org.micromanager.AstigPlugin.tests;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import java.awt.GridBagLayout;

import javax.swing.JPanel;

import java.awt.Component;
import java.awt.GridBagConstraints;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.micromanager.AstigPlugin.gui.ConfigurationPanel;
import org.micromanager.AstigPlugin.gui.HistogramRendererPanel;

import java.awt.CardLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.awt.Dimension;

public class CardLayoutTest extends JFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9057360094673076477L;
	private JButton btnPrevious;
	private JButton btnNext;
	private JButton btnNew;
	private JPanel panelCards;
	private int counter;

	public CardLayoutTest() {
		setMinimumSize(new Dimension(350, 250));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(300, 150));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{238, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		panelCards = new JPanel();
		GridBagConstraints gbc_panelCards = new GridBagConstraints();
		gbc_panelCards.anchor = GridBagConstraints.WEST;
		gbc_panelCards.fill = GridBagConstraints.VERTICAL;
		gbc_panelCards.gridx = 0;
		gbc_panelCards.gridy = 0;
		getContentPane().add(panelCards, gbc_panelCards);
		panelCards.setLayout(new CardLayout(0, 0));
		
		JPanel panelButtons = new JPanel();
		GridBagConstraints gbc_panelButtons = new GridBagConstraints();
		gbc_panelButtons.fill = GridBagConstraints.BOTH;
		gbc_panelButtons.gridx = 1;
		gbc_panelButtons.gridy = 0;
		getContentPane().add(panelButtons, gbc_panelButtons);
		
		btnPrevious = new JButton("Previous");
		btnPrevious.addActionListener(this);
		
		btnNext = new JButton("Next");
		btnNext.addActionListener(this);
		
		btnNew = new JButton("New");
		btnNew.addActionListener(this);
		GroupLayout gl_panelButtons = new GroupLayout(panelButtons);
		gl_panelButtons.setHorizontalGroup(
			gl_panelButtons.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelButtons.createSequentialGroup()
					.addGroup(gl_panelButtons.createParallelGroup(Alignment.TRAILING, false)
						.addGroup(gl_panelButtons.createSequentialGroup()
							.addGap(6)
							.addComponent(btnNew, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addGroup(gl_panelButtons.createSequentialGroup()
							.addContainerGap()
							.addComponent(btnNext, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
					.addGap(17))
				.addGroup(gl_panelButtons.createSequentialGroup()
					.addContainerGap()
					.addComponent(btnPrevious, GroupLayout.PREFERRED_SIZE, 86, Short.MAX_VALUE)
					.addContainerGap())
		);
		gl_panelButtons.setVerticalGroup(
			gl_panelButtons.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelButtons.createSequentialGroup()
					.addGap(29)
					.addComponent(btnPrevious)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnNext)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnNew)
					.addGap(150))
		);
		panelButtons.setLayout(gl_panelButtons);
		final ConfigurationPanel panel1 = new Panel1();
		panel1.setName("panel1");
		panelCards.add(panel1, "FIRST");
		final ConfigurationPanel hrp = new HistogramRendererPanel();
		hrp.addChangeListener( new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent evt) {
				final Map<String, Object> map = hrp.getSettings();
				System.out.println(map.toString());
			}
		});
		hrp.setName("hrp");
		panelCards.add(hrp, "SECOND");
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					Locale.setDefault(new Locale("en", "US")); 
					CardLayoutTest frame = new CardLayoutTest();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		
		if (s==this.btnNext){
			((CardLayout) panelCards.getLayout()).last(panelCards); 
		}
		
		if (s==this.btnPrevious){
			((CardLayout) panelCards.getLayout()).first(panelCards); 
		}
		
		if (s==this.btnNew){
			ConfigurationPanel comp = getConfigSettings(panelCards);
			Map<String, Object> settings = comp.getSettings();
			if (comp.getName() == "panel1"){
				counter++;
				String newString = "new" + String.valueOf(counter);
				settings.put("label", newString);
				comp.setSettings(settings);
			}
			if (comp.getName() == "hrp"){
				settings.put(HistogramRendererPanel.KEY_xmax, 200d);
				settings.put(HistogramRendererPanel.KEY_ymax, 200d);
				comp.setSettings(settings);
			}
		}
	}
	
	private static ConfigurationPanel getConfigSettings(JPanel cardPanel){
		ConfigurationPanel s = null;
		for (Component comp : cardPanel.getComponents()) {
	           if (comp.isVisible()) 
	                s = ((ConfigurationPanel) comp);
		}
		return s;
	}
	
	class Panel1 extends ConfigurationPanel{
		/**
		 * 
		 */
		private static final long serialVersionUID = -8436552847951715349L;
		private JLabel lblLabel1;

		public Panel1(){
			lblLabel1 = new JLabel("Label1");
			add(lblLabel1);
		}

		@Override
		public void setSettings(Map<String, Object> settings) {	
			String txt = (String) settings.get("label");
			lblLabel1.setText(txt);
		}

		@Override
		public Map<String, Object> getSettings() {
			HashMap<String, Object> s = new HashMap<String, Object>();
			s.put("label", lblLabel1.getText());
			return s;
		}
		
	};
}
