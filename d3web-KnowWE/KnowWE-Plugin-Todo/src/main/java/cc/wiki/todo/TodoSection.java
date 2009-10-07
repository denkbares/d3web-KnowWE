package cc.wiki.todo;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class TodoSection extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
	}

	@Override
	public SectionFinder getSectioner() {
		return new RegexSectionFinder("TODO:(.*)");
	}
	
	public String getTodoText(Section sec) {
		String text = sec.getOriginalText();
		return text.substring(5);
	}
	
	@Override
	public KnowWEDomRenderer getRenderer() {
		return new KnowWEDomRenderer() {
			
			@Override
			public void render(Section sec, KnowWEUserContext user,
					StringBuilder string) {
				
				
				string.append(KnowWEEnvironment.maskHTML("<div class='information'>"));
				
				TodoSection type = (TodoSection) sec.getObjectType();
				string.append(type.getTodoText(sec));

				string.append(KnowWEEnvironment.maskHTML("</div>"));
				
			}
			
		};
	}
}
