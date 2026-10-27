/*
 * Copyright (C) 2026 denkbares GmbH, Germany
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

package de.knowwe.jspwiki.acl;

import java.util.List;
import java.util.ResourceBundle;

import org.apache.wiki.auth.acl.DefaultAclManager;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiConnector;
import de.knowwe.jspwiki.JSPWikiConnector;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;

/**
 * A page ACL, e.g. {@code [{ALLOW edit Max Mustermann, Admin}]}.
 * <p>
 * The markup itself is rendered unchanged, JSPWiki still evaluates it. Behind it, users who may edit the page see a
 * warning for every entry that does not or not unambiguously take effect, see {@link AccessRuleCheck}. The check runs on
 * rendering and not on compiling, so the warnings follow changes of users and groups without recompiling the page.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 28.10.2026
 */
public class AccessRuleType extends AbstractType {

	public AccessRuleType() {
		setSectionFinder(new RegexSectionFinder(DefaultAclManager.ACL_PATTERN));
		setRenderer((section, user, result) -> {
			DelegateRenderer.getInstance().render(section, user, result);

			WikiConnector connector = user.getEnvironment().getWikiConnector();
			if (!(connector instanceof JSPWikiConnector jspWikiConnector)) return;
			// readers do not need to know about the users and groups of the page's ACL
			if (!KnowWEUtils.canWrite(section.getArticle(), user)) return;

			ResourceBundle bundle = Messages.getMessageBundle(user);
			AccessRuleCheck check = new AccessRuleCheck(jspWikiConnector.getAuthorizationManager(),
					jspWikiConnector.getUserManager().getUserDatabase(), bundle);
			List<Message> warnings = check.checkAccessRule(section.getText())
					.stream()
					.map(problem -> Messages.warning(problem.verbalize(bundle)))
					.toList();
			if (!warnings.isEmpty()) {
				DefaultMarkupRenderer.renderMessagesOfType(Message.Type.WARNING, warnings, result);
			}
		});
	}
}
