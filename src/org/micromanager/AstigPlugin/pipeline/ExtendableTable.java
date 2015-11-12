package org.micromanager.AstigPlugin.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.micromanager.AstigPlugin.interfaces.Element;
import org.micromanager.AstigPlugin.interfaces.Store;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;


/**
 * @author Ronny Sczech
 *
 */
public class ExtendableTable {
	
	private Map<String, List<Number>> table = new LinkedHashMap<String, List<Number>>();
	public Map<String, Predicate<Number>> filtersCollection = new HashMap<String, Predicate<Number>>();
	private Map<String, String> names = new LinkedHashMap<String, String>();
	
	/**
	 * 
	 */
	public ExtendableTable(){
	}
	
	public ExtendableTable(Map<String, List<Number>> table){
		this.table = table;
		for(String key : table.keySet())
			names.put(key,key);
	}
	
		
	/**
	 * @param member - member
	 */
	public void addNewMember(String member) {
		table.put(member,new ArrayList<Number>());
		names.put(member,member);
	}
	
	/**
	 * 
	 */
	public void addXYMembers(){
		//table.put("id",new FastTable<Object>());
		addNewMember("x");
		addNewMember("y");
	}
	
	
	public Set<String> columnNames(){
		return table.keySet();
	}
	
	public ExtendableTable filter(){
		
		if (filtersCollection.isEmpty()) return this;
		
		final ExtendableTable filteredTable = new ExtendableTable(); //new instance
		for (String col: this.columnNames())
			filteredTable.addNewMember(col);
		
		Map<String, Number> row;
		for (int index = 0 ; index < getNumberOfRows(); index++){
			row = getRow(index);
			boolean filtered = true;
			for (String key : filtersCollection.keySet()){
				Number value = row.get(key);
				if (value!=null)
					filtered = filtered && (filtered == filtersCollection.get(key).apply(value));
				else
					filtered = false;
			}
			if(filtered)
				filteredTable.addRow(row);
		}
		return filteredTable;
	}
	
	public void addFilterMinMax(final String col, final double min, final double max){
		Predicate<Number> p = new Predicate<Number>(){

			@Override
			public boolean apply(Number t) {				
				return (t.doubleValue()>=min) && (t.doubleValue()<=max);
			}
			
		};
		filtersCollection.put(col, p);
	}
	
	public void addFilterExact(final String col, final Number o){
		filtersCollection.put(col, Predicates.equalTo(o));
	}
	
	/**
	 * @param row - row
	 */
	public void addRow(Map<String,Number> row){
		for (Entry<String,Number> e : row.entrySet()){
			add(e.getKey(),e.getValue());
		}
	}
	
	/**
	 * @param row - row
	 * @return data
	 */
	public Map<String, Number> getRow(int row){
		Map<String,Number> map = new HashMap<String, Number>(); // row map
		for (String key : table.keySet())
			map.put(key, table.get(key).get(row));
		return map;
	}
	
	/**
	 * @param col - colummn
	 * @return column
	 */
	public List<Number> getColumn(String col){
		return table.get(col);
	}
	
	/**
	 * @param col - colummn
	 * @return column
	 */
	public Object getData(String col, int row){
		List<Number> c = table.get(col);
		if(c != null && row < getNumberOfRows())
			return c.get(row);
		
		System.err.println("unknown column or row");
		return null;
	}
	
	/**
	 * @param member - member 
	 * @param o - object
	 */
	public void add(String member, Number o){
		List<Number> t = table.get(member);
		if (t!=null){
			t.add(o);
			return;
		}
		System.err.println("unknown column");
	}
	
	
	/**
	 * @return number of rows
	 */
	public int getNumberOfRows() {
		return table.values().iterator().next().size();
	}
	
	/**
	 * @return names
	 */
	public Map<String, String> getNames() {
		return names;
	}
	
	
	/**
	 * This method provides a bridge between the Workspace abstraction and the Store abstraction. 
	 * 
	 * It creates a mutable view on the workspace which allows a module working with Stores to have read/write access to the Workspace in a 
	 * first-in-first-out order using the methods provided by the Store interface. 
	 * 
	 * The put method adds the data to the end of the table, the get keeps track of the last row read. 
	 * The get method is NON-BLOCKING: if the table is empty, or you read all rows, it returns 'null'.
	 *  
	 * @return a class implementing the Store interface.
	 */
	public Store getFIFO() {
		
		return new Store () {
			int lastRow = 0;
						
			@Override
			public boolean isEmpty() {
				return lastRow >= getNumberOfRows()-1;
			}
			
			@Override
			public synchronized void put(Element el) {
				if (el instanceof ElementMap){
					final ElementMap em = (ElementMap) el;
					addRow(em);
				}
			}

			@Override
			public synchronized Element get() {
				ElementMap em = null;
				if (!isEmpty())	
					em = new ElementMap(getRow(lastRow++).entrySet());
				else{
					em = new ElementMap(getRow(lastRow-1).entrySet());
					em.setLast(true);
				}
				return em;
			}

			@Override
			public synchronized Element peek() {
				return new ElementMap(getRow(lastRow).entrySet());
			}

			@Override
			public int getLength() {
				return lastRow;
			}

			@Override
			public Collection<Element> view() {
				return this.view();
			}			

		};
	}
}