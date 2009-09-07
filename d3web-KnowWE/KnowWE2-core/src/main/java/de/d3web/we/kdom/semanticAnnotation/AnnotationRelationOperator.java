package de.d3web.we.kdom.semanticAnnotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.kdom.contexts.AnnotationContext;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.SpecialDelegateRenderer;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;

public class AnnotationRelationOperator extends DefaultAbstractKnowWEObjectType {

	private HashMap<Section, String> opstore;
	private static AnnotationRelationOperator me;

	private AnnotationRelationOperator() {
		opstore = new HashMap<Section, String>();
	}

	public static synchronized AnnotationRelationOperator getInstance() {
		if (me == null) {
			me = new AnnotationRelationOperator();
		}
		return me;
	}

	/**
	 * prevent cloning
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	@Override
	public KnowWEDomRenderer getDefaultRenderer() {
		return SpecialDelegateRenderer.getInstance();
	}


	private class AnnotationPropertyFinder extends SectionFinder {
		private String pattern;
		AnnotationRelationOperator type;

		public AnnotationPropertyFinder(AnnotationRelationOperator type) {
			super(type);
			this.pattern = "[\\w\\W]*::";
			this.type = type;
		}

		@Override
		public List<Section> lookForSections(Section text, Section father,
				KnowledgeRepresentationManager mgn, KnowWEDomParseReport rep,
				IDGenerator idg) {
			ArrayList<Section> result = new ArrayList<Section>();
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(text.getOriginalText());
			Section newsection = null;
			while (m.find()) {
				newsection = Section.createSection(this.getType(), father,
						text, m.start(), m.end(), mgn, rep, idg);
				if (newsection != null)
					result.add(newsection);
				break;
			}
			if (newsection != null) {
				String prop = text.getOriginalText().substring(m.start(),
						m.end()).replaceAll("::", "").trim();
				type.setOperator(newsection, prop);
				AnnotationContext con = new AnnotationContext(prop);
				ContextManager.getInstance().attachContext(
						father.getFather().getFather(), con);
			}
			return result;
		}
	}

	public void setOperator(Section sec, String op) {
		opstore.put(sec, op);
	}

	public String getOperator(Section sec) {
		return opstore.get(sec);
	}


	@Override
	protected void init() {
		this.sectionFinder = new AnnotationPropertyFinder(this);
		
	}

}
