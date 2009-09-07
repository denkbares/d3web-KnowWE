package de.d3web.we.kdom.contexts;

import org.openrdf.model.URI;

import de.d3web.we.module.semantic.owl.UpperOntology2;

public class SolutionContext extends Context {
    	private URI soluri;
	public final static String CID="SOLUTIONCONTEXT";
	
	public void setSolution(String sol){
		attributes.put("solution", sol);
	}

	public void setSolutionURI(URI solutionuri){
	    soluri=solutionuri;
	}
	public String getSolution() {
		return attributes.get("solution");
	}
	
	public URI getSolutionURI(){
	    if (soluri==null){
		UpperOntology2 uo=UpperOntology2.getInstance();
		soluri=uo.createlocalURI(getSolution());
	    }
	    return soluri;
	}

	@Override
	public String getCID() {		
		return CID;
	}
}
