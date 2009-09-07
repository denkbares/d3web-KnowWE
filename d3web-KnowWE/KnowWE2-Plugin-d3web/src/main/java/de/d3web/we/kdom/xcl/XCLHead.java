package de.d3web.we.kdom.xcl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.SpecialDelegateRenderer;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.UpperOntology2;

public class XCLHead extends DefaultAbstractKnowWEObjectType {
	
	Map<Section, String> solutionStore = new HashMap<Section, String>(); 

	@Override
	public KnowWEDomRenderer getDefaultRenderer() {
		return SpecialDelegateRenderer.getInstance();
	}

	
	
	class XCLHeadSectionFinder extends SectionFinder{
		public XCLHeadSectionFinder(KnowWEObjectType type) {
			super(type);
		}

		@Override
		public List<Section> lookForSections(Section tmp, Section father, KnowledgeRepresentationManager kbm, KnowWEDomParseReport report, IDGenerator idg) {
			String text = tmp.getOriginalText();
			if(text.length() == 0) return null;
			List<Section> result = new ArrayList<Section>();
			
			int start = 0;
			while(text.charAt(start) == ' ' || text.charAt(start) == '\n' || text.charAt(start) == '\r') {
				start++;
				if(start == text.length()) break;
			}
			int end=text.indexOf('{');
			Section s = Section.createSection(this.getType(), father, tmp, start,end, kbm, report, idg);			
			
			if(s != null && start <= end) {
			    String solution=text.substring(start,end).trim();
				result.add(s);				
				solutionStore.put(s, solution);
				SolutionContext con=new SolutionContext();
				con.setSolution(solution);
				ContextManager.getInstance().attachContext(father, con);
			}
			
			return result;
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
			io.addStatement(uo.createStatement(solutionuri,RDF.TYPE, UpperOntology2.SOLUTION));
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return io;
	}



	@Override
	protected void init() {
		this.sectionFinder = new XCLHeadSectionFinder(this);
		
	}

}
