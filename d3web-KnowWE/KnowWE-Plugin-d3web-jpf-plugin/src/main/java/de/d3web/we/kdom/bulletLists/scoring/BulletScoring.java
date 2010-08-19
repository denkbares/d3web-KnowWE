/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

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
