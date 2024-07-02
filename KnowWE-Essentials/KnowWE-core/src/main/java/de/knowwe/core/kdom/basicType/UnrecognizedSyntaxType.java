/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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

import java.util.ArrayList;
import java.util.Collection;

import com.denkbares.strings.Strings;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.DefaultGlobalCompiler.DefaultGlobalHandler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.rendering.elements.Span;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;

/**
 * Type to be used for left over text in markups. Left over means that no other SectionFinder could recognize this
 * text.
 * <p>
 * The found text will be rendered red and an error messages will be attached to the section.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 29.11.2011
 */
public class UnrecognizedSyntaxType extends AbstractType {

	private static final UnrecognizedSyntaxType instance = new UnrecognizedSyntaxType();

	private UnrecognizedSyntaxType() {
		this.setSectionFinder(new AllTextFinderTrimmed(AllTextFinderTrimmed.TrimType.SPACES_AND_LINE_BREAKS));
		this.setRenderer(new Renderer() {
			@Override
			public void render(Section<?> section, UserContext user, RenderResult result) {
				result.append(new Span(section.getText()).clazz("unrecognized-syntax"));
			}
		});
		this.addCompileScript(new DefaultGlobalHandler<UnrecognizedSyntaxType>() {

			@Override
			public Collection<Message> create(DefaultGlobalCompiler article, Section<UnrecognizedSyntaxType> section) {
				Collection<Message> msgs = new ArrayList<>(1);
				msgs.add(Messages.error("Unrecognized syntax: " + Strings.ellipsis(section.getText(), 50)));
				return msgs;
			}
		});
	}

	public static UnrecognizedSyntaxType getInstance() {
		return instance;
	}
}
