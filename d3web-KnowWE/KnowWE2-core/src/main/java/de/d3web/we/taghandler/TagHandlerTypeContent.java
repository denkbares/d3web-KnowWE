package de.d3web.we.taghandler;

import java.util.HashMap;
import java.util.Map;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;

public class TagHandlerTypeContent extends DefaultAbstractKnowWEObjectType{
	
	
	
	private Map<Section, Map<String,String>>  tagValuesForSections = new HashMap<Section, Map<String,String>>();

	public Map<Section, Map<String,String>> getValuesForSections() {
		return this.tagValuesForSections;
	}
	
	public void setTagValuesForSections(
			Map<Section, Map<String, String>> tagValuesForSections) {
		this.tagValuesForSections = tagValuesForSections;
	}


	@Override
	protected void init() {
		this.sectionFinder = new TagHandlerAttributeFinder(this);
	}

}
