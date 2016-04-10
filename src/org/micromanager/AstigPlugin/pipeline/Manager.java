package org.micromanager.AstigPlugin.pipeline;

import ij.IJ;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.micromanager.AstigPlugin.interfaces.Store;

public class Manager{
	
	public static final int STATE_DONE = 1;
	private Map<Integer,Store> storeMap = new LinkedHashMap<Integer, Store>();
	private Map<Integer,AbstractModule> modules = new LinkedHashMap<Integer, AbstractModule>();
	private ExecutorService service;
	private volatile boolean done = false;
	private int maximum = 1;
	private int progress;
	private final List< PropertyChangeListener > changeListeners = new ArrayList< PropertyChangeListener >();

	public Manager(ExecutorService service) {
		this.service = service;
	}
	
	public void addPropertyChangeListener( final PropertyChangeListener listener ){
		changeListeners.add( listener );
	}
	
	public void firePropertyChanged(PropertyChangeEvent e) {
		for ( final PropertyChangeListener cl : changeListeners )
			cl.propertyChange( e );
	}

	public void add(AbstractModule module){		
		modules.put(module.hashCode(),module);		
	}
	
	public void linkModules(AbstractModule from, AbstractModule to, boolean noInputs, int maxElements){
		Store s = null;
		if (noInputs){
			int n = (int) Math.min(Runtime.getRuntime().freeMemory()/Math.pow(2,17), maxElements*0.5); // performance tweak
			System.out.println("Manager starts with maximal "+n+" elements" );
			s = new ArrayListStore(n);
		} else {
			s = new ArrayListStore();
		}
		AbstractModule source = modules.get(from.hashCode());
		if (source==null) throw new NullPointerException("Wrong linkage!");
		source.setOutput(s);
		AbstractModule well = modules.get(to.hashCode());
		if (well==null) throw new NullPointerException("Wrong linkage!");
		well.setInput(s);
		storeMap.put(s.hashCode(), s);
	}
	
	public void linkModules(AbstractModule from , AbstractModule to ){
		AbstractModule source = modules.get(from.hashCode());
		if (source==null) throw new NullPointerException("Wrong linkage!");
		Store s = new ArrayListStore();
		source.setOutput(s);
		AbstractModule well = modules.get(to.hashCode());
		if (well==null) throw new NullPointerException("Wrong linkage!");
		well.setInput(s);
		storeMap.put(s.hashCode(), s);
	}
	
	public Map<Integer, Store> getMap(){
		return storeMap;
	}
	
	public Map<Integer, AbstractModule> getModules(){
		return modules;
	}
	
	public void reset(){
		storeMap.clear();
		modules.clear();
		done = false;			
		setProgress(0);
	}
	
	public void startAndJoin() {
		final Runner r = new Runner();
		r.start();
		try {
			r.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void setProgress(int i) {
		final PropertyChangeEvent EVENT_PROGRESS = new PropertyChangeEvent(this, "progress", progress, i);
		progress=i;
		firePropertyChanged(EVENT_PROGRESS);
	}

	
	private class Runner extends Thread{ 
		@Override
		public void run() {
			if (modules.isEmpty()) return;
			StoreMonitor sm = new StoreMonitor();
			sm.start();
			final List<Future<?>> threads= new ArrayList<Future<?>>();
			
			for(AbstractModule starter:modules.values()){
				if (!starter.check()) {
					IJ.error("Module not linked properly " + starter.getClass().getSimpleName());
					break;
				}
				starter.setService(service);
				threads.add(service.submit(starter));
				
				try {
					Thread.sleep(10); 						// HACK : give the module some time to start working
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			try {
				for(Future<?> joiner:threads){
					joiner.get(); 
					while(!joiner.isDone()) Thread.sleep(10);
				}
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			} catch (ExecutionException e) {
				System.err.println(e.getMessage());
			}
			setProgress(100);
			done = true;
			try {
	            sm.join(200);
	        } catch (InterruptedException ignore) {}
			
			final PropertyChangeEvent EVENT_DONE = new PropertyChangeEvent(this, "state", 0, STATE_DONE);
			firePropertyChanged(EVENT_DONE);
			return;
		}
	}
	
	private class StoreMonitor extends Thread{

		@Override
		public void run() {
			while(!done){
				try {
	                Thread.sleep(200);
	            } catch (InterruptedException ignore) {break;}
				int max = 0;
				int n = 0;
				for(Integer key : storeMap.keySet()){
					n = storeMap.get(key).getLength();
					max= Math.max(n, max);
				}
				if (max > maximum)
					maximum = max;
				
				setProgress(Math.round(100-(float)max/maximum*100));
			}
		}
	}
}
