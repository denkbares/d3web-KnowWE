package de.d3web.we.kdom.visitor;

import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;


public class OWLBuilderVisitor {
	public OWLBuilderVisitor() {
		
	}

	
	public IntermediateOwlObject visit(Section s) {
	    	IntermediateOwlObject io=new IntermediateOwlObject();
		KnowWEObjectType type = s.getObjectType();		
		if (type instanceof AbstractKnowWEObjectType) {
		    AbstractKnowWEObjectType owlsec=(AbstractKnowWEObjectType)type;
		    io.merge(owlsec.getOwl(s));
		} else {			
			for (Section cur : s.getChildren()) {
			    io.merge(visit(cur));
			}
		}
		return io;
	}

}
