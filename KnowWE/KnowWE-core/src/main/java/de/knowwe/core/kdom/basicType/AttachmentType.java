/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.kdom.basicType;

import java.io.IOException;

import de.knowwe.core.Environment;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.DefaultGlobalCompiler.DefaultGlobalScript;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.core.report.Messages;
import de.knowwe.core.wikiConnector.WikiAttachment;

/**
 * A type that refers to a wiki attachment and checks for its existence.
 *
 * @author Reinhard Hatko
 * @created 06.11.2012
 */
public class AttachmentType extends AbstractType {

	public AttachmentType() {
		setSectionFinder(AllTextFinder.getInstance());
		addCompileScript(Priority.HIGHER, new DefaultGlobalScript<AttachmentType>() {

			@Override
			public void compile(DefaultGlobalCompiler compiler, Section<AttachmentType> section) {
				String path = getAbsolutePath(section);
				if (path.isEmpty()) {
					Messages.storeMessage(section, getClass(),
							Messages.syntaxError("No file specified."));
					return;
				}

				WikiAttachment attachment;
				try {
					attachment = getAttachment(section);
				}
				catch (IOException e) {
					Messages.storeMessage(section, getClass(),
							Messages.internalError("Could not access attachment '"
									+ path + "'.", e));
					return;
				}

				if (attachment == null) {
					Messages.storeMessage(section, getClass(),
							Messages.noSuchObjectError("Attachment", path));
					return;
				}

				Messages.clearMessages(section, getClass());
			}

		});
	}

	public static WikiAttachment getAttachment(Section<AttachmentType> section) throws IOException {
		String path = getAbsolutePath(section);
		return Environment.getInstance().getWikiConnector().getAttachment(path);

	}

	public static String getAbsolutePath(Section<AttachmentType> section) {
		String path = section.getText().trim();
		if (!path.contains("/")) {
			path = section.getTitle() + "/" + path;
		}
		return path;
	}

}
