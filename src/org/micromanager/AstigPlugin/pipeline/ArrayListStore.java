package org.micromanager.AstigPlugin.pipeline;

import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.micromanager.AstigPlugin.interfaces.Element;
import org.micromanager.AstigPlugin.interfaces.Store;

public class ArrayListStore implements Store {
	
	private final Queue<Element> q;
	
	public ArrayListStore(){
		q = new LinkedBlockingQueue<Element>();
	}

	public ArrayListStore(int n) {
		q = new LinkedBlockingQueue<Element>(n);
	}

	@Override
	public void put(Element element) {
		while(!q.offer(element)){
			//System.out.println("Queue "+this.hashCode()+" is full "+this.getLength());
			pause(10);
		}
	}

	@Override
	public Element get() {
		Element e = q.poll();
		if(e==null){
			pause(1);
			//System.out.println("Queue "+this.hashCode()+" is empty "+this.getLength());
		}
		return e;
	}

	@Override
	public Element peek() {
		return q.peek();
	}

	@Override
	public boolean isEmpty() {
		return q.size()<1 || q.isEmpty();
	}

	@Override
	public int getLength() {
		return q.size();
	}

	@Override
	public Collection<Element> view() {
		return Collections.unmodifiableCollection(q);
	}
	
	private void pause(long ms){
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			System.err.println("Pause:"+e.getMessage());
		}
	}

}
