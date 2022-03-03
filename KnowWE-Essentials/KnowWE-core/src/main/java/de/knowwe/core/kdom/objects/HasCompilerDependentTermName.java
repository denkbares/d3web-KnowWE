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
package de.knowwe.core.kdom.objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.parsing.Section;

/**
 * Mixin interface for Terms that produce compiler dependent term names
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 09.12.2020
 */
public interface HasCompilerDependentTermName extends Term {
	Logger LOGGER = LoggerFactory.getLogger(HasCompilerDependentTermName.class);

	String getTermName(TermCompiler compiler, Section<? extends Term> section);

	@Override
	default String getTermName(Section<? extends Term> section) {
		LOGGER.warn("Calling getTermName on Term with compiler-dependent term name without a compiler. This has to be fixed!");
		return Term.super.getTermName(section);
	}

	default String getDefaultTermName(Section<? extends Term> section) {
		return Term.super.getTermName(section);
	}
}
