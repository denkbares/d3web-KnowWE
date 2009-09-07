package de.d3web.we.kdom.semanticAnnotation;

import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.core.SemanticCore;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.contexts.SolutionContext;
import de.d3web.we.kdom.sectionFinder.AllTextFinder;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.UpperOntology2;

public class AnnotationKnowledgeSliceSubject extends DefaultAbstractKnowWEObjectType {


	class AnnotationKnowledgeSliceSubjectSectionFinder extends SectionFinder {
		public AnnotationKnowledgeSliceSubjectSectionFinder(
				KnowWEObjectType type) {
			super(type);
		}

		@Override
		public List<Section> lookForSections(Section tmp, Section father, KnowledgeRepresentationManager mgn, KnowWEDomParseReport rep, IDGenerator idg) {
			String text = tmp.getOriginalText();
			if(father.hasRightSonOfType(AnnotationRelationOperator.class, text)) {				
				List<Section> sections = new AllTextFinder(this.getType()).lookForSections(tmp, father,null,rep, idg);
				SolutionContext con=new SolutionContext();
				con.setSolution(tmp.getOriginalText().trim());
				ContextManager.getInstance().attachContext(sections.get(0).getFather(),con);
				return sections;
			}
			return null;
		}
	}
	
	@Override
	public IntermediateOwlObject getOwl(Section section) {
	    IntermediateOwlObject io =new IntermediateOwlObject();		
		SolutionContext sol=(SolutionContext)ContextManager.getInstance().getContext(section, SolutionContext.CID);
		String solution=sol!=null?sol.getSolution():null;
		UpperOntology2 uo=SemanticCore.getInstance().getUpper();
		try {
			URI solutionuri=uo.createlocalURI(solution);
			sol.setSolutionURI(solutionuri);
			io.addStatement(uo.createStatement(solutionuri,RDF.TYPE, UpperOntology2.SOLUTION));
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return io;
	}
	
	@Override
	protected void init() {
		this.sectionFinder = new AnnotationKnowledgeSliceSubjectSectionFinder(this);
		
	}
}
