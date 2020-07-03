/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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

package de.knowwe.core.tools;

import java.util.Collection;
import java.util.stream.Collectors;

import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.tools.ToolUtils;
import de.knowwe.util.Icon;

/**
 * @author Stefan Plehn
 * @created 10.03.2014
 */
public class RenamingToolProvider implements ToolProvider {

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		if (hasTools(section, userContext)) {
			return new Tool[] {
					getRenamingTool(Sections.cast(section, Term.class)) };
		}
		return ToolUtils.emptyToolArray();
	}

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		if (!(section.get() instanceof RenamableTerm)) return false;
		Section<RenamableTerm> termSection = Sections.cast(section, RenamableTerm.class);
		Collection<TermCompiler> compilers = Compilers.getCompilersWithCompileScript(section, TermCompiler.class)
				.stream()
				.filter(c -> isCompatibleCompiler(c, termSection))
				.collect(Collectors.toList());
		return !compilers.isEmpty()
				&& compilers.stream()
				.anyMatch(c -> !c.getTerminologyManager()
						.getTermClasses(termSection.get().getTermIdentifier(c, termSection)).isEmpty())
				&& ((RenamableTerm) section.get()).allowRename(Sections.cast(section, RenamableTerm.class))
				&& termSection.get().getTermIdentifier(compilers.iterator().next(), termSection) != null
				&& KnowWEUtils.canWrite(section, userContext);
	}

	public boolean isCompatibleCompiler(TermCompiler compiler, Section<RenamableTerm> termSection) {
		try {
			termSection.get().getTermIdentifier(compiler, termSection);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	protected Tool getRenamingTool(Section<? extends Term> section) {
		return new RenamingTool(
				Icon.RENAME,
				"Rename",
				"Rename this term wiki wide.",
				createRenamingAction(section),
				Tool.CATEGORY_EDIT);
	}

	protected String createRenamingAction(Section<? extends Term> section) {
		return "KNOWWE.plugin.renaming.renameTerm('" + section.getID() + "')";
	}

	public static class RenamingTool extends DefaultTool {

		public RenamingTool(Icon icon, String title, String description, String renamingAction, String categoryEdit) {
			super(icon, title, description, renamingAction, categoryEdit);
		}
	}
}

