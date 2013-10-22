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

import java.util.Collection;
import java.util.List;

import de.d3web.strings.Identifier;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * 
 * 
 * @author Stefan Plehn
 * @created 08.05.2013
 * 
 * 
 */
public class PackageTerm extends AbstractType implements Term, RenamableTerm {

	public PackageTerm(boolean checkExistingSections) {

		this.setSectionFinder(new AllTextFinderTrimmed());

		setRenderer(StyleRenderer.PACKAGE);

		if (checkExistingSections) {
			addSubtreeHandler(Priority.HIGH, new CheckSectionsForPackageExistence());
		}
	}

	@Override
	public Class<?> getTermObjectClass(Section<? extends Term> section) {
		return PackageTerm.class;
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
		String replacement = newIdentifier.getLastPathElement();
		return replacement;
	}

	private class CheckSectionsForPackageExistence extends SubtreeHandler<PackageTerm> {

		@Override
		public Collection<Message> create(Article article, Section<PackageTerm> section) {

			if (section.getText().equals(PackageManager.THIS)) {
				return Messages.noMessage();
			}
			PackageManager packageManager =
					Environment.getInstance().getPackageManager(article.getWeb());
			List<Section<?>> sectionsOfPackage = packageManager.getSectionsOfPackage(section.getText());

			if (sectionsOfPackage.isEmpty()) {
				return Messages.asList(Messages.warning("Package " + section.getText()
						+ " does not contain any sections."));
			}
			else {
				return Messages.noMessage();
			}

		}

	}

}
