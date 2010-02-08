package de.d3web.we.kdom.questionTreeNew;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.sectionFinder.ConditionalAllTextFinder;

public class NumericCondLine extends DefaultAbstractKnowWEObjectType {

	
	@Override
	protected void init() {
		this.sectionFinder = new ConditionalAllTextFinder() {
			
			@Override
			protected boolean condition(String text, Section father) {
				return (text.startsWith("[") && text.endsWith("]")) || text.startsWith("<") || text.startsWith(">") || text.startsWith("=") ;
			}
		};
		
		this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR7));
		
	}
	
	public static Double getValue(Section<NumericCondLine> sec) {
		String content = sec.getOriginalText();
		if(content.startsWith("\"") && content.endsWith("\"")) {
			content = content.substring(1, content.length()-1);
		}
		
			String value = content.substring(getComparator(sec).length()).trim();
			Double d = null;
			try {
			d = Double.parseDouble(value);
			} catch (Exception e) {
				
			}
			return d;
	}
	
	public static String getComparator(Section<NumericCondLine> sec) {
		String content = sec.getOriginalText();
		if(content.startsWith("\"") && content.endsWith("\"")) {
			content = content.substring(1, content.length()-1).trim();
		}
		
		String [] comps = {"<=", ">=", "<", ">", "="};
		for (String string : comps) {
			if(content.startsWith(string)) {
				return string;
			}
		}
		return null;
	}
	
	public boolean isIntervall(Section<NumericCondLine> sec) {
		if(sec.getOriginalText().startsWith("[") && sec.getOriginalText().endsWith("]")) {
			return true;
		}
		return false;
	}
}
