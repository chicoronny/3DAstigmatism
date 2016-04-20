package org.micromanager.AstigPlugin.pipeline;

import org.micromanager.AstigPlugin.interfaces.Element;

import ij.IJ;

public abstract class SingleRunModule extends AbstractModule {

	@Override
	public void run() {

		if (!inputs.isEmpty() && !outputs.isEmpty()) {
			if (inputs.keySet().iterator().hasNext() && iterator==null)
				iterator = inputs.keySet().iterator().next();
			while (inputs.get(iterator).isEmpty())
				pause(10);
			
			beforeRun();
			while (running) {
				Element data = nextInput();
				if (data != null) 
					processData(data);
				else pause(10);
			}
			afterRun();
			return;
		}
		if (!inputs.isEmpty()) {  // no outputs
			if (inputs.keySet().iterator().hasNext() && iterator==null)
				iterator = inputs.keySet().iterator().next();
			while (inputs.get(iterator).isEmpty())
				pause(10);
			
			beforeRun();
			while (running) {
				Element data = nextInput();
				if (data != null) 
					processData(data);
				else 
					pause(10);
			}
			afterRun();
			return;
		}
		if (!outputs.isEmpty()) { // no inputs
			beforeRun();
			while (running) {
				newOutput(processData(null));
			}
			afterRun();
			return;
		}
		IJ.error("No inputs or outputs!");
	}

	protected void afterRun() {
	}

	void beforeRun() {
		start = System.currentTimeMillis();
		running = true;
	}
	
}
