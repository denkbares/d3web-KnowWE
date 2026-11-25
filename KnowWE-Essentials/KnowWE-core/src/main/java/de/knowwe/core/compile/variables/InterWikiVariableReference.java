/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.core.compile.variables;

import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.objects.SimpleReference;
import de.knowwe.core.kdom.objects.SimpleReferenceRegistrationScript;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * The name of a variable where it is used, e.g. the <code>@wikiVariable</code> of an import. The reference is not
 * validated here: whether the variable has a value depends on the variant, which is checked and shown by the markup
 * using the variable.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 25.11.2026
 */
public class InterWikiVariableReference extends SimpleReference {

	public InterWikiVariableReference() {
		super(new SimpleReferenceRegistrationScript<>(DefaultGlobalCompiler.class, false), InterWikiVariable.class);
		setSectionFinder(AllTextFinderTrimmed.getInstance());
		setRenderer(StyleRenderer.CONSTANT.withToolMenu());
	}

	@Override
	public Identifier getTermIdentifier(@Nullable TermCompiler compiler, Section<? extends Term> section) {
		return InterWikiVariable.toIdentifier(getTermName(section));
	}
}
