package org.micromanager.AstigPlugin.pipeline;

import ij.IJ;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.SwingWorker;

import org.micromanager.AstigPlugin.interfaces.Store;

public class Manager extends SwingWorker<Void,Void> {
	
	private Map<Integer,Store> storeMap = new LinkedHashMap<Integer, Store>();
	private Map<Integer,AbstractModule> modules = new LinkedHashMap<Integer, AbstractModule>();
	private final ExecutorService service = Executors.newCachedThreadPool();
	private volatile boolean done = false;
	private int maximum = 1;

	public Manager() {
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
	
	@Override
	protected Void doInBackground() throws Exception {
		if (modules.isEmpty()) return null;
		StoreMonitor sm = new StoreMonitor();
		sm.addPropertyChangeListener(new PropertyChangeListener(){
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("progress")) 
					setProgress((Integer) evt.getNewValue());
			}});
		service.execute(sm);
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
			for(Future<?> joiner:threads)
				joiner.get();
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
		}
		done = true;
		sm.cancel(true);
		return null;
	}
	
	@Override
	public void done(){
		setProgress(0);
		service.shutdown();
	}

	public void reset() {
		modules.clear();
		storeMap.clear();
	}
	
	
	class StoreMonitor extends SwingWorker<Void,Void> {

		public StoreMonitor() {
		}

		@Override
		protected Void doInBackground() throws Exception {
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
			return null;
		}
	}
}
