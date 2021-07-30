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
package de.knowwe.core.compile.terminology;

import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.objects.TermUtils;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * This interface provides functionality for renaming terms. Therefore you have to implement it, if you want renaming to
 * work. It's is possible to specify how a renaming action affects the corresponding section. If it is the simplest case
 * and replacement text and section text are the same, you can just return the replacement text.
 *
 * @author Stefan Plehn
 * @created 22.05.2013
 */
public interface RenamableTerm extends Term {

	/**
	 * Returns the new content text of the markup section. After renaming the returned text is replacing the original
	 * text of the section. The method should make sure that the replaced section content still refers to the new
	 * identifier.
	 * <p>
	 * Typically the method (a) encodes the identifier to match the markup. It also (b) may abbreviate the identifier to
	 * local names, e.g. only the last part of the identifier needs to be referenced by the section (e.g. if a choice
	 * name is changed, the question's identifier part is still constructed without named explicitly).
	 *
	 * @param section       the (outdated) section to be modified
	 * @param oldIdentifier the previously identifier (as created by the outdated section)
	 * @param newIdentifier the new identifier, the new section should refer to
	 * @return the content text of the section, replacing the outdated section's text
	 */
	default String getSectionTextAfterRename(Section<? extends RenamableTerm> section, Identifier oldIdentifier, Identifier newIdentifier) {
		// we sometimes register multiple different identifiers to the same section
		// so by default we only change the text of those sections, where the text actually matches the
		// last element of the old identifier
		if (section.getText().equals(oldIdentifier.getLastPathElement())
				|| section.getText().equals(Strings.quote(oldIdentifier.getLastPathElement()))) {
			return TermUtils.quoteIfRequired(newIdentifier.getLastPathElement());
		}
		else {
			return section.getText();
		}
	}

	/**
	 * Returns true if the specified section is really allowed to be renamed. You may override / implement the method if
	 * a more selective renaming is required. Otherwise this method default to "true".
	 *
	 * @param section the section of the renamable term
	 * @return true if the term can be renamed
	 */
	default boolean allowRename(Section<? extends RenamableTerm> section) {
		// by default, we always allow renaming, except for attachment articles (instead, the attachment source should be edited)
		return !KnowWEUtils.isAttachmentArticle(section.getArticle());
	}
}
