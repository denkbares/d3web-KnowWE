package de.d3web.we.kdom.Annotation;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.filter.TypeSectionFilter;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.UpperOntology2;

public class Finding extends DefaultAbstractKnowWEObjectType {

 @Override
    public void init() {
    	this.childrenTypes.add(new FindingComparator());
    	this.childrenTypes.add(new FindingQuestion());
    	this.childrenTypes.add(new FindingAnswer());
    	this.sectionFinder = new FindingSectionFinder(this);
    }
    
	@Override
	public KnowWEDomRenderer getRenderer() {
		return FontColorRenderer.getRenderer(FontColorRenderer.COLOR3);
	}



    @Override
    public IntermediateOwlObject getOwl(Section section) {
	UpperOntology2 uo = UpperOntology2.getInstance();
	IntermediateOwlObject io = new IntermediateOwlObject();
	try {
	Section csection = section.getChildren(
		new TypeSectionFilter(
			new FindingComparator().getName())).get(0);
	String comparator = ((FindingComparator) csection
		.getObjectType()).getComparator(csection);

	Section qsection = section.getChildren(
		new TypeSectionFilter(
			new FindingQuestion().getName())).get(0);
	String question = ((FindingQuestion) qsection
		.getObjectType()).getQuestion(qsection);

	Section asection = section.getChildren(
		new TypeSectionFilter(
			new FindingAnswer().getName())).get(0);
	String answer = ((FindingAnswer) asection
		.getObjectType()).getAnswer(asection);
	
	URI compuri = uo.getComparator(comparator);
	URI questionuri = uo.createlocalURI(question);
	URI answeruri = uo.createlocalURI(answer);
	URI literalinstance = uo.createlocalURI(section.getTopic()+".."+section.getId() + ".."
		+ question + comparator + answer);
	
	
	ArrayList<Statement> slist = new ArrayList<Statement>();
	try {
	    slist.add(uo.createStatement(literalinstance, RDF.TYPE, uo
		    .createURI("Literal")));
	    slist.add(uo.createStatement(literalinstance, uo
		    .createURI("hasInput"), questionuri));
	    slist.add(uo.createStatement(literalinstance, uo
		    .createURI("hasComparator"), compuri));
	    slist.add(uo.createStatement(literalinstance, uo
		    .createURI("hasValue"), answeruri));
	} catch (RepositoryException e) {
	    e.printStackTrace();
	}
		io.addAllStatements(slist);
		io.addLiteral(literalinstance);
	} catch (IndexOutOfBoundsException e){
	    Logger.getLogger(this.getName()).log(Level.WARNING,"Finding without subsections");
	}
		
	return io;
    }

}
