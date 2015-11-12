package org.micromanager.AstigPlugin.pipeline;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.micromanager.AstigPlugin.interfaces.Element;

import com.google.common.collect.Lists;

public abstract class MultiRunModule extends AbstractModule{
	
	public MultiRunModule(){
	}
	
	@Override
	public void run() {
		if (!inputs.isEmpty() && !outputs.isEmpty()) { // first check for existing inputs
			if (iterator==null)
				iterator = inputs.keySet().iterator().next();
			while (inputs.get(iterator).isEmpty())
				pause(10);
			beforeRun();
			
			final ArrayList<Future<Boolean>> futures = Lists.newArrayList();

			for (int taskNum = 0; taskNum < numThreads; ++taskNum) {

				final Callable<Boolean> r = new Callable<Boolean>() {

					@Override
					public Boolean call() {
						while (running) {
							if (Thread.currentThread().isInterrupted())
								break;
							Element data = nextInput();
							if (data != null) 
								newOutput(processData(data));
							else
								pause(10);
						}
						return running;
					}

				};
				//if (!service.isShutdown() || !service.isTerminated())
					futures.add(service.submit(r));
			}

			for (final Future<Boolean> f : futures) {
				try {
					f.get();
				} catch (final InterruptedException e) {
					System.err.println(getClass().getSimpleName()+e.getMessage());
					e.printStackTrace();
				} catch (final ExecutionException e){
					System.err.println(getClass().getSimpleName()+e.getMessage());
					e.printStackTrace();
				}
			}
			afterRun();
			return;
		}
		if (!inputs.isEmpty()) { // first check for existing inputs
			if (iterator==null)
				iterator = inputs.keySet().iterator().next();
			while (inputs.get(iterator).isEmpty())
				pause(10);
			beforeRun();
			
			final ArrayList<Future<Boolean>> futures = Lists.newArrayList();

			for (int taskNum = 0; taskNum < numThreads; ++taskNum) {

				final Callable<Boolean> r = new Callable<Boolean>() {

					@Override
					public Boolean call() {
						while (running) {
							if (Thread.currentThread().isInterrupted())
								break;
							Element data = nextInput();
							if (data != null) 
								processData(data);
							else pause(10);
						}
						return running;
					}

				};
				//if (!service.isShutdown() || !service.isTerminated())
					futures.add(service.submit(r));
			}

			for (final Future<Boolean> f : futures) {
				try {
					f.get();
				} catch (final InterruptedException  e) {
					System.err.println(getClass().getSimpleName()+e.getMessage());
					e.printStackTrace();
				} catch (final ExecutionException e){
					System.err.println(getClass().getSimpleName()+e.getMessage());
					e.printStackTrace();
				}
			}
			afterRun();
			return;
		}
		if (!outputs.isEmpty()) { // only output
			beforeRun();
			while (running) {
				if (Thread.currentThread().isInterrupted())
					break;
				Element data = processData(null);
				newOutput(data);
			}
			afterRun();
			return;
		}
		return;
	}

	protected void afterRun() {		
	}

	protected void beforeRun() {
		start = System.currentTimeMillis();
	}
}
