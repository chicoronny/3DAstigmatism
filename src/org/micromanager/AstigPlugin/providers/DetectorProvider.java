package org.micromanager.AstigPlugin.providers;

import org.micromanager.AstigPlugin.factories.DetectorFactory;
import org.micromanager.AstigPlugin.plugins.DoGFinder;
import org.micromanager.AstigPlugin.plugins.NMSDetector;
import org.micromanager.AstigPlugin.plugins.PeakFinder;

public class DetectorProvider extends AbstractProvider<DetectorFactory> {

	public DetectorProvider() {
		super(DetectorFactory.class);
		addModule(DoGFinder.Factory.class.getName());
		addModule(NMSDetector.Factory.class.getName());
		addModule(PeakFinder.Factory.class.getName());
	}
	
	public static void main( final String[] args ){
		final DetectorProvider provider = new DetectorProvider();
		System.out.println( provider.echo() );
	}


}
