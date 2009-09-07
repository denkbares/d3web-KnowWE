package de.d3web.we.kdom.contexts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import de.d3web.we.kdom.Section;

/**
 * A context is a common environment in which a section-node operates. A context is inherited 
 * from the parents. Sections can have multiple contexts. 
 * 
 * 
 * @author Fabian Haupt
 *
 */
public class ContextManager {
	private static ContextManager me;
	private static HashMap<Section, HashMap<String,Context>> contextmap;	
	private ContextManager(){
		contextmap=new HashMap<Section, HashMap<String,Context>>();
		
	}
	
	/**
	 * Attaches a context to a specific section.
	 * 
	 * @param section
	 * @param context
	 */
	public void attachContext(Section section, Context context){
		HashMap<String,Context> current=contextmap.get(section);
		if (current==null){
			current=new HashMap<String,Context>();
			current.put(context.getCID(),context);
			contextmap.put(section, current);
		} else {
				current.put(context.getCID(),context);
		}		
	}
	
	/**
	 * returns a set of all contextids a section belongs too, including the inherited ones
	 * @param section
	 * @return
	 */
	public Set<String> getContexts(Section section){
		Set<String> contextlist=new HashSet<String>();
		contextlist.addAll(contextmap.get(section).keySet());
		if (section.getFather()!=null){
			contextlist.addAll(contextmap.get(section.getFather()).keySet());
		}
		return contextlist;
	}
	
	/**
	 * returns a contextmanagerinstance
	 * @return
	 */
	public static synchronized ContextManager getInstance(){
		if (me==null){
			me = new ContextManager();
		}
		return me;
	}
	
	/**
	 * prevent cloning
	 */
	 @Override
	public Object clone()
		throws CloneNotSupportedException
	  {
	    throw new CloneNotSupportedException(); 	   
	  }
	
	/**
	 * returns a context of a section. looks for inherited contexts too
	 * @param section
	 * @param contextid
	 * @return
	 */
	public Context getContext(Section section, String contextid){
		Context erg=contextmap.get(section)!=null?contextmap.get(section).get(contextid):null;
		return erg!=null?erg:section.getFather()!=null?getContext(section.getFather(),contextid):null;				
	}

}
