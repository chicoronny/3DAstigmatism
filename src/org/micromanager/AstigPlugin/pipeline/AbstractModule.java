package org.micromanager.AstigPlugin.pipeline;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.micromanager.AstigPlugin.interfaces.Element;
import org.micromanager.AstigPlugin.interfaces.Frame;
import org.micromanager.AstigPlugin.interfaces.Store;

public abstract class AbstractModule implements Runnable {
	
	protected int numTasks;
	final int numThreads = Runtime.getRuntime().availableProcessors()-1;
	ExecutorService service;
	protected final Map<Integer, Store> inputs = new LinkedHashMap<Integer, Store>();
	protected final Map<Integer, Store> outputs = new LinkedHashMap<Integer, Store>();
	protected long start;
	protected volatile boolean running = true;
	Integer iterator;
	
	
	AbstractModule(){
	}
	
	public void reset(){
		inputs.clear();
		outputs.clear();
		running = true;
		iterator = null;
	}
	
	void setService(ExecutorService service){
		this.service = service;
	}
	
	protected void newOutput(final Element data) {
		if (outputs.isEmpty()) throw new NullPointerException("No Output Mappings!");
		if (data == null) return;
		for (Integer key : outputs.keySet()) {
			outputs.get(key).put(data);
		}
	}
	
	Element nextInput() {
		return getInput(iterator);
	}


	protected void cancel() {
		running = false;
	}

	private Element getInput(Integer key) {
		return inputs.get(key).get();
	}

	protected Map<Integer, Element> getInputs() {
		Map<Integer, Element> outMap = new HashMap<Integer, Element>();
		for (Integer key : inputs.keySet()) {
			outMap.put(key, inputs.get(key).get());
		}
		return outMap;
	}

	protected Element getOutput(Integer key) {
		return outputs.get(key).get();
	}

	protected Map<Integer, Element> getOutputs() {
		Map<Integer, Element> outMap = new HashMap<Integer, Element>();
		for (Integer key : outputs.keySet()) {
			outMap.put(key, outputs.get(key).get());
		}
		return outMap;
	}

	protected void setInput(Integer key, Store store) {
		inputs.put(key, store);		
	}
	
	public void setInput(Store store) {
		inputs.put(store.hashCode(), store);		
	}

	protected void setOutput(Integer key, Store store) {
		outputs.put(key, store);		
	}
	
	public void setOutput(Store store) {
		outputs.put(store.hashCode(), store);		
	}

	protected boolean isRunning() {
		return running;
	}
	
	@SuppressWarnings("static-method")
	void pause(long ms){
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			System.err.println("Pause:"+e.getMessage());
		}
	}
	
	public Element preview(Element el){
		return processData(el);
	}
	
	public <T> Element preview(Frame<T> el){
		return processData(el);
	}	
	
	/**
	 * Method to be overwritten by children of this class.
	 * @param data - data to process
	 * @return Element
	 */
	protected abstract Element processData(Element data);

	protected boolean check() {
		return false;
	}
}
