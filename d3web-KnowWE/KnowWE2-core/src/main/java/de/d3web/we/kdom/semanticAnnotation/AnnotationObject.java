/**
 * 
 */
package de.d3web.we.kdom.semanticAnnotation;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.contexts.SolutionContext;
import de.d3web.we.kdom.sectionFinder.AllTextFinder;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.PropertyManager;
import de.d3web.we.module.semantic.owl.UpperOntology2;

/**
 * @author kazamatzuri
 * 
 */
public class AnnotationObject extends DefaultAbstractKnowWEObjectType {

	@Override
	public void init() {
		this.childrenTypes.add(new AnnotationProperty());
		this.childrenTypes.add(new AnnotationSubject());
		this.childrenTypes.add(new SimpleAnnotation());
		this.sectionFinder = new AllTextFinder(this);
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public IntermediateOwlObject getOwl(Section s) {
		UpperOntology2 uo = UpperOntology2.getInstance();
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
			SolutionContext sol = (SolutionContext) ContextManager
					.getInstance().getContext(s, SolutionContext.CID);
			URI soluri = sol.getSolutionURI();
			try {
				if (PropertyManager.getInstance().isRDFS(prop)) {
					io.addStatement(uo.createStatement(soluri, prop, stringa));
				} else {
					IntermediateOwlObject tempio = PropertyManager
							.getInstance().createProperty(soluri, prop,
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
