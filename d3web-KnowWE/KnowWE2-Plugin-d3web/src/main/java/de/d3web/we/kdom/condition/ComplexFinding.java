package de.d3web.we.kdom.condition;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.SpecialDelegateRenderer;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.UpperOntology2;

public class ComplexFinding extends DefaultAbstractKnowWEObjectType {

	@Override
	public KnowWEDomRenderer getDefaultRenderer() {
		return SpecialDelegateRenderer.getInstance();
	}

	@Override
	protected void init() {
		this.childrenTypes.add(new OrOperator());
		this.sectionFinder = new ComplexFindingANTLRSectionCreator(this);
		this.childrenTypes.add(new Disjunct());

	}
	
	@Override
	public KnowWEDomRenderer getRenderer() {
		return FontColorRenderer.getRenderer(FontColorRenderer.COLOR5);
	}

	@Override /* (non-Javadoc)
	* @see de.d3web.we.dom.AbstractOWLKnowWEObjectType#getOwl(de.d3web.we.dom.Section)
	*/	
	public IntermediateOwlObject getOwl(Section s) {
	    IntermediateOwlObject io=new IntermediateOwlObject();
	    try {
	    UpperOntology2 uo=UpperOntology2.getInstance();
	    URI complexfinding=uo.createChildOf(uo.createURI("ComplexFinding"),uo.createlocalURI(s.getTopic()+".."+s.getId()));
	    io.addLiteral(complexfinding);
	    for (Section current:s.getChildren()){
		if (current.getObjectType() instanceof AbstractKnowWEObjectType) {
		    AbstractKnowWEObjectType handler=(AbstractKnowWEObjectType) current.getObjectType();
		    IntermediateOwlObject iohandler = handler.getOwl(current);
		    for (URI curi:iohandler.getLiterals()){
			Statement state=uo.createStatement(complexfinding,uo.createURI("hasDisjuncts"),curi);
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
