package de.d3web.we.taghandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.utils.KnowWEUtils;

public class TagHandlerAttributeFinder extends SectionFinder {

	public static final String ATTRIBUTE_MAP = "TagHandler.attributeMap";

	public TagHandlerAttributeFinder(KnowWEObjectType type) {
		super(type);
	}

	@Override
	public List<Section> lookForSections(Section tmpSection, Section father,
			KnowledgeRepresentationManager kbm, KnowWEDomParseReport report, IDGenerator idg) {
		Map<Section,Map<String,String>> map  = ((TagHandlerTypeContent)type).getValuesForSections();
		
		Map<String, String> values = new HashMap<String, String>();

		//attribute parsen und einfügen
		String[] tmpSecSplit = tmpSection.getOriginalText().split(",");
		for (int i = 0; i < tmpSecSplit.length; i++) {
			String tag = tmpSecSplit[i].split("=")[0];
			String value = new String();
			if (tmpSecSplit[i].contains("=")) {
				String[] splitted = tmpSecSplit[i].split("=");
				if(splitted.length == 2) {
					value = splitted[1];	
				}else {
					value = "";
				}
			}
			values.put(tag, value);
		}

		int length = tmpSection.getOriginalText().length();
		Section sec = Section.createSection(this.getType(), father, tmpSection, 0, length, kbm, report, idg);
		map.put(sec, values);
		((TagHandlerTypeContent)type).setTagValuesForSections(map);
		KnowWEUtils.storeSectionInfo(father.getArticle().getWeb(),father.getTopic(), sec.getFather().getId(), ATTRIBUTE_MAP, values);
		List<Section> result = new ArrayList<Section>();
		result.add(sec);
		return result;
	}
}
