package de.d3web.we.kdom.TiRex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.UpperOntology2;
import de.d3web.we.wikiConnector.KnowWEUserContext;

@Deprecated
public class TiRexHead extends DefaultAbstractKnowWEObjectType {

	private static final String REGEXP_TIREXHEAD = "<TiRex[\\w\\W]*?>";
	private static final String REGEXP_SOLUTION = "solution=\"[\\w\\W]*?\"";
	private static final String REGEXP_QUOTED = "\"([\\w\\W]*?)\"";
	
	Map<Section, String> solutionStore = new HashMap<Section, String>(); 
	
	@Override
	public List<? extends KnowWEObjectType> getAllowedChildrenTypes() {
		return null;
	}
	
	public String getSolution(Section s) {
		return solutionStore.get(s);
	}
	
	@Override
	public Collection<Section> getAllSectionsOfType() {
		return solutionStore.keySet();
	}

	
	@Override
	public KnowWEDomRenderer getRenderer() {
		return new KnowWEDomRenderer() {
			@Override
			public String render(Section sec, KnowWEUserContext user, String web, String topic) {
				String solution = null;
				solution  = ((TiRexHead)sec.getObjectType()).getSolution(sec);
				String result = "__TiRex Knowledge for solution: "+solution+"__"; 
				
				return result;
			}
		};
	}
	
	
	@Override
	public SectionFinder getSectioner() {
		return new TiRexHeadSectionFinder(this);
	}
	
	class TiRexHeadSectionFinder extends SectionFinder {
		
	
		public TiRexHeadSectionFinder(KnowWEObjectType type) {
			super(type);
		}

		@Override
		public List<Section> lookForSections(Section tmp, Section father, KnowledgeRepresentationManager mgn, KnowWEDomParseReport rep, IDGenerator idg) {
			String text = tmp.getOriginalText();
			ArrayList<Section> result = new ArrayList<Section>();
			
			Pattern p = Pattern.compile(REGEXP_TIREXHEAD);
			
			Matcher m = p.matcher(text);

			while (m.find()) {
				
				String head = m.group();
				Pattern p2 = Pattern.compile(REGEXP_SOLUTION);
				Matcher m2 = p2.matcher(head);
				String solution = father.getTopic();
				if (m2.find()) {
					String solutionAttribute = m2.group();
					Pattern p3 = Pattern.compile(REGEXP_QUOTED);
					Matcher m3 = p3.matcher(solutionAttribute);
					if(m3.find()) {
						solution = m3.group(1);
					}

				}
				Section sec = Section.createSection(this.getType(), father, tmp, m.start(), m.end(), mgn, rep, idg);
				result.add (sec);
				solutionStore.put(sec, solution);				
				SolutionContext con=new SolutionContext();
				con.setSolution(solution);UpperOntology2 uo=SemanticCore.getInstance().getUpper();
				URI solutionuri=uo.createlocalURI(solution);
				con.setSolutionURI(solutionuri);
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
		// TODO Auto-generated method stub
		
	}

}
