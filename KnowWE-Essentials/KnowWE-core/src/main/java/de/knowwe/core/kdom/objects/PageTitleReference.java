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

package de.knowwe.core.kdom.objects;

import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * @author Jochen Reutelshöfer
 * @created 21.08.2014
 */
public class PageTitleReference extends SimpleReference {

	public PageTitleReference() {
		super(DefaultGlobalCompiler.class, Article.class);
	}

	public PageTitleReference(SectionFinder finder) {
		this();
		setSectionFinder(finder);
	}

	/**
	 * Returns the referenced object (Article), or null if the referenced article does not exists.
	 * <p>
	 * Note: do not mix up with #getArticle, that returns the article the section is defined in.
	 *
	 * @param section the section that references the article
	 * @return the referenced article
	 */
	@Nullable
	public Article getTermObject(Section<? extends PageTitleReference> section) {
		return KnowWEUtils.getArticle(section.getWeb(), getTermIdentifier(section).getLastPathElement());
	}

	@Override
	public String getTermName(Section<? extends Term> section) {
		String text = Strings.trim(section.getText());
		if (text.startsWith("[") && text.endsWith("]")) {
			text = text.substring(1, text.length() - 1);
		}
		return text;
	}

	@Override
	public String getSectionTextAfterRename(Section<? extends RenamableTerm> section, Identifier oldIdentifier, Identifier newIdentifier) {
		// keep brackets if there had been brackets before
		String oldText = Strings.trim(section.getText());
		String newText = newIdentifier.getLastPathElement();
		if (oldText.startsWith("[") && oldText.endsWith("]")) {
			newText = "[" + newText + "]";
		}
		return newText;
	}
}
