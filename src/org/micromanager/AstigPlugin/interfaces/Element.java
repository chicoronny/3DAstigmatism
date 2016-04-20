package org.micromanager.AstigPlugin.interfaces;

public interface Element{
	/**
	 * @return checks if Element is the last in the queue.
	 */
	boolean isLast();
	
	void setLast(boolean b);
}
