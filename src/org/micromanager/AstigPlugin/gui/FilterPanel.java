package org.micromanager.AstigPlugin.gui;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.micromanager.AstigPlugin.pipeline.ExtendableTable;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;

public class FilterPanel extends ConfigurationPanel implements ChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2042228716255813527L;
	public static final String KEY = "FILTER";
	
	private JButton btnAdd;
	private JButton btnRemove;
	private JScrollPane scrollPane;
	private ExtendableTable table;
	private Deque<HistogramPanel> panelStack = new ArrayDeque<HistogramPanel>();
	private final ChangeEvent CHANGE_EVENT = new ChangeEvent( this );
	private JPanel panelHolder;
	private JPanel panelButtons;
	
	public FilterPanel(ExtendableTable table) {
		setBorder(null);
		setMinimumSize(new Dimension(295, 315));
		setPreferredSize(new Dimension(300, 340));
		setName(KEY);
		this.table = table;
		
		scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(290, 300));
		scrollPane.setOpaque(true);
		scrollPane.setAutoscrolls(true);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setEnabled(true);
		
		panelHolder = new JPanel();
		scrollPane.setViewportView(panelHolder);
		panelHolder.setLayout(new BoxLayout(panelHolder, BoxLayout.Y_AXIS));
		
		panelButtons = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panelButtons.getLayout();
		flowLayout.setHgap(0);
		
		btnAdd = new JButton("Add");
		panelButtons.add(btnAdd);
		btnAdd.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed( final ActionEvent e ){
				addPanel();
			}
		});
		btnAdd.setAlignmentY(Component.TOP_ALIGNMENT);
		
		btnRemove = new JButton("Remove");
		panelButtons.add(btnRemove);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(scrollPane);
		add(panelButtons);
		btnRemove.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed( final ActionEvent e ){
				removePanel();
			}
		});
	}

	protected void removePanel() {
		if (!panelStack.isEmpty()){
			HistogramPanel hPanel = panelStack.removeLast();
			hPanel.removeChangeListener(this);
			panelHolder.remove(hPanel);
			repaint();
			stateChanged( CHANGE_EVENT );	
		}			
	}

	protected void addPanel() {
		if (table==null) return;
		HistogramPanel hPanel = new HistogramPanel(table);
		hPanel.addChangeListener(this);
		panelStack.add(hPanel);
		panelHolder.add(hPanel);
		panelHolder.validate();
		stateChanged( CHANGE_EVENT );		
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		table.filtersCollection.clear();
		for ( final HistogramPanel hp : panelStack ){
			table.addFilterMinMax(hp.getKey(), hp.getThreshold(), hp.getUpperThreshold());
		}		
		fireChanged(e);
	}
	

	@Override
	public void setSettings(Map<String, Object> settings) {
		// nothing
	}

	@Override
	public Map<String, Object> getSettings() {
		return new HashMap<String, Object>();
	}

}
