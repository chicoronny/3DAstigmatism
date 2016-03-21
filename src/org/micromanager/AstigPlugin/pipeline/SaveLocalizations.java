package org.micromanager.AstigPlugin.pipeline;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.micromanager.AstigPlugin.interfaces.Element;
import org.micromanager.AstigPlugin.interfaces.LocalizationInterface;

import ij.IJ;

public class SaveLocalizations extends SingleRunModule {

	final private Locale curLocale;
	private File file;
	private FileWriter w;
	private int counter = 0;
	private static String[] preferredOrder= new String[]{"ID","x","y","z","sX","sY","sZ","N", "Bg", "xdetect", "ydetect", "RMS", "Iter", "Eval", "frame"}; 

	public SaveLocalizations(File file) {
		this.curLocale = Locale.getDefault();
		this.file = file;
	}

	@Override
	public void beforeRun() {
		final Locale usLocale = new Locale("en", "US"); // setting us locale
		Locale.setDefault(usLocale);

		Element el = inputs.get(iterator).peek();
		Set<String> headset = new LinkedHashSet<String>();
		try {
			w = new FileWriter(file);
			BeanInfo b = Introspector.getBeanInfo(el.getClass());
			for (PropertyDescriptor p : b.getPropertyDescriptors()) {
				String prop = p.getName();
				boolean test = prop.contains("class") | prop.contains("last");
				if (!test){
					headset.add(prop);
				}
			}
			String headline="";
			for (int n=0; n<preferredOrder.length; n++){
				if(headset.contains(preferredOrder[n]))
					headline += preferredOrder[n]+ "\t";
			}
			headline = headline.substring(0, headline.length()-1);
			w.write(headline+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IntrospectionException e) {
			e.printStackTrace();
		}

		start = System.currentTimeMillis();
	}

	@Override
	public Element processData(Element data) {

		if (data.isLast()) {
			if (inputs.get(iterator).isEmpty()) {
				cancel();
				return null;
			}
			inputs.get(iterator).put(data);
			return null;
		}
		
		LocalizationInterface loc = (LocalizationInterface) data;

		try {
			w.write(loc.toString()+"\n");
		} catch (IOException e) {
			IJ.error("SaveLocalization:"+e.getMessage());
		}
		counter++;
		return null;
	}

	@Override
	public void afterRun() {
		try {
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Locale.setDefault(curLocale);
		System.out.println("" + counter + " fitted localizations saved in "
				+ (System.currentTimeMillis() - start) + "ms.");
	}

	@Override
	public boolean check() {
		return inputs.size()==1;
	}
}
