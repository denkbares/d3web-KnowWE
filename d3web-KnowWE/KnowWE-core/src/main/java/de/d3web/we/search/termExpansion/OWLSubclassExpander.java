package de.d3web.we.search.termExpansion;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

import de.d3web.we.module.semantic.owl.UpperOntology;
import de.d3web.we.search.SearchTerm;
import de.d3web.we.utils.SPARQLUtil;

public class OWLSubclassExpander implements SearchTermExpander{

	@Override
	public List<SearchTerm> expandSearchTerm(SearchTerm t) {
		
		List<SearchTerm> results = new ArrayList<SearchTerm>();
		
		String name = t.getTerm();
		URI termURI = UpperOntology.getInstance().getHelper().createlocalURI(name);
		
		// adds SearchTerms for each found subclass of the class for t
		expandSubclasses(t, results, termURI, 0.9);
		
		// adds SearchTerms for each found superclass of the class for t
		expandSuperclasses(t, results, termURI, 0.5);
		
		return results;
	}
	
	private void expandSuperclasses(SearchTerm t, List<SearchTerm> results,
			URI termURI, double discountFactor) {
		TupleQueryResult findSubClasses = SPARQLUtil.findSuperClasses(termURI);
		if(findSubClasses != null) {
			try {
				while(findSubClasses.hasNext()) {
					BindingSet set = findSubClasses.next();
					String subClassName = set.getBinding("x").getValue().stringValue();
					
					subClassName = URLDecoder.decode(subClassName, "UTF-8");
					
					results.add(new SearchTerm(subClassName.substring(subClassName.indexOf("#")+1), t.getImportance()*discountFactor));
				}
			} catch (QueryEvaluationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void expandSubclasses(SearchTerm t, List<SearchTerm> results,
			URI termURI, double discountFactor) {
		TupleQueryResult findSubClasses = SPARQLUtil.findSubClasses(termURI);
		if(findSubClasses != null) {
			try {
				while(findSubClasses.hasNext()) {
					BindingSet set = findSubClasses.next();
					String subClassName = set.getBinding("x").getValue().stringValue();
					
					subClassName = URLDecoder.decode(subClassName, "UTF-8");
					
					results.add(new SearchTerm(subClassName.substring(subClassName.indexOf("#")+1), t.getImportance()*discountFactor));
				}
			} catch (QueryEvaluationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
