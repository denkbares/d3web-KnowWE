package de.d3web.we.hermes.kdom.conceptMining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.core.semantic.IntermediateOwlObject;
import de.d3web.we.core.semantic.OwlHelper;
import de.d3web.we.core.semantic.PropertyManager;
import de.d3web.we.core.semantic.UpperOntology;
import de.d3web.we.hermes.kdom.TimeEventContext;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.SimpleMessageError;
import de.d3web.we.kdom.sectionFinder.AllBeforeTypeSectionFinder;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.kdom.semanticAnnotation.SemanticAnnotationObject;
import de.d3web.we.kdom.semanticAnnotation.SemanticAnnotationProperty;
import de.d3web.we.kdom.semanticAnnotation.SemanticAnnotationSubject;
import de.d3web.we.kdom.semanticAnnotation.SimpleAnnotation;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;

public class AnnotationObjectInTimeEvent extends SemanticAnnotationObject {

	@Override
	public void init() {
		SemanticAnnotationProperty propType = new SemanticAnnotationProperty();
		SemanticAnnotationSubject subject = new SemanticAnnotationSubject();
		subject.setSectionFinder(new AllBeforeTypeSectionFinder(propType));
		this.childrenTypes.add(propType);
		this.childrenTypes.add(subject);
		this.childrenTypes.add(new SimpleAnnotation());
		this.sectionFinder = new AllTextSectionFinder();
		this.addSubtreeHandler(new AnnotationObjectInTimeEventSubTreeHandler());
	}
	
	private class AnnotationObjectInTimeEventSubTreeHandler implements
			SubtreeHandler {

		@Override
		public Collection<KDOMReportMessage> reviseSubtree(KnowWEArticle article, Section s) {
			List<KDOMReportMessage> msgs = new ArrayList<KDOMReportMessage>();
			UpperOntology uo = UpperOntology.getInstance();
			IntermediateOwlObject io = new IntermediateOwlObject();
			List<Section> childs = s.getChildren();
			URI prop = null;
			URI stringa = null;
			for (Section cur : childs) {
				if (cur.getObjectType().getClass().equals(
						SemanticAnnotationProperty.class)) {
					prop = ((IntermediateOwlObject) KnowWEUtils
							.getStoredObject(cur, OwlHelper.IOO)).getLiterals()
							.get(0);
				} else if (cur.getObjectType().getClass().equals(
						SimpleAnnotation.class)) {
					stringa = ((IntermediateOwlObject) KnowWEUtils
							.getStoredObject(cur, OwlHelper.IOO)).getLiterals()
							.get(0);
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
					KnowWEUtils.storeSectionInfo(s, OwlHelper.IOO, io);
					return msgs;
				} else {
					TEURI = tURI.getTimeEventURI();
				}

				try {

					ArrayList<Statement> slist = new ArrayList<Statement>();
					slist.add(uo.getHelper().createStatement(TEURI, prop,
							stringa));

					io.addAllStatements(slist);
				} catch (RepositoryException e) {
					msgs.add(new SimpleMessageError(e.getMessage()));
				}
			}
			KnowWEUtils.storeSectionInfo(s, OwlHelper.IOO, io);
			return msgs;
		}

	}
}
