package de.d3web.we.ci4ke.handling;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum CIBuildManagment {
	
	INSTANCE;
	
	private Map<String,Set<Runnable>> hooks = new HashMap<String,Set<Runnable>>();
	
	public void registerOnSaveHook(String article, Runnable hook){
		if(!hooks.containsKey(article))
			hooks.put(article, new HashSet<Runnable>());
		hooks.get(article).add(hook);
	}
	
	public void deregisterOnSaveHook(){
		
	}
	
	public void deregisterAllOnSaveHooksForArticle(){
		
	}
	
	public void exeuteOnSaveHook(String article){
		for(Runnable r : hooks.get(article))
			new Thread(r).start();
	}
}
