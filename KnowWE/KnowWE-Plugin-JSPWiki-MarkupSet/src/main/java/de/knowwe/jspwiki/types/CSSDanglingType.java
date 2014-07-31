/*
 * Copyright (C) 2013 denkbares GmbH
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
package de.knowwe.jspwiki.types;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Type to find leftover JSPWiki class types that where defined around other
 * types (like {@link DefaultMarkupType}s) an therefore not found by the
 * {@link ClassType}. We do no longer support such class definitions.
 * 
 * @author Albrecht Striffler, Sebastian Furth (denkbares GmbH)
 * @created 28.08.2013
 */
public class CSSDanglingType extends AbstractType {
	
	private static final String[] STYLES = new String[] 
		{ "small", "sub", "sup", "strike", "ltr", "rtl", "commentbox",
		   "information", "warning", "error", "quote", "center", "collapse",
		   "collapsebox", "category", "tip", "graphbar", "sortable", "table-filter",
		   "zebra-table", "columns", "tabbedSection", "tab", "tabbedAccordion",
		   "accordion", "prettify", "slimbox", "reflection"
		 };
	
	private static String PATTERN_STYLES;
	
	static {
		StringBuilder b = new StringBuilder();
		b.append("%%(");
		for (String style : STYLES) {
			b.append(style);
			b.append("|");
		}
		b.delete(b.length() - 1, b.length());
		b.append(").*");
		PATTERN_STYLES = b.toString();
	}
	
	public CSSDanglingType() {
		this.setSectionFinder(new RegexSectionFinder("(?:%|/)%[^\\s]*"));
		this.setRenderer((section, user, result) -> {
			String text = section.getText();
			String customSyle = CustomStyleMatcher.getCustomStyle(text);
			if (customSyle != null) {
				result.append("%%");
				result.append(customSyle);
				result.append("\n");
			}
			else if (text.startsWith("%%(") || text.equals("%%")
					|| text.equals("/%") || text.matches(PATTERN_STYLES)) {
				result.append(text);
			}
			else {
				result.appendJSPWikiMarkup(text);
			}
		});
	}

}
