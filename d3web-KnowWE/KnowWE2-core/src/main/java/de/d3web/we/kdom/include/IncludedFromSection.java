package de.d3web.we.kdom.include;

import java.util.List;

import de.d3web.we.kdom.Section;

public class IncludedFromSection extends Section {
	
	
	/**
	 * Special class for IncludedFromSections
	 * -> Skips rekursive creating of children!
	 */
	public IncludedFromSection(Section father, String text, String topic, int offset, List<Section> children) {
		this.objectType = new IncludedFromType();
		this.father = father;
		this.children = children;
		this.originalText = text;
		this.topic = topic;
		this.id = getArticle().getIDGen().newID().getID();
		this.offSetFromFatherText = offset;
		
		int childOffset = 0;
		for (Section child:children) {
			child.setFather(this);
			child.setOffSetFromFatherText(childOffset);
			childOffset += child.getOriginalText().length();
		}
	}

}
