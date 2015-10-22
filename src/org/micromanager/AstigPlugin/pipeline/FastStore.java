package org.micromanager.AstigPlugin.pipeline;

import java.util.Collection;
import org.micromanager.AstigPlugin.interfaces.Element;
import org.micromanager.AstigPlugin.interfaces.Store;

import javolution.util.FastTable;

public class FastStore implements Store {
	
	private FastTable<Element> q = new FastTable<Element>();
	
	/**
	 * 
	 */
	public FastStore(){
	}
	
	/**
	 * @return Returns the length of the queue.
	 */
	@Override
	public int getLength() {
		return q.size();
	}

	@Override
	public synchronized void put(Element el) {
			q.addLast(el);
	}
	
	@Override
	public synchronized Element peek(){
		Element res = null;
		
			while (!isEmpty()){
				res = q.peek();
				if (res != null) break;
			} 
				
		return res;
	}

	@Override
	public synchronized Element get() {
		Element res = null;
		
		while (!isEmpty()){
			res = q.poll();
			if (res != null) break;
		} 	
		return res;
	}

	@Override
	public boolean isEmpty() {
		return q.size()<1 || q.isEmpty();
	}
	
	@Override
	public  Collection<Element> view(){
		return q.immutable().value();
	}

}
