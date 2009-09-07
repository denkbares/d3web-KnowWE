package de.d3web.we.kdom.condition;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Annotation.Finding;
import de.d3web.we.kdom.sectionFinder.AllTextFinder;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.UpperOntology2;

public class Conjunct extends DefaultAbstractKnowWEObjectType {

    @Override
    public void init() {
    	this.childrenTypes.add(new Finding());
    	this.sectionFinder = new AllTextFinder(this);
    }

    @Override /* (non-Javadoc)
	* @see de.d3web.we.dom.AbstractOWLKnowWEObjectType#getOwl(de.d3web.we.dom.Section)
	*/	
	public IntermediateOwlObject getOwl(Section s) {
	    IntermediateOwlObject io=new IntermediateOwlObject();
	    try {
	    UpperOntology2 uo=UpperOntology2.getInstance();
	    URI compositeexpression=uo.createlocalURI(s.getTopic()+".."+s.getId());
	    io.addStatement(uo.createStatement(compositeexpression,RDF.TYPE,uo.createURI("Conjunction")));	    
	    io.addLiteral(compositeexpression);
	    for (Section current:s.getChildren()){
		if (current.getObjectType() instanceof AbstractKnowWEObjectType) {
		    AbstractKnowWEObjectType handler=(AbstractKnowWEObjectType) current.getObjectType();
		    IntermediateOwlObject iohandler = handler.getOwl(current);		    
		    for (URI curi:iohandler.getLiterals()){
			Statement state=uo.createStatement(compositeexpression,uo.createURI("hasConjuncts"),curi);
			io.addStatement(state);
			iohandler.removeLiteral(curi);
		    }		    
		    io.merge(iohandler);
		}
	    }} catch (RepositoryException e){
		//TODO error management?
	    }
	    return io;
	}

}
