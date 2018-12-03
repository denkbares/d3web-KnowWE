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

import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;

/**
 * Basic {@link Term} which can easily be handled for registering in a
 * {@link TerminologyManager}.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 07.02.2012
 */
public interface Term extends Type {

	String IDENTIFIER_KEY = "identifierKey";

	static Identifier getCachedIdentifier(TermCompiler compiler, Section<? extends Term> section) {
		return (Identifier) section.getObject(compiler, IDENTIFIER_KEY);
	}

	static Identifier cacheIdentifier(TermCompiler compiler, Section<? extends Term> section, Identifier identifier) {
		section.storeObject(compiler, IDENTIFIER_KEY, identifier);
		return identifier;
	}

	/**
	 * Defines the class for which the Term should be registered. Can be dependent on given {@link TermCompiler}, which
	 * can be null, so there should also be a fall back term class.
	 *
	 * @param compiler the term compiler this identifier is generated for, can be null!
	 * @param section  the {@link Section} with this interface
	 * @return the class of this {@link Term}
	 * @created 28.03.2013
	 */
	Class<?> getTermObjectClass(@Nullable TermCompiler compiler, Section<? extends Term> section);

	/**
	 * Defines the {@link Identifier} which can be used to register this {@link Term}. {@link Identifier} need to be
	 * unique inside the {@link TermCompiler}, the term name does not have this restriction.
	 * {@link Identifier}s can be different depending on the compiler it is generated for. The compiler can also be
	 * null, so there has to be all fall back {@link Identifier} for this case.
	 *
	 * @param compiler the term compiler this identifier is generated for, can be null!
	 * @param section  the {@link Section} to create the identifier for
	 * @return the Identifier for this {@link TermCompiler} and {@link Section}
	 * @created 28.03.2013
	 */
	default Identifier getTermIdentifier(@Nullable TermCompiler compiler, Section<? extends Term> section) {
		return new Identifier(getTermName(section));
	}

	/**
	 * Defines the {@link Identifier} which can be used to register this {@link Term}. {@link Identifier} need to be
	 * unique inside the {@link TermCompiler}, the term name does not have this restriction.
	 * This method can be called, but should not be overridden, be sure to always override {@link
	 * Term#getTermIdentifier(TermCompiler, Section)} instead.
	 *
	 * @param section the {@link Section} to create the identifier for
	 * @return the Identifier for this {@link Section} and the first {@link TermCompiler} found compiling the section
	 */
	default Identifier getTermIdentifier(Section<? extends Term> section) {
		return getTermIdentifier(null, section);
	}

	/**
	 * Defines the name of this {@link Term}. It can be used for example to
	 * render this Term, but is often also at least part of the
	 * {@link Identifier}.
	 *
	 * @param section the {@link Section} with this {@link InternalError}
	 * @return the name of this term
	 * @created 28.03.2013
	 */
	default String getTermName(Section<? extends Term> section) {
		return Strings.trimQuotes(section.getText());
	}
}
