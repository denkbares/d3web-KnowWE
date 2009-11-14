package de.d3web.we.hermes.kdom.conceptMining;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.hermes.kdom.TimeEventContext;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.semanticAnnotation.SemanticAnnotationObject;
import de.d3web.we.kdom.semanticAnnotation.SemanticAnnotationProperty;
import de.d3web.we.kdom.semanticAnnotation.SimpleAnnotation;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.PropertyManager;
import de.d3web.we.module.semantic.owl.UpperOntology;

public class AnnotationObjectInTimeEvent extends SemanticAnnotationObject {

	@Override
	public IntermediateOwlObject getOwl(Section s) {
		UpperOntology uo = UpperOntology.getInstance();
		IntermediateOwlObject io = new IntermediateOwlObject();
		List<Section> childs = s.getChildren();
		URI prop = null;
		URI stringa = null;
		for (Section cur : childs) {
			if (cur.getObjectType().getClass().equals(SemanticAnnotationProperty.class)) {
				prop = ((SemanticAnnotationProperty) cur.getObjectType()).getOwl(cur)
						.getLiterals().get(0);
			} else if (cur.getObjectType().getClass().equals(
					SimpleAnnotation.class)) {
				stringa = ((SimpleAnnotation) cur.getObjectType()).getOwl(cur)
						.getLiterals().get(0);
			}

		}

		boolean validprop = false;
		if (prop != null) {
			validprop = PropertyManager.getInstance().isValid(prop);
			io.setBadAttribute(prop.getLocalName());
		}
		io.setValidPropFlag(validprop);
		if (!validprop) {
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
					"invalid property: " + prop.getLocalName());
		}

		if (prop != null && validprop && stringa != null) {
			TimeEventContext tURI = (TimeEventContext) ContextManager
					.getInstance().getContext(s, TimeEventContext.CID);
			URI TEURI = null;
			if (tURI == null) {
				return io;
			} else {
				TEURI = tURI.getTimeEventURI();
			}

			try {

				ArrayList<Statement> slist = new ArrayList<Statement>();
				slist.add(uo.getHelper().createStatement(TEURI, prop, stringa));

				io.addAllStatements(slist);
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return io;
	}
}
