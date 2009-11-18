package de.d3web.we.kdom.dashTree;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class SubTree extends DefaultAbstractKnowWEObjectType{
	
	@Override
	protected void init() {
		this.sectionFinder = new SubTreeFinder();
		this.childrenTypes.add(new Root());
		this.childrenTypes.add(this);
	}
	
	public static int getLevel(Section s) {
		Section root = s.findChildOfType(Root.class);
		if(root == null) return 0;
		return Root.getLevel(root)+1;
	}
	
	
	class SubTreeFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section father) {
			
			int level = 0;
			
			KnowWEObjectType fatherType = father.getObjectType();
			if(fatherType instanceof SubTree) {
				level = getLevel(father);
			}
			
			String dashesPrefix = "";
			for(int i=0; i < level; i++) {
				dashesPrefix += "-";
			}
			
			ArrayList<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			Matcher m = Pattern.compile("^"+dashesPrefix+"[^-]+", Pattern.MULTILINE).matcher(text);
			int lastStart = -1;
			while (m.find()) {
				String finding = m.group();
				if(lastStart > -1) {
					String found = text.substring(lastStart,m.start()+1);
					result.add(new SectionFinderResult(lastStart,m.start()));
				}
				lastStart = m.start();
				
				
			}
			if(lastStart > -1 ) {
				result.add(new SectionFinderResult(lastStart,text.length()));
			}
			return result;
			
			
		}
		
	}

}
