package de.d3web.we.hermes.kdom.conceptMining;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.hermes.kdom.TimeEventContext;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.semanticAnnotation.AnnotationObject;
import de.d3web.we.kdom.semanticAnnotation.AnnotationProperty;
import de.d3web.we.kdom.semanticAnnotation.SimpleAnnotation;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.PropertyManager;
import de.d3web.we.module.semantic.owl.UpperOntology;

public class AnnotationObjectInTimeEvent extends AnnotationObject {

	
	@Override
	public IntermediateOwlObject getOwl(Section s) {
		UpperOntology uo = UpperOntology.getInstance();
		IntermediateOwlObject io = new IntermediateOwlObject();
		List<Section> childs = s.getChildren();
		URI prop = null;
		URI stringa = null;
		for (Section cur : childs) {
			if (cur.getObjectType().getClass().equals(AnnotationProperty.class)) {
				prop = ((AnnotationProperty) cur.getObjectType()).getOwl(cur)
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
		if (!validprop){
		    Logger.getLogger(this.getClass().getName()).log(
				Level.WARNING,
				"invalid property: "+prop.getLocalName());
		}

		if (prop != null && validprop && stringa != null) {
			TimeEventContext tURI = (TimeEventContext) ContextManager
					.getInstance().getContext(s, TimeEventContext.CID);
			URI soluri = null;
			if(tURI == null) {
				System.out.println("TE URI not found");
				return io;
			}else {
				soluri = tURI.getTimeEventURI();
				System.out.println("FOUND!!!!!");
			}
			
			try {
				if (PropertyManager.getInstance().isRDFS(prop)) {
					io.addStatement(uo.getHelper().createStatement(soluri, prop, stringa));
				} else if (PropertyManager.getInstance().isRDF(prop)) {
					io.addStatement(uo.getHelper().createStatement(soluri, prop, stringa));
				}
				else {
					IntermediateOwlObject tempio = UpperOntology.getInstance().getHelper().createProperty(soluri, prop,
									stringa, s);
					io.merge(tempio);
				}
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return io;
	}
}
