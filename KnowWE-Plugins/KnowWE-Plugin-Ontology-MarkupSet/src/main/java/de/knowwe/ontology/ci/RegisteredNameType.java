/*
 * Copyright (C) 2020 denkbares GmbH, Germany
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

package de.knowwe.ontology.ci;

import java.util.Collection;
import java.util.Collections;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.objects.SimpleDefinition;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.utils.KnowWEUtils;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * A simple type to be used in name annotations (and similar). It will register the section with the name/content of the
 * section, prepended by the class name of the constructor.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 18.03.2020
 */
public class RegisteredNameType extends SimpleDefinition {

	private final Class<? extends Type> parentClass;

	public RegisteredNameType(Class<? extends Type> parentClass) {
		super(DefaultGlobalCompiler.class, parentClass);
		setSectionFinder(AllTextFinderTrimmed.getInstance());
		this.parentClass = parentClass;
	}

	@Override
	public Identifier getTermIdentifier(@Nullable TermCompiler compiler, Section<? extends Term> section) {
		return new Identifier(parentClass.getSimpleName(), getTermName(section));
	}

	@NotNull
	public static <T extends Type> Collection<Section<T>> getNamedMarkupSections(String registeredName, Class<T> registeredType) {
		if (registeredName == null) return Collections.emptyList();
		DefaultGlobalCompiler defaultGlobalCompiler = Compilers.getGlobalCompiler(KnowWEUtils.getDefaultArticleManager());
		Section<? extends Type> registeredSections = defaultGlobalCompiler.getTerminologyManager()
				.getTermDefiningSection(new Identifier(registeredType.getSimpleName(), registeredName));
		return $(registeredSections).closest(registeredType).asList();
	}
}
