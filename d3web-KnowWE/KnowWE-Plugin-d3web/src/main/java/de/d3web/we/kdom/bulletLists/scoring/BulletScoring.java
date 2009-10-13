package de.d3web.we.kdom.bulletLists.scoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;

public class BulletScoring extends AbstractXMLObjectType{
	
	public BulletScoring() {
		super("BulletScoring");
	}
	
	public void init() {
		childrenTypes.add(new ScoringListContentType());
	}
	
	public static final String TARGET_SCORING_DELIMITER = "[AND]";
	
	public static  List<String> getScoringTargets(Section s) {
		
		Map<String,String> map = AbstractXMLObjectType.getAttributeMapFor(s);
		
		String values = map.get("scorings");
		
		String [] targets = values.split(("\\Q"+TARGET_SCORING_DELIMITER+"\\E"));
		
		List<String> result = Arrays.asList(targets);
		
		return result;
		
	}
	
	public static final String DEFAULT_VALUE_KEY = "defaultValue";
	
	public static String getDefaultValue(Section s) {
		Map<String,String> map = AbstractXMLObjectType.getAttributeMapFor(s);
		
		return map.get(DEFAULT_VALUE_KEY);
	}

}
