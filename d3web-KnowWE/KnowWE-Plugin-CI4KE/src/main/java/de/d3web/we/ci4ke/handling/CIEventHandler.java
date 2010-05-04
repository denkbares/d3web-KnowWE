package de.d3web.we.ci4ke.handling;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.event.EventListener;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;

public class CIEventHandler implements EventListener {

	private Map<String, Set<Runnable>> hooks;
	
	private static final CIEventHandler INSTANCE = new CIEventHandler();
	
	private CIEventHandler() {
		super();
		hooks = new TreeMap<String, Set<Runnable>>();
		
//		registerHook("BLA", new Runnable(){
//			@Override
//			public void run() {
//				Logger.getLogger(CIEventHandler.class.getName()).log(
//						Level.INFO, " ======= HelloWorld from a Thread!!! ======= ");
//			}
//		});
	}
	
	public static CIEventHandler getInstance() { return INSTANCE; }
	
	public boolean registerHook(String articleTitle, Runnable hook) {
		
		if(!hooks.containsKey(articleTitle)){
			TreeSet<Runnable> set = new TreeSet<Runnable>();
			set.add(hook);
			hooks.put(articleTitle, set);
			return true;
		} else {
			Set<Runnable> set = hooks.get(articleTitle);
			if(set.contains(hook))
				return false;
			else {
				set.add(hook);
				hooks.put(articleTitle, set);
				return true;
			}
		}
	}
	
	@Override
	public String[] getEvents() {
		String[] ret = {KnowWEEnvironment.EVENT_ARTICLE_CREATED};
		return ret;
	}

	@Override
	public void notify(String username, Section<? extends KnowWEObjectType> s,
			String eventName) {
		
		if(eventName.equals(KnowWEEnvironment.EVENT_ARTICLE_CREATED)) {
			if(s.getClass().equals(KnowWEArticle.class) &&
					hooks.containsKey(s.getId())) {
				Set<Runnable> set = hooks.get(s.getId());
				for(Runnable r : set)
					new Thread(r).run();
			}
		}
	}
}
