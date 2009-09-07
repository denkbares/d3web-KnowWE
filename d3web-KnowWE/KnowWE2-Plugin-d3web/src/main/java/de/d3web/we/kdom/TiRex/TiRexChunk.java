package de.d3web.we.kdom.TiRex;

import java.util.ArrayList;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.UpperOntology2;

public class TiRexChunk extends DefaultAbstractKnowWEObjectType {
	
	public TiRexChunk() {
		childrenTypes.add(new TiRexQuestion());
		childrenTypes.add(new TiRexAnswer());
	}
	
	
	@Override
	public IntermediateOwlObject getOwl(Section section) {

		UpperOntology2 uo = UpperOntology2.getInstance();
		IntermediateOwlObject io = new IntermediateOwlObject();
		boolean valid = true;
		String comparator = "=";
		Section qsection = section.findSuccessor(TiRexQuestion.class);
		Section asection = section.findSuccessor(TiRexAnswer.class);

		if (qsection == null || asection == null)
			return io;
		URI answeruri = asection.getObjectType().getOwl(asection).getLiterals()
				.get(0);
		URI compuri = uo.getComparator(comparator);
		URI questionuri = qsection.getObjectType().getOwl(qsection)
				.getLiterals().get(0);

		URI literalinstance = uo.createlocalURI(section.getTopic() + ".."
				+ section.getId() + ".." + questionuri.getLocalName()
				+ comparator + answeruri.getLocalName());

		URI explainsdings = uo.createlocalURI(section.getTopic() + ".."
				+ section.getId());
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
			io.addStatement(uo.createStatement(explainsdings, RDF.TYPE, uo
					.createURI("Explains")));
			io.addStatement(uo.createStatement(explainsdings, uo
					.createURI("hasFinding"), literalinstance));

		} catch (RepositoryException e) {
			// TODO real error handling
		}
		io.addAllStatements(slist);
		io.addLiteral(literalinstance);
		io.setOrigin(literalinstance, section.getId());

		return io;
	}


	@Override
	protected void init() {
		// TODO Auto-generated method stub
		
	}

}
