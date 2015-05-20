package org.ThreeDimAstig.graphics;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class ChartBuilder {
	public XYDataset createDataSet(double[] A, String nameA){
	    XYSeriesCollection dataset = new XYSeriesCollection();
	    XYSeries series = new XYSeries(nameA);
	    
		for(int i=0;i<A.length;i++){
			series.add(i, A[i]);
		}
	    dataset.addSeries(series);
	 
	    return dataset;
	}	
	
	public XYDataset createDataSet(double[] A, String nameA, double[] B, String nameB){
	    XYSeriesCollection dataset = new XYSeriesCollection();
	    XYSeries series1 = new XYSeries(nameA);
	    XYSeries series2 = new XYSeries(nameB);

		for(int i=0;i<A.length;i++){
			series1.add(i, A[i]);
		}
		for(int i=0;i<B.length;i++){
			series2.add(i, B[i]);
		}
	    dataset.addSeries(series1);
	    dataset.addSeries(series2);
	 
	    return dataset;
	}
	
	public XYDataset createDataSet(double[] X, double[] Y, String nameDataSet){
	    XYSeriesCollection dataset = new XYSeriesCollection();
	    XYSeries series1 = new XYSeries(nameDataSet);

	    if(X.length != Y.length){
	    	// generate exception here //////////////////////////////////////////////////////////////////////////////////////////////////
	    	return null;
	    }
	    
		for(int i=0;i<X.length;i++){
			series1.add(X[i], Y[i]);
		}
	    dataset.addSeries(series1);
	 
	    return dataset;
	}
	
	public XYDataset createDataSet(double[] X, double[] Y1, String nameY1, double[] Y2, String nameY2){
	    XYSeriesCollection dataset = new XYSeriesCollection();
	    XYSeries series1 = new XYSeries(nameY1);
	    XYSeries series2 = new XYSeries(nameY2);

	    if(X.length != Y1.length || X.length != Y2.length){
	    	// generate exception here //////////////////////////////////////////////////////////////////////////////////////////////////
	    	return null;
	    }
	    
		for(int i=0;i<X.length;i++){
			series1.add(X[i], Y1[i]);
			series2.add(X[i], Y2[i]);
		}
	    dataset.addSeries(series1);
	    dataset.addSeries(series2);
	 
	    return dataset;
	}

	public void plotXYDots(double[] A, String nameA, double[] B, String nameB, String domainName, String rangeName, String plotTitle){
		createXYDots(createDataSet(A, nameA, B, nameB), domainName, rangeName, plotTitle);
	}
	
	public void plotXYDots(double[] X, double[] Y1, String nameY1, double[] Y2, String nameY2, String domainName, String rangeName, String plotTitle){
		createXYDots(createDataSet(X, Y1, nameY1, Y2, nameY2), domainName, rangeName, plotTitle);
	}
	
	public void plotXYLine(double[] X, double[] Y, String nameY, String domainName, String rangeName, String plotTitle){
		createXYLine(createDataSet(X, Y, nameY), domainName, rangeName, plotTitle);
	}
	public void plotXYDotsAndLines(double[] X, double[] Y1, String nameY1, double[] Y2, String nameY2, double[] fY1, String namefY1, double[] fY2, String namefY2, String domainName, String rangeName, String plotTitle){
		createXYDotsAndLines(createDataSet(X, Y1, nameY1, Y2, nameY2),createDataSet(X, fY1, namefY1, fY2, namefY2), domainName, rangeName, plotTitle);
	}

	private void createXYDotsAndLines(XYDataset dataset1, XYDataset dataset2, String domainName, String rangeName,String plotTitle) {
		// Create a single plot containing both the scatter and line
		XYPlot plot = new XYPlot();

		/* SETUP SCATTER */

		// Create the scatter data, renderer, and axis
		XYDataset collection1 = dataset2;
		XYItemRenderer renderer1 = new XYLineAndShapeRenderer(true, false);   // Lines only
		ValueAxis domain1 = new NumberAxis(domainName);
		ValueAxis range1 = new NumberAxis(rangeName);

		// Set the scatter data, renderer, and axis into plot
		plot.setDataset(0, collection1);
		plot.setRenderer(0, renderer1);
		plot.setDomainAxis(0, domain1);
		plot.setRangeAxis(0, range1);

		// Map the scatter to the first Domain and first Range
		plot.mapDatasetToDomainAxis(0, 0);
		plot.mapDatasetToRangeAxis(0, 0);

		/* SETUP LINE */

		// Create the line data, renderer, and axis
		XYDataset collection2 = dataset1;
		XYItemRenderer renderer2 = new XYLineAndShapeRenderer(false, true);   // Shapes only

		// Set the line data, renderer, and axis into plot
		plot.setDataset(1, collection2);
		plot.setRenderer(1, renderer2);
		plot.setDomainAxis(1, domain1);
		plot.setRangeAxis(1, range1);

		// Map the line to the second Domain and second Range
		plot.mapDatasetToDomainAxis(1, 1);
		plot.mapDatasetToRangeAxis(1, 1);

		// Create the chart with the plot and a legend
		JFreeChart chart = new JFreeChart(plotTitle, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		
		ChartPanel cp = new ChartPanel(chart);
	    cp.setPreferredSize(new Dimension(750,550));
	    
		JPanel jp = new JPanel();
	    jp.add(cp);
		jp.setPreferredSize(new Dimension(800,600));

		JFrame jf = new JFrame();
		jf.setPreferredSize(new Dimension(800,600));
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		jf.setLocation(dim.width/2-400, dim.height/2-300);
		
		jf.setContentPane(jp);
		jf.pack();
		jf.setVisible(true);
	}
	
	public void createXYDots(XYDataset xy, String domainName, String rangeName, String plotTitle){															////////////////////////////// change to be less redundant with previous function
		// Create a single plot
		XYPlot plot = new XYPlot();

		/* SETUP SCATTER */

		// Create the scatter data, renderer, and axis
		XYDataset collection = xy;
		XYItemRenderer renderer = new XYLineAndShapeRenderer(false, true);   // Shapes only
		ValueAxis domain = new NumberAxis(domainName);
		ValueAxis range = new NumberAxis(rangeName);

		// Set the scatter data, renderer, and axis into plot
		plot.setDataset(0, collection);
		plot.setRenderer(0, renderer);
		plot.setDomainAxis(0, domain);
		plot.setRangeAxis(0, range);

		// Map the scatter to the first Domain and first Range
		plot.mapDatasetToDomainAxis(0, 0);
		plot.mapDatasetToRangeAxis(0, 0);

		// Create the chart with the plot and a legend
		JFreeChart chart = new JFreeChart(plotTitle, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		
		ChartPanel cp = new ChartPanel(chart);
	    cp.setPreferredSize(new Dimension(750,550));
		
		JPanel jp = new JPanel();
	    jp.add(cp);
		jp.setPreferredSize(new Dimension(800,600));

		JFrame jf = new JFrame();
		jf.setPreferredSize(new Dimension(800,600));
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		jf.setLocation(dim.width/2-400, dim.height/2-300);
		
		jf.setContentPane(jp);
		jf.pack();
		jf.setVisible(true);
	}
	
	public void createXYLine(XYDataset xy, String domainName, String rangeName, String plotTitle){															////////////////////////////// change to be less redundant with previous function
		// Create a single plot
		XYPlot plot = new XYPlot();

		/* SETUP SCATTER */

		// Create the scatter data, renderer, and axis
		XYDataset collection = xy;
		XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);   // line only
		ValueAxis domain = new NumberAxis(domainName);
		ValueAxis range = new NumberAxis(rangeName);

		// Set the scatter data, renderer, and axis into plot
		plot.setDataset(0, collection);
		plot.setRenderer(0, renderer);
		plot.setDomainAxis(0, domain);
		plot.setRangeAxis(0, range);

		// Map the scatter to the first Domain and first Range
		plot.mapDatasetToDomainAxis(0, 0);
		plot.mapDatasetToRangeAxis(0, 0);

		// Create the chart with the plot and a legend
		JFreeChart chart = new JFreeChart(plotTitle, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		
		ChartPanel cp = new ChartPanel(chart);
	    cp.setPreferredSize(new Dimension(750,550));
		
		JPanel jp = new JPanel();
	    jp.add(cp);
		jp.setPreferredSize(new Dimension(800,600));

		JFrame jf = new JFrame();
		jf.setPreferredSize(new Dimension(800,600));
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		jf.setLocation(dim.width/2-400, dim.height/2-300);
		
		jf.setContentPane(jp);
		jf.pack();
		jf.setVisible(true);
	}
}
