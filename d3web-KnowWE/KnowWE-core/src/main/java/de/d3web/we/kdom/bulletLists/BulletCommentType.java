package de.d3web.we.kdom.bulletLists;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;


public class BulletCommentType extends  DefaultAbstractKnowWEObjectType {

	@Override
	public void init() {
		this.sectionFinder = new BulletCommentFinder();
		this.setCustomRenderer(new CommentRenderer());
	}
	
	
	class BulletCommentFinder extends SectionFinder { 

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section father, KnowWEObjectType type) {
			ArrayList<SectionFinderResult> result =  new ArrayList<SectionFinderResult>();;
			if(text.contains("//")) {
				
			    // be sure the comment is in a quoted region   
				int index = text.indexOf("//");
				int quotesBefore = countQuotes(text.substring(0, index));
				int quotesAfter = countQuotes(text.substring(index, text.length()));
				
				if(! (quotesBefore == 1 && quotesAfter >= 1 )) {
					result.add(new SectionFinderResult(index, text.length()));
				}
					
			}
			return result;
		}

		private int countQuotes(String text) {
			String lineRegex = "\"";
			Pattern linePattern = Pattern.compile(lineRegex);
			
	        Matcher tagMatcher = linePattern.matcher(text);		
	        int count = 0;
	        while (tagMatcher.find()) {
	        	count++;
			}
			return count;
		}
		
	}
}
