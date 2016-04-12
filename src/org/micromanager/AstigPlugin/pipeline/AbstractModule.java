package org.micromanager.AstigPlugin.pipeline;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.micromanager.AstigPlugin.interfaces.Element;
import org.micromanager.AstigPlugin.interfaces.Frame;
import org.micromanager.AstigPlugin.interfaces.Store;

public abstract class AbstractModule implements Runnable {
	
	protected int numTasks;
	protected int numThreads = Runtime.getRuntime().availableProcessors()-1;
	protected ExecutorService service;
	protected Map<Integer, Store> inputs = new LinkedHashMap<Integer, Store>();
	protected Map<Integer, Store> outputs = new LinkedHashMap<Integer, Store>();
	protected long start;
	protected volatile boolean running = true;
	protected Integer iterator;
	
	
	public AbstractModule(){
	}
	
	public void reset(){
		inputs.clear();
		outputs.clear();
		running = true;
		iterator = null;
	}
	
	protected void setService(ExecutorService service){
		this.service = service;
	}
	
	protected void newOutput(final Element data) {
		if (outputs.isEmpty()) throw new NullPointerException("No Output Mappings!");
		if (data == null) return;
		Iterator<Integer> it = outputs.keySet().iterator();
		while(it.hasNext()){
			Integer key = it.next();
			outputs.get(key).put(data);
		}
	}
	
	protected Element nextInput() {
		return getInput(iterator);
	}


	protected void cancel() {
		running = false;
	}

	protected Element getInput(Integer key) {
		Element el = inputs.get(key).get();
		return el;
	}

	protected Map<Integer, Element> getInputs() {
		Map<Integer, Element> outMap = new HashMap<Integer, Element>();
		Iterator<Integer> it = inputs.keySet().iterator();
		while(it.hasNext()){
			Integer key = it.next();
			outMap.put(key, inputs.get(key).get());
		}
		return outMap;
	}

	protected Element getOutput(Integer key) {
		return outputs.get(key).get();
	}

	protected Map<Integer, Element> getOutputs() {
		Map<Integer, Element> outMap = new HashMap<Integer, Element>();
		Iterator<Integer> it = outputs.keySet().iterator();
		while(it.hasNext()){
			Integer key = it.next();
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
	protected void pause(long ms){
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
