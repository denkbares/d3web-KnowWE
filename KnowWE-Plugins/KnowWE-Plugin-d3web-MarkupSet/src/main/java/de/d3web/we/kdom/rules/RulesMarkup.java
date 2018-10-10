/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
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

package de.d3web.we.kdom.rules;

import de.d3web.we.kdom.rules.utils.RuleEditToolProvider;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * A markup to create a block for text-rules
 * <p>
 * @author Jochen
 * @see RuleContentType
 */
public class RulesMarkup extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("Rule");
		MARKUP.addContentType(new RuleContentType());
		PackageManager.addPackageAnnotation(MARKUP);
	}

	public RulesMarkup() {
		super(MARKUP);
		this.setRenderer(new DefaultMarkupRenderer("KnowWEExtension/d3web/icon/rule24.png") {
			@Override
			protected void renderContents(Section<?> section, UserContext user, RenderResult string) {
				string.append("[Label|SSM Wiring]");
				boolean ruleDebuggingActive = RuleEditToolProvider.isRuleDebuggingActive(user);
				if (ruleDebuggingActive) {
					string.appendHtml("<div class='ruleDebugView'>");
				}
				super.renderContents(section, user, string);
				if (ruleDebuggingActive) {
					string.appendHtml("</div>>");
				}
			}
		});
	}
}
