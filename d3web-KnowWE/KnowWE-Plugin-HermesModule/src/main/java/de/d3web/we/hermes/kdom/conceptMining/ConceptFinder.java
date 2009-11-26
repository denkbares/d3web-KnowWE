package de.d3web.we.hermes.kdom.conceptMining;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.hermes.util.TimeEventSPARQLUtils;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public abstract class ConceptFinder extends SectionFinder {
	
	
	private static final String INSTANCE_SPARQL = "SELECT ?x WHERE { ?x rdf:type lns:CLASS .}";
		
	
	
	protected abstract String[] getClassNames();
	
	private Set<String> objectNames = null;
	
	@Override
	public List<SectionFinderResult> lookForSections(String arg0, Section arg1) {
		
		
		String text = arg0;
		
		//if(objectNames == null) {
			fillObjectNameList();
		//}
	
		List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
		
		for (String objectName : objectNames) {
			int index = text.indexOf(objectName); 
			
			
			if(index == -1) continue;
			
			result.add(new SectionFinderResult(index, index + objectName.length()));	
		}
		
			
		return result;
		
	}

	private void fillObjectNameList() {
		objectNames = new HashSet<String>();
		
		String[] classes = this.getClassNames();
		for (String clazz : classes) {
			String query = INSTANCE_SPARQL.replace("CLASS", clazz);
			
			TupleQueryResult result = TimeEventSPARQLUtils.executeQuery(query);
			
			try {
				while (result.hasNext()) {
					BindingSet set = result.next();

					
					Binding tB = set.getBinding("x");
					String name = tB.getValue().stringValue();
					
					try {
						name = URLDecoder.decode(name, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					objectNames.add(name.substring(name.lastIndexOf("#")+1));
					
				}
			} catch (QueryEvaluationException e) {
				// moo
				e.printStackTrace();
			} finally {
				try {
					result.close();
				} catch (QueryEvaluationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
	}

}
