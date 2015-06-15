/*
 * Copyright (C) 2015 denkbares GmbH, Germany
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import de.d3web.strings.Strings;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;

/**
 * Represents a URL and checks whether the syntax is valid.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 15.06.15
 */
public class URLType extends AbstractType {

	public URLType() {
		setSectionFinder(new AllTextFinderTrimmed());
		addCompileScript(new DefaultGlobalCompiler.DefaultGlobalHandler<URLType>() {

			@Override
			public Collection<Message> create(DefaultGlobalCompiler compiler, Section<URLType> section) throws CompilerMessage {
				URL url = getURL(section);
				if (url == null) {
					return Messages.asList(Messages.error("'" + Strings.trim(section.getText()) + "' is not a valid URL"));
				}
				else {
					return Messages.noMessage();
				}

			}

		});
	}

	public static URL getURL(Section<URLType> urlSection) {
		try {
			return new URL(Strings.trim(urlSection.getText()));
		}
		catch (MalformedURLException e) {
			return null;
		}
	}
}
