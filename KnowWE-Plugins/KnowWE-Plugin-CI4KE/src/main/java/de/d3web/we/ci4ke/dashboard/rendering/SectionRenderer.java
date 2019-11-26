/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.d3web.we.ci4ke.dashboard.rendering;

import de.knowwe.core.Environment;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Renders section in CI results. Object name can either be the id of the section or in case of DefaultMarkupSections,
 * the annotation "name" of that section. In this case, the first section found with this name will be rendered.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 30.05.17
 */
public class SectionRenderer implements ObjectNameRenderer {

	@Override
	public void render(UserContext context, String objectName, RenderResult result) {
		Section<?> section = null;
		try {
			section = Sections.get(objectName);
		}
		catch (NumberFormatException ignore) {
		}
		String displayName;
		if (section == null) {
			// seems to not be the id, try annotation "name"
			displayName = objectName;
			section = $(Environment.getInstance().getArticleManager(context.getWeb()))
					.successor(DefaultMarkupType.class)
					.stream()
					.filter(s -> objectName.equals(DefaultMarkupType.getAnnotation(s, "name")))
					.findFirst()
					.orElse(null);
		}
		else {
			displayName = section.get().getName() + "(id:" + objectName + ")";
		}
		if (section == null) {
			result.append(displayName);
		}
		else {
			String url = KnowWEUtils.getURLLink(section);
			result.appendHtml("<a href='" + url + "'>");
			result.append(displayName);
			result.appendHtml("</a>");
		}
	}
}
