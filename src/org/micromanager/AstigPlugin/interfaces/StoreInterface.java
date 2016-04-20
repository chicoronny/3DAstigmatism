package org.micromanager.AstigPlugin.interfaces;

import java.util.Collection;


public interface StoreInterface<E extends Element> {
	/**
	 * Adds the element el to the store.
	 * @param el is the element to be put
	 */
	void put(E element);
	
	/**
	 * Retrieves (and typically removes) an element from the store.
	 * @return element 
	 */
	E get();
	
	/**
	 * Retrieves an element from the store without removing it.
	 * @return element 
	 */
	E peek();
	
	/**
	 * Checks if the store is empty. 
	 * @return The store is empty.
	 */
	boolean isEmpty();
	
	/**
	 * Length of the store.
	 * @return length.
	 */
	int getLength();
	
	/**
	 * Current view of store.
	 * @return view.
	 */
	Collection<Element> view();
}
