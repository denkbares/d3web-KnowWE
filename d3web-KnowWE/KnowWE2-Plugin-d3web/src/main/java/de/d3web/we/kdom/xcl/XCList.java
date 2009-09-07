package de.d3web.we.kdom.xcl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.SpecialDelegateRenderer;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;

public class XCList extends DefaultAbstractKnowWEObjectType {

	@Override
	public KnowWEDomRenderer getDefaultRenderer() {
		return SpecialDelegateRenderer.getInstance();
	}


	
	@Override
	public void init() {
		this.sectionFinder = new XCListSectioner(this);
		childrenTypes.add(new XCLHead());
		childrenTypes.add(new XCLTail());
		childrenTypes.add(new XCLBody());
	}
	
	
	class XCListSectioner extends SectionFinder {
		public XCListSectioner(KnowWEObjectType type) {
			super(type);
		}

		@Override
		public List<Section> lookForSections(Section tmp, Section father,
				KnowledgeRepresentationManager kbm, KnowWEDomParseReport report, IDGenerator idg) {
			List<Section> matches = new ArrayList<Section>();
			
			Pattern p = Pattern.compile("(\\s*?^\\s*?$\\s*|\\s*?\\Z|\\s*?<[/]?includedFrom[^>]*?>\\s*?)", Pattern.MULTILINE);
			
				Matcher m = p.matcher(tmp.getOriginalText());
				
				int start = 0;
				int end = 0;
				while (m.find()) {
					end = m.start();
					if (tmp.getOriginalText().substring(start, end).replaceAll("\\s", "").length() > 0 && start < end) {
						Section s = Section.createSection(this.getType(), father, tmp, start, 
								end , kbm, report, idg);
						matches.add(s);
					}
					start = m.end();
				}
			return matches;
		}
	}
}
