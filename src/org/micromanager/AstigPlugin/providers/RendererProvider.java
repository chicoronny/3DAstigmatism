package org.micromanager.AstigPlugin.providers;

import org.micromanager.AstigPlugin.factories.RendererFactory;
import org.micromanager.AstigPlugin.plugins.GaussRenderer;
import org.micromanager.AstigPlugin.plugins.HistogramRenderer;


public class RendererProvider extends AbstractProvider<RendererFactory> {

	public RendererProvider() {
		super(RendererFactory.class);
		addModule(HistogramRenderer.Factory.class.getName());
		addModule(GaussRenderer.Factory.class.getName());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final RendererProvider provider = new RendererProvider();
		System.out.println( provider.echo() );
	}

}
