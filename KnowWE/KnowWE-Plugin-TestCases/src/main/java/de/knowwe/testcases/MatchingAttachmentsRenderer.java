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
package de.knowwe.testcases;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Lists all files specified by regular expressions annotated with "@file"
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 12.03.2012
 */
public class MatchingAttachmentsRenderer implements Renderer {

	private static final String ANNOTATION_FILE = "file";

	@Override
	public void render(Section<?> section, UserContext user, RenderResult string) {
		Section<DefaultMarkupType> defaultMarkupSection;
		if (section.get() instanceof DefaultMarkupType) {
			defaultMarkupSection = Sections.cast(section, DefaultMarkupType.class);
		}
		else {
			defaultMarkupSection = Sections.ancestor(section,
					DefaultMarkupType.class);
		}
		String[] annotations = DefaultMarkupType.getAnnotations(defaultMarkupSection,
				ANNOTATION_FILE);
		Set<String> attachments = new TreeSet<String>();
		for (String s : annotations) {
			try {
				for (WikiAttachment attachment : KnowWEUtils.getAttachments(section.getTitle(),
						s)) {
					attachments.add(attachment.getPath());
				}
			}
			catch (IOException e) {
			}
		}
		if (attachments.size() > 0) {
			string.append("The following attachments match the specified regular expression(s): \n");
			for (String s : attachments) {
				string.append("# " + s + "\n");
			}
			string.append("\n");
		}
	}

}
