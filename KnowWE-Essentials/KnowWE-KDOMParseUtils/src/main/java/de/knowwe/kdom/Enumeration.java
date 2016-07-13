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
package de.knowwe.kdom;

import java.util.Collection;

import com.denkbares.strings.Strings;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.DefaultGlobalCompiler.DefaultGlobalHandler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * This class describes a plain enum constant value to be parsed out of some
 * wiki text.
 * 
 * @author Volker Belli
 * @created 26.01.2014
 */
public class Enumeration<T extends Enum<T>> extends AbstractType {

	private final Class<T> enumType;

	public Enumeration(Class<T> enumType) {
		this(enumType, new RegexSectionFinder("[\\w_]+"));
	}

	public Enumeration(Class<T> enumType, SectionFinder f) {
		this.enumType = enumType;
		this.setSectionFinder(f);
		this.addCompileScript(new EnumerationChecker());
		this.setRenderer(StyleRenderer.NUMBER);
	}

	/**
	 * Returns the enum constant parsed out of the sections text, or null if the
	 * text could not been matched to a valid enum constant.
	 * 
	 * @created 13.06.2011
	 * @param section the section to parse the number from
	 * @return the parsed number
	 */
	public T getValue(Section<Enumeration<T>> section) {
		return Strings.parseEnum(section.getText(), enumType);
	}

	class EnumerationChecker extends DefaultGlobalHandler<Enumeration<T>> {

		@Override
		public Collection<Message> create(DefaultGlobalCompiler compiler, Section<Enumeration<T>> section) throws CompilerMessage {
			T value = section.get().getValue(section);
			if (value == null) {
				throw CompilerMessage.error("Invalid value '" + section.getText() + "' where a "
						+ enumType.getSimpleName() + " name is expected");
			}
			return Messages.noMessage();
		}

	}
}
