/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.compile.packaging;

import java.util.regex.Pattern;

import de.d3web.strings.Identifier;
import de.knowwe.core.compile.PackageRegistrationCompiler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.kdom.objects.SimpleReference;
import de.knowwe.core.kdom.objects.SimpleReferenceRegistrationScript;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * @author Stefan Plehn, Albrecht Striffler (denkbares GmbH)
 * @created 08.05.2013
 */
public class PackageTerm extends SimpleReference {

	public PackageTerm() {
		super(new SimpleReferenceRegistrationScript<>(PackageRegistrationCompiler.class, false), Package.class, Priority.BELOW_DEFAULT);
		this.addCompileScript(Priority.LOW, new PackageNotCompiledWarningScript());
		this.setSectionFinder(new RegexSectionFinder(Pattern.compile("\\s*((?=\\s*\\S).+?)\\s*(?:\r?\n|\\z)"), 1));
		setRenderer(StyleRenderer.PACKAGE);
	}

	@Override
	public Identifier getTermIdentifier(Section<? extends Term> section) {
		return new Identifier(getTermName(section));
	}

	@Override
	public String getTermName(Section<? extends Term> section) {
		return section.getText();
	}

	@Override
	public String getSectionTextAfterRename(Section<? extends RenamableTerm> section, Identifier oldIdentifier, Identifier newIdentifier) {
		return newIdentifier.getLastPathElement();
	}

}
