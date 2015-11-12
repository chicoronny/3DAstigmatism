package org.micromanager.AstigPlugin.interfaces;

public interface Element{
	/**
	 * @return checks if Element is the last in the queue.
	 */
	public boolean isLast();
	
	public void setLast(boolean b);
}