package org.micromanager.AstigPlugin.providers;

import org.micromanager.AstigPlugin.factories.FitterFactory;
import org.micromanager.AstigPlugin.plugins.AstigFitter;
import org.micromanager.AstigPlugin.plugins.CentroidFitter;

public class FitterProvider extends AbstractProvider<FitterFactory> {

	public FitterProvider() {
		super(FitterFactory.class);
		addModule(AstigFitter.Factory.class.getName());
		addModule(CentroidFitter.Factory.class.getName());

	}
	
	public static void main( final String[] args ){
		final FitterProvider provider = new FitterProvider();
		System.out.println( provider.echo() );
	}


}
