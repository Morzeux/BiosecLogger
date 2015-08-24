package biosecLogger.charts;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import biosecLogger.analysis.Result;
import biosecLogger.analysis.Results;

/**
 * Builds graphs for user samples. Contains bar chart and line chart.
 * 
 * @author Stefan Smihla
 *
 */
public class ChartBuilder {

	private Activity act;
	
	private double maxValue;
	private double minValue;
	
	/**
	 * Creates instance of ChartBuilder with activity.
	 */
	public ChartBuilder(Activity act){
		this.act = act;
	}
	
	/**
	 * Format legend string for bar chart.
	 * 
	 * @param text
	 * 			beginning text
	 * @param phrase
	 * 			evaluated phrase
	 * @return	formatted string
	 */
	private String getLegendString(String text, String phrase){
		return String.format(Locale.ENGLISH, "%s - \"%s\"", text, phrase);
	}
	
	/**
	 * Computes color from row to obtain always same color for specific set of row.
	 * 
	 * @return	computed color
	 */
	private int computeColor(List<Double> row){
		double color = 0;
		for (double value : row){
			color += Math.abs((value * 10000)) % 0xff000000;
		}
		
		return -1 * ((int) color % 0xff000000);
	}
	
	/**
	 * Inits graph renderer.
	 * 
	 * @param title
	 * 			graph title
	 * @param xLabel
	 * 			x label
	 * @param yLabel
	 * 			y label
	 * @return	base renderer
	 */
	private XYMultipleSeriesRenderer initBaseRenderer(String title, String xLabel, String yLabel){
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		
		renderer.setAxisTitleTextSize(16);
		renderer.setChartTitleTextSize(20);
		renderer.setLabelsTextSize(15);
		renderer.setLegendTextSize(15);
		renderer.setMargins(new int[] {30, 30, 30, 0});
		
		renderer.setChartTitle(title);
		renderer.setXTitle(xLabel);
		renderer.setYTitle(yLabel);
		
		renderer.setApplyBackgroundColor(true);
		renderer.setBackgroundColor(Color.BLACK);
		renderer.setMarginsColor(Color.BLACK);
		renderer.setShowGrid(true);
		
		renderer.setZoomButtonsVisible(true);
		
		return renderer;
	}
	
	/**
	 * Sets limits to renderer. Limits are set to graph values range.
	 * 
	 * @param renderer
	 * 			renderer to set
	 * @param values
	 * 			minX, maxX, minY, maxY limit values
	 */
	private void setLimitsRenderer(XYMultipleSeriesRenderer renderer, double[] values){
		renderer.setXAxisMin(values[0]);
		renderer.setXAxisMax(values[1]);
		renderer.setYAxisMin(values[2]);
		renderer.setYAxisMax(values[3]);
		
		renderer.setPanLimits(values);
		renderer.setZoomLimits(values);
	}
	
	/**
	 * Returns renderer for bar graph.
	 * 
	 * @param title
	 * 			graph title
	 * @param xLabel
	 * 			x label
	 * @param yLabel
	 * 			y label
	 * @param res
	 * 			results
	 * @return	complete renderer
	 */
	private XYMultipleSeriesRenderer setBarRenderer(String title, String xLabel, String yLabel, Results res){
		XYMultipleSeriesRenderer renderer = initBaseRenderer(title, xLabel, yLabel);
		setLimitsRenderer(renderer, new double[] {0, res.getSimpleResult().getUserCount() + 1, 0, maxValue});
		renderer.setBarSpacing(0.25);
		renderer.setXAxisMax(5);
		
		int[] colors = new int[]{Color.rgb(0, 0, 200), Color.rgb(0, 0, 100), 
				Color.rgb(0, 200, 0), Color.rgb(0, 100, 0)};
		
		SimpleSeriesRenderer r;
		for (int i = 0; i < colors.length; i++){
			r = new SimpleSeriesRenderer();
			r.setColor(colors[i]);
			renderer.addSeriesRenderer(r);
		}
		
		return renderer;
	}
	
	/**
	 * Returns renderer for line graph.
	 * 
	 * @param title
	 * 			graph title
	 * @param xLabel
	 * 			x label
	 * @param yLabel
	 * 			y label
	 * @param values
	 * 			xy values to render
	 * @return	complete renderer
	 */
	private XYMultipleSeriesRenderer setLineRenderer(String title, String xLabel, String yLabel, List<List<Double>> values){
		XYMultipleSeriesRenderer renderer = initBaseRenderer(title, xLabel, yLabel);
		setLimitsRenderer(renderer, new double[] {1, values.get(0).size(), minValue, maxValue});
		
		XYSeriesRenderer r;
		for (int i = 0; i < values.size(); i++){
			r = new XYSeriesRenderer();
			r.setColor(computeColor(values.get(i)));
			r.setPointStyle(PointStyle.CIRCLE);
			r.setFillPoints(true);
			renderer.addSeriesRenderer(r);
		}
		
		return renderer;
	}
	
	
	/**
	 * Sets values to bar graphs.
	 * 
	 * @param dataset
	 * 			dataset to add values
	 * @param values
	 * 			source values
	 * @param category
	 * 			bar category
	 * @return	max value to set limit
	 */
	private double addSeriesToBarChart(XYMultipleSeriesDataset dataset, double[] values, String category){
		CategorySeries series = new CategorySeries(category);
		
		double maxValue = 0;
		for (int i = 0; i < values.length; i++) {
			series.add(values[i] * 100);
			if (maxValue < values[i] * 100){
				maxValue = values[i] * 100;
			}
		}
		
		dataset.addSeries(series.toXYSeries());
		return maxValue;
	}
	
	/**
	 * Sets bar dataset from results.
	 * 
	 * @param res
	 * 			source results
	 * @return	complete dataset
	 */
	private XYMultipleSeriesDataset setBarDataSet(Results res){
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		maxValue = 0;
		minValue = 0;
		
		Result simplePass = res.getSimpleResult();
		Result complexPass = res.getComplexResult();
		
		Double[] maxValues = new Double[4];
		maxValues[0] = addSeriesToBarChart(dataset, simplePass.getFarByUser(), 
				getLegendString("FAR", simplePass.getPhrase()));
		maxValues[1] = addSeriesToBarChart(dataset, simplePass.getFrrByUser(), 
				getLegendString("FRR", simplePass.getPhrase()));
		maxValues[2] = addSeriesToBarChart(dataset, complexPass.getFarByUser(), 
				getLegendString("FAR", complexPass.getPhrase()));
		maxValues[3] = addSeriesToBarChart(dataset, complexPass.getFarByUser(), 
				getLegendString("FRR", complexPass.getPhrase()));

		maxValue = Collections.max(Arrays.asList(maxValues));
		return dataset;
	}
	
	/**
	 * Sets line graph dataset from xy values.
	 * 
	 * @param values
	 * 			source values
	 * @return	complete dataset
	 */
	private XYMultipleSeriesDataset setLineDataset(List<List<Double>> values) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		maxValue = 0;
		minValue = Double.POSITIVE_INFINITY;
		
		int samplesCount = values.size();
		
		for (int i = 0; i < samplesCount; i++) {
			XYSeries series = new XYSeries("" + i);
			int j = 0;
			for (double value : values.get(i)){
				if (value > maxValue){
					maxValue = value;
				}
				
				if (value < minValue){
					minValue = value;
				}
				
				series.add(j+1, value);
				j++;
			}
			
			dataset.addSeries(series);
		}
		return dataset;
	}
	
	/**
	 * Builds bar graph from results.
	 * 
	 * @param title
	 * 			graph title
	 * @param xLabel
	 * 			x label
	 * @param yLabel
	 * 			y label
	 * @param res
	 * 			source results
	 * @return	intent with new activity
	 */
	public Intent buildBarChart(String title, String xLabel, String yLabel, Results res){ 
	    return ChartFactory.getBarChartIntent(act, setBarDataSet(res), 
	    		setBarRenderer(title, xLabel, yLabel, res), Type.DEFAULT);
	}
	
	/**
	 * Builds bar graph from xy values.
	 * 
	 * @param title
	 * 			graph title
	 * @param xLabel
	 * 			x label
	 * @param yLabel
	 * 			y label
	 * @param values
	 * 			source values
	 * @return	intent with new activity
	 */
	public Intent buildLineChart(String title, String xLabel, String yLabel, List<List<Double>> values){ 
	    return ChartFactory.getLineChartIntent(act, setLineDataset(values), 
	    		setLineRenderer(title, xLabel, yLabel, values));
	}
}
