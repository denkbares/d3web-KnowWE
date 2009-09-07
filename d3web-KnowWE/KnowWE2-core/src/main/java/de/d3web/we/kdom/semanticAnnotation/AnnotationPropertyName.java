/**
 * 
 */
package de.d3web.we.kdom.semanticAnnotation;

import org.openrdf.model.URI;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.AllTextFinder;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.UpperOntology2;

/**
 * @author kazamatzuri
 * 
 */
public class AnnotationPropertyName extends DefaultAbstractKnowWEObjectType  {

    @Override
    public void init() {
	this.sectionFinder = new AllTextFinder(this);
    }

    @Override
    public IntermediateOwlObject getOwl(Section s) {
	IntermediateOwlObject io = new IntermediateOwlObject();
	UpperOntology2 uo = UpperOntology2.getInstance();
	String prop = s.getOriginalText();
	URI property=null;
	if (prop.equals("subClassOf") || prop.equals("type") || prop.equals("subPropertyOf")){
	    property = uo.getRDFS(prop);
	}else {
	     property = uo.createlocalURI(prop);
	}
	io.addLiteral(property); 
	return io;
    }

}
