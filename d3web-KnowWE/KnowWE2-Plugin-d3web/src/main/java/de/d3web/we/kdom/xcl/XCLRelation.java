package de.d3web.we.kdom.xcl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.kdom.condition.ComplexFinding;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.contexts.SolutionContext;
import de.d3web.we.kdom.renderer.KDomXCLRelationRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.SpecialDelegateRenderer;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.UpperOntology2;

public class XCLRelation extends DefaultAbstractKnowWEObjectType {
	
	/**
	 * Stores the KnowledgeBase-Ids from XCLRelation Sections.
	 * Can be retrieved with kdomId.
	 */
	private HashMap<String, String> knowledgeBaseIds;
	
	/**
	 * Stores an id in knowledgeBaseIds
	 * 
	 * @param kdomId
	 * @param kbId
	 */
	public void storeId(String kdomId, String kbId) {
		this.knowledgeBaseIds.put(kdomId, kbId);
	}
	
	/**
	 * Gets the KnowledgeBase-Id for a given kdomId.
	 * 
	 * @param kdomId
	 * @return
	 */
	public String getId(String kdomId) {
		return this.knowledgeBaseIds.get(kdomId);
	}
	
	@Override
	public KnowWEDomRenderer getDefaultRenderer() {
		return SpecialDelegateRenderer.getInstance();
	}

	@Override
	public void init() {
		this.childrenTypes.add(new XCLRelationWeight());
		this.childrenTypes.add(new ComplexFinding());
		this.sectionFinder = new XCLRelationSectionFinder(this);
		this.knowledgeBaseIds = new HashMap<String,String>();
	}
	
	@Override
	public KnowWEDomRenderer getRenderer() {
		return KDomXCLRelationRenderer.getInstance();
	}
	
	public static List<String> splitUnquoted(String conditionText2,
			String operatorGreaterEqual) {
		boolean quoted = false;
		List<String> parts = new ArrayList<String>();
		StringBuffer actualPart = new StringBuffer();
		for (int i = 0; i < conditionText2.length(); i++) {

			if (conditionText2.charAt(i) == '"') {
				quoted = !quoted;
			}
			if (quoted) {
				actualPart.append(conditionText2.charAt(i));
				continue;
			}
			if ((i + operatorGreaterEqual.length() <= conditionText2.length()) && conditionText2
					.subSequence(i, i + operatorGreaterEqual.length()).equals(
							operatorGreaterEqual)) {
				parts.add(actualPart.toString().trim());
				actualPart = new StringBuffer();
				i += operatorGreaterEqual.length() - 1;
				continue;
			}
			actualPart.append(conditionText2.charAt(i));

		}
		parts.add(actualPart.toString().trim());
		return parts;
	}


	class XCLRelationSectionFinder extends SectionFinder {
		public XCLRelationSectionFinder(KnowWEObjectType type) {
			super(type);
		}

		@Override
		public List<Section> lookForSections(Section tmp, Section father,
				de.d3web.we.knowRep.KnowledgeRepresentationManager kbm, KnowWEDomParseReport report,
				IDGenerator idg) {
			String text = tmp.getOriginalText();
			List<Section> result = new ArrayList<Section>();
			List<String> lines = splitUnquoted(text, ",");
			for (String string : lines) {
				if (containsData(string)) {
					int indexOf = text.indexOf(string);
					result.add(Section.createSection(this.getType(), father,
							tmp, indexOf, indexOf + string.length(), kbm,
							report, idg));
				}
			}

			return result;
		}

		private boolean containsData(String string) {
			int index = 0;
			while (index < string.length()) {
				char charAt = string.charAt(index);
				if (charAt != ' ' && charAt != '\n' && charAt != '\r'
						&& charAt != '}') {
					return true;
				}
				index++;
			}

			return false;
		}
	}

	@Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.d3web.we.dom.AbstractOWLKnowWEObjectType#getOwl(de.d3web.we.dom.Section
	 * )
	 */
	public IntermediateOwlObject getOwl(Section s) {
		IntermediateOwlObject io = new IntermediateOwlObject();
		try {
			UpperOntology2 uo = UpperOntology2.getInstance();

			URI explainsdings = uo.createlocalURI(s.getTopic() + ".."
					+ s.getId());
			URI solutionuri = ((SolutionContext) ContextManager.getInstance()
					.getContext(s, SolutionContext.CID)).getSolutionURI();
			io.addStatement(uo.createStatement(solutionuri, uo
					.createURI("isRatedBy"), explainsdings));

			URI torigin = uo.createlocalURI("Origin" + s.getTopic()
					+ solutionuri.getLocalName() + s.getId());
			io.addStatement(uo.createStatement(torigin, RDF.TYPE, uo
					.createURI("TextOrigin")));
			Literal nodeid = uo.getConnection().getValueFactory()
					.createLiteral(s.getId());
			io.addStatement(uo.createStatement(torigin,
					uo.createURI("hasNode"), nodeid));

			io.addStatement(uo.createStatement(explainsdings, RDF.TYPE, uo
					.createURI("Explains")));
			for (Section current : s.getChildren()) {
				if (current.getObjectType() instanceof ComplexFinding) {
					AbstractKnowWEObjectType handler = (AbstractKnowWEObjectType) current
							.getObjectType();
					for (URI curi : handler.getOwl(current).getLiterals()) {
						Statement state = uo.createStatement(explainsdings, uo
								.createURI("hasFinding"), curi);
						io.addStatement(state);
						handler.getOwl(current).removeLiteral(curi);
					}
					io.merge(handler.getOwl(current));
				} else if (current.getObjectType() instanceof XCLRelationWeight) {
					AbstractKnowWEObjectType handler = (AbstractKnowWEObjectType) current
							.getObjectType();
					if (handler.getOwl(current).getLiterals().size() > 0) {
						io.addStatement(uo.createStatement(explainsdings, uo
								.createURI("hasWeight"), handler
								.getOwl(current).getLiterals().get(0)));
						io.addAllStatements(handler.getOwl(current)
								.getAllStatements());
					}
				}

			}
		} catch (RepositoryException e) {
			// TODO error management?
		}
		return io;
	}

}
