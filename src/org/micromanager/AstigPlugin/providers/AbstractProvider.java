package org.micromanager.AstigPlugin.providers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.micromanager.AstigPlugin.interfaces.PluginInterface;
import org.scijava.Context;
import org.scijava.InstantiableException;
import org.scijava.log.LogService;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;

public class AbstractProvider< K extends PluginInterface > {

	private Class<K> cl;
	private ArrayList<String> keys;
	private ArrayList<String> visibleKeys;
	private ArrayList<String> disabled;
	private HashMap<String, K> implementations;

	public AbstractProvider( final Class< K > cl ) {
		this.cl = cl;
		keys = new ArrayList<String>();
		visibleKeys = new ArrayList<String>();
		disabled = new ArrayList<String>();
		implementations = new HashMap<String, K>();
		registerModules();
	}

	@SuppressWarnings("unchecked")
	public void addModule(String className){
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			K instance = (K) classLoader.loadClass(className).newInstance();
			String key = instance.getKey();
			if(keys.contains(key)) return;
			keys.add(key);
			visibleKeys.add(key);
			implementations.put(key, instance);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public void registerModules() {
		Context context = null;
		try{
			context = new Context(LogService.class, PluginService.class);
		} catch (Exception e){
			return;
		}
		final LogService log = context.getService(LogService.class);
		final PluginService pluginService = context.getService(PluginService.class);
		final List<PluginInfo<K>> infos = pluginService.getPluginsOfType(cl);

		final Comparator<PluginInfo<K>> priorityComparator = new Comparator<PluginInfo<K>>() {
			@Override
			public int compare(final PluginInfo<K> o1, final PluginInfo<K> o2) {
				return o1.getPriority() > o2.getPriority() ? 1 : o1.getPriority() < o2.getPriority() ? -1 : 0;
			}
		};

		Collections.sort(infos, priorityComparator);

		for (final PluginInfo<K> info : infos) {
			if (!info.isEnabled()) {
				disabled.add(info.getClassName());
				continue;
			}
			try {
				final K implementation = info.createInstance();
				final String key = implementation.getKey();

				implementations.put(key, implementation);
				keys.add(key);
				if (info.isVisible()) 
					visibleKeys.add(key);

			} catch (final InstantiableException e) {
				log.error("Could not instantiate " + info.getClassName(), e);
			}
		}
	}
	
	public List< String > getKeys()
	{
		return new ArrayList<String>( keys );
	}

	public List< String > getVisibleKeys()
	{
		return new ArrayList<String>( visibleKeys );
	}

	public List< String > getDisabled()
	{
		return new ArrayList<String>( disabled );
	}

	public K getFactory( final String key )
	{
		return implementations.get( key );
	}
	
	public String echo()
	{
		final StringBuilder str = new StringBuilder();
		str.append( "Discovered modules for " + cl.getSimpleName() + ":\n" );
		str.append( "  Enabled & visible:" );
		if ( getVisibleKeys().isEmpty() )
		{
			str.append( " none.\n" );
		}
		else
		{
			str.append( '\n' );
			for ( final String key : getVisibleKeys() ){
				if (getFactory( key ) == null)
					str.append( "  - " + key + '\n' );
				else
					str.append( "  - " + key + "\t-->\t" + getFactory( key ).getName() + '\n' );
			}
		}
		str.append( "  Enabled & not visible:" );
		final List< String > invisibleKeys = getKeys();
		invisibleKeys.removeAll( getVisibleKeys() );
		if (invisibleKeys.isEmpty()) {
			str.append( " none.\n" );
		} else{
			str.append( '\n' );
			for ( final String key : invisibleKeys )
			{
				if (getFactory( key ) == null)
					str.append( "  - " + key + '\n' );
				else
					str.append( "  - " + key + "\t-->\t" + getFactory( key ).getName() + '\n' );
			}
		}
		str.append( "  Disabled:" );
		if ( getDisabled().isEmpty() )
		{
			str.append( " none.\n" );
		}
		else
		{
			str.append( '\n' );
			for ( final String cn : getDisabled() )
			{
				str.append( "  - " + cn + '\n' );
			}
		}
		return str.toString();
	}

}
