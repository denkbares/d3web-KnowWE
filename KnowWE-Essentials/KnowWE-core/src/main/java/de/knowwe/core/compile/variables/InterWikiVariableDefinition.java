/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.core.compile.variables;

import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.objects.SimpleDefinition;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * The name of a variable where it is defined, i.e. the <code>@name</code> of an {@link InterWikiVariableMarkup}. A
 * variable usually has several definitions, one per variant.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 25.11.2026
 */
public class InterWikiVariableDefinition extends SimpleDefinition {

	public InterWikiVariableDefinition() {
		super(DefaultGlobalCompiler.class, InterWikiVariable.class);
		setSectionFinder(AllTextFinderTrimmed.getInstance());
		setRenderer(StyleRenderer.CONSTANT.withToolMenu());
	}

	@Override
	public Identifier getTermIdentifier(@Nullable TermCompiler compiler, Section<? extends Term> section) {
		return InterWikiVariable.toIdentifier(getTermName(section));
	}
}
