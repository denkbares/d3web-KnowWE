/**
 * 
 */
package de.d3web.we.kdom.sparql;

import java.util.HashMap;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.xml.XMLContent;

/**
 * @author kazamatzuri
 *
 */
public class SparqlContent extends XMLContent{
    
    private HashMap<Section, String> queries;
    
	public void addQuery(Section s, String str){
		queries.put(s, str);
	}
	
	public HashMap<Section, String>getQueries(){
		return queries;
	}
	
	@Override
	public void init() {
    	    queries=new HashMap<Section, String>();
    	    this.setCustomRenderer(SparqlDelegateRenderer.getInstance());
    	}
	public String getQuery() {
		return queries.get(this);
	}
}
