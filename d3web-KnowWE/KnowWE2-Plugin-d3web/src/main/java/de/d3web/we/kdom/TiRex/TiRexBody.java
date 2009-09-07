package de.d3web.we.kdom.TiRex;

import java.util.Map;

import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.contexts.SolutionContext;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.kdom.xml.XMLContent;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.UpperOntology2;


public class TiRexBody extends XMLContent {

	
	@Override
	protected  void init() {
		childrenTypes.add(new TiRexParagraph());
	}
	
	@Override
	public IntermediateOwlObject getOwl(Section section) {
		IntermediateOwlObject io = new IntermediateOwlObject();
		URI solutionuri = null;
		UpperOntology2 uo = UpperOntology2.getInstance();
		SolutionContext solutioncontext = ((SolutionContext) ContextManager
				.getInstance().getContext(section, SolutionContext.CID));
		String solution;
		if (solutioncontext!=null)
			solutionuri=solutioncontext.getSolutionURI();
		if (solutioncontext == null || solutioncontext.getSolutionURI() == null) {
			
			if (section.getObjectType() instanceof AbstractXMLObjectType) {
				Map<String, String> mapFor = ((AbstractXMLObjectType) section
						.getObjectType()).getMapFor(section);
				if (mapFor != null) {
					String str = mapFor.get("solution");
					if (str != null) {
						solution = str;
						if (solutioncontext == null) {
							solutioncontext = new SolutionContext();
							solutioncontext.setSolution(solution);
							ContextManager.getInstance().attachContext(
									section.getFather(), solutioncontext);
							solutionuri = solutioncontext.getSolutionURI();
						} else {
							solutioncontext.setSolution(solution);
						}

						try {
							io.addStatement(uo.createStatement(solutionuri,
									RDF.TYPE, UpperOntology2.SOLUTION));
						} catch (RepositoryException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}

			}

		}
		try {

			for (Section current : section.getChildren()) {
				if (current.getObjectType() instanceof TiRexParagraph) {
					AbstractKnowWEObjectType handler = (AbstractKnowWEObjectType) current
							.getObjectType();
					IntermediateOwlObject cowl = handler.getOwl(current);
					for (URI curi : cowl.getLiterals()) {
						Statement state = uo.createStatement(solutionuri, uo
								.createURI("isRatedBy"), curi);
						io.addStatement(state);
						cowl.removeLiteral(curi);
						if (cowl.getOrigin(curi) != null) {
							URI torigin = uo.createlocalURI("Origin"
									+ section.getTopic() + curi.getLocalName());
							io.addStatement(uo.createStatement(torigin,
									RDF.TYPE, uo.createURI("TextOrigin")));
							Literal nodeid = uo.getConnection()
									.getValueFactory().createLiteral(
											cowl.getOrigin(curi));
							io.addStatement(uo.createStatement(torigin, uo
									.createURI("hasNode"), nodeid));
						}
					}
					io.merge(cowl);
				}

			}
		} catch (RepositoryException e) {
			// TODO error management?
		}

		return io;
	}



}
