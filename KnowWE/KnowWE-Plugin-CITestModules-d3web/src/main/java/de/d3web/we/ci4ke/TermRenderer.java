/*
 * Copyright (C) 2012 denkbares GmbH
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
package de.d3web.we.ci4ke;

import java.util.Collection;

import de.d3web.we.ci4ke.rendering.TestObjectRenderer;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.terminology.TermIdentifier;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * Renders a link to the definition of a term or to the ObjectInfoPage of the
 * term, if there are multiple definitions.
 * 
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 25.09.2012
 */
public class TermRenderer implements TestObjectRenderer {

	@Override
	public String render(String objectName) {
		String url = null;
		Collection<TerminologyManager> terminologyManagers = Environment.getInstance().getTerminologyManagers(
				Environment.DEFAULT_WEB);
		for (TerminologyManager terminologyManager : terminologyManagers) {
			Collection<Section<?>> termDefiningSections = terminologyManager.getTermDefiningSections(new TermIdentifier(
					objectName));
			if (termDefiningSections.size() > 1) break;
			if (termDefiningSections.size() == 1) {
				if (url == null) {
					url = KnowWEUtils.getURLLink(termDefiningSections.iterator().next());
				}
				else {
					url = null;
					break;
				}
			}
		}
		if (url == null) url = "Wiki.jsp?page=ObjectInfoPage&objectname=" + objectName;
		return "<a href='" + url + "'>" + objectName + "</a>";
	}

}
