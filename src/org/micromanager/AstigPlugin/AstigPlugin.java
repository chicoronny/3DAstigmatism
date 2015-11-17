package org.micromanager.AstigPlugin;

import java.util.Locale;

import org.micromanager.AstigPlugin.gui.Controller;
import org.micromanager.AstigPlugin.interfaces.Frame;
import org.micromanager.api.MMPlugin;
import org.micromanager.api.ScriptInterface;

import mmcorej.CMMCore;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;

@SuppressWarnings("unused")
public class AstigPlugin <T extends NumericType<T> & NativeType<T> & RealType<T> & IntegerType<T>> implements MMPlugin{
	
	public static String menuName = "3DAstigmatism";
	public static String tooltipDescription = "3DAstigmatism calibration and fit";
	
	private Controller<T> frame;
	
	private ScriptInterface app_;
	private CMMCore core_;
	private Locale curLocale;


	@Override
	public String getCopyright() {
		return "Ronny Sczech and Joran Deschamps";
	}

	@Override
	public String getDescription() {
		return tooltipDescription;
	}

	@Override
	public String getInfo() {
		return "";
	}

	@Override
	public String getVersion() {
		return "0.4";
	}

	@Override
	public void dispose() {
		Locale.setDefault(curLocale);
		if (frame!=null){
			frame.setVisible(false);
			frame.dispose();
			frame = null;
		}
	}

	@Override
	public void setApp(ScriptInterface app) {
		 app_ =  app;
	     core_ = app.getMMCore();
	     this.curLocale = Locale.getDefault();
		 final Locale usLocale = new Locale("en", "US"); // setting us locale
		 Locale.setDefault(usLocale);
	}

	@Override
	public void show() {
		if (frame==null){
			frame = new Controller<T>();
			frame.setVisible(true);
		}
		else{
			frame.setVisible(true);
			frame.toFront();
		}
	}

}
