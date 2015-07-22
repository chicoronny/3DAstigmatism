package org.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

public class txtWriter {
	private Locale curLocale;
	private FileWriter w;

	public txtWriter(File file) {
		this.curLocale = Locale.getDefault();
		final Locale usLocale = new Locale("en", "US"); // setting us locale
		Locale.setDefault(usLocale);
		try {
			w = new FileWriter(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void process(String p){
		try {
			w.write(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close(){
		try {
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Locale.setDefault(curLocale);
	}

}
