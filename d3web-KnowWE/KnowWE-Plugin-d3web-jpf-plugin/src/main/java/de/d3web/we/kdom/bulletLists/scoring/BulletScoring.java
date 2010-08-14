package de.d3web.we.kdom.bulletLists.scoring;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;

public class BulletScoring extends AbstractXMLObjectType {

	public BulletScoring() {
		super("BulletScoring");
	}

	@Override
	public void init() {
		childrenTypes.add(new ScoringListContentType());
		// this.setNotRecyclable(true);
	}

	public static final String TARGET_SCORING_DELIMITER = "[AND]";

	public static List<String> getScoringTargets(Section s) {

		Map<String, String> map = AbstractXMLObjectType.getAttributeMapFor(s);

		String values = map.get("scorings");

		if (values == null) return null;

		String[] targets = values.split(("\\Q" + TARGET_SCORING_DELIMITER + "\\E"));

		List<String> result = Arrays.asList(targets);

		return result;

	}

	public static final String DEFAULT_VALUE_KEY = "defaultValue";

	public static String getDefaultValue(Section s) {
		Map<String, String> map = AbstractXMLObjectType.getAttributeMapFor(s);

		return map.get(DEFAULT_VALUE_KEY);
	}

}
