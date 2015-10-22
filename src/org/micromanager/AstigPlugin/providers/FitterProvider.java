package org.micromanager.AstigPlugin.providers;

import org.micromanager.AstigPlugin.factories.FitterFactory;
import org.micromanager.AstigPlugin.plugins.AstigFitter;
import org.micromanager.AstigPlugin.plugins.GaussianFitter;
import org.micromanager.AstigPlugin.plugins.QuadraticFitter;


public class FitterProvider extends AbstractProvider<FitterFactory> {

	public FitterProvider() {
		super(FitterFactory.class);
		addModule(AstigFitter.Factory.class.getName());
		addModule(QuadraticFitter.Factory.class.getName());
		addModule(GaussianFitter.Factory.class.getName());
	}
	
	public static void main( final String[] args ){
		final FitterProvider provider = new FitterProvider();
		System.out.println( provider.echo() );
	}


}
