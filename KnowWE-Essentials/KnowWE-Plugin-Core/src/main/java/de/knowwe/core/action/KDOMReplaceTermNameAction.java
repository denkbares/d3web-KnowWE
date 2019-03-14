/*
 * Copyright (C) 2009-2011 Chair of Artificial Intelligence and Applied
 * Informatics Computer Science VI, University of Wuerzburg
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

package de.knowwe.core.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.denkbares.strings.Strings;
import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * This Action replaces a term name contained in a single KDOM node. Before
 * performing the change, the users privileges are checked.
 * <p/>
 * <p>
 * Needed Parameters:
 * </p>
 * <ul>
 * <li><tt>{@link Attributes.TARGET}:</tt> The KDOM node of which the content
 * will be replaced</li>
 * <li><tt>{@link Attributes.TEXT}:</tt> The new term reference inside the
 * node</li>
 * </ul>
 *
 * @author Alex Legler
 * @created 05.01.2011
 */
public class KDOMReplaceTermNameAction extends AbstractAction {

	@SuppressWarnings("unchecked")
	@Override
	public void execute(UserActionContext context) throws IOException {

		if (context.getWriter() == null) {
			return;
		}

		String nodeID = context.getParameter(Attributes.TARGET);
		String name = context.getTitle();
		String newText = context.getParameter(Attributes.TEXT);

		// Check for user access
		if (!Environment.getInstance().getWikiConnector().userCanEditArticle(name,
				context.getRequest())) {
			context.sendError(403, "You do not have the permission to edit this page.");
			return;
		}

		// Prepare new text, urldecode and strip whitespaces that JSPWiki might
		// have added
		newText = Strings.decodeURL(newText);
		newText = newText.replaceAll("\\s*$", "");

		Map<String, String> nodesMap = new HashMap<>();

		Section<?> section = Sections.get(nodeID);

		if (!(section.get() instanceof Term)) {
			context.sendError(500, "Invalid section type");
			return;
		}
		Section<? extends Term> simpleSection = (Section<? extends Term>) section;
		String originalText = simpleSection.getText();
		String oldTermName = simpleSection.get().getTermIdentifier(simpleSection).getLastPathElement();
		String newNodeText = originalText.replace(oldTermName, newText);

		nodesMap.put(nodeID, newNodeText);
		Sections.replace(context, nodesMap).sendErrors(context);
		try {
			KnowWEUtils.getArticleManager(context.getWeb()).getCompilerManager().awaitTermination();
		}
		catch (InterruptedException e) {
			throw new IOException(e.getMessage());
		}
		context.setContentType(HTML);
		context.getWriter().write("done");
	}
}
