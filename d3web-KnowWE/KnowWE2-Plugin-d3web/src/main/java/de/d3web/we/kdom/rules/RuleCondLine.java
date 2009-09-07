package de.d3web.we.kdom.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.LineBreak;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;

public class RuleCondLine extends DefaultAbstractKnowWEObjectType{

	@Override
	protected void init() {
		sectionFinder = new RuleCondLineFinder(this);
		childrenTypes.add(new LineBreak());
		childrenTypes.add(new If());
		childrenTypes.add(new RuleCondition());
	}
	
}

	class RuleCondLineFinder extends SectionFinder {

		public RuleCondLineFinder(KnowWEObjectType type) {
			super(type);
		}

		@Override
		public List<Section> lookForSections(Section text, Section father,
				KnowledgeRepresentationManager kbm, KnowWEDomParseReport report, IDGenerator idg) {
			
			//String lineRegex = "(IF|WENN).+(?=(\\z|THEN|DANN))";
			String lineRegex = "(IF|WENN).+";
			Pattern linePattern = Pattern.compile( lineRegex , Pattern.DOTALL);
			
	        Matcher tagMatcher = linePattern.matcher( text.getOriginalText() );		
	        ArrayList<Section> resultRegex = new ArrayList<Section>();
	        
	        while (tagMatcher.find()) {
	        	resultRegex.add(Section.createSection(this.getType(), father, text, tagMatcher.start(), tagMatcher.end(), kbm, report, idg));
			}
			return resultRegex;
		}
}


	
