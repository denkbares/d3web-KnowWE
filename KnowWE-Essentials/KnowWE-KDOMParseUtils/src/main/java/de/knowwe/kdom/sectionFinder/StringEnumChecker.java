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

package de.knowwe.kdom.sectionFinder;

import java.util.Arrays;

import de.knowwe.core.compile.CompileScript;
import de.knowwe.core.compile.OptInIncrementalCompileScript;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Message;

public class StringEnumChecker<C extends de.knowwe.core.compile.Compiler, T extends Type> implements CompileScript<C, T>, OptInIncrementalCompileScript<T> {

	private final String[] values;
	private final Message error;
	private final int startOffset;
	private final int endOffset;
	private Class<C> compilerClass;

	public StringEnumChecker(Class<C> compilerClass, String[] values, Message error, int startOffset, int endoffset) {
		this.values = Arrays.copyOf(values, values.length);
		this.error = error;
		this.startOffset = startOffset;
		this.endOffset = endoffset;
		this.compilerClass = compilerClass;
	}

	public StringEnumChecker(Class<C> compilerClass, String[] values, Message error) {
		this(compilerClass, values, error, 0, 0);
	}

	@Override
	public void compile(C compiler, Section<T> s) throws CompilerMessage {

		// cut offsets and trim
		String sectionContent = s.getText();
		sectionContent = sectionContent.substring(startOffset);
		sectionContent = sectionContent.substring(0,
				sectionContent.length() - endOffset);
		String checkContent = sectionContent.trim();

		// check against string values
		boolean found = false;
		for (String string : values) {
			if (checkContent.equalsIgnoreCase(string)) {
				found = true;
				break;
			}
		}
		if (!found) {
			throw new CompilerMessage(error);
		}
	}

	@Override
	public Class<C> getCompilerClass() {
		return compilerClass;
	}

	@Override
	public boolean isIncrementalCompilationSupported(Section<T> section) {
		return true;
	}
}
