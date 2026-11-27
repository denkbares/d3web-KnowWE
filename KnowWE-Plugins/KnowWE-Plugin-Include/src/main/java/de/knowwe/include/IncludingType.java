/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.include;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;

/**
 * A type whose sections render sections of (other) articles within themselves, by
 * {@link FramedIncludedSectionRenderer#renderTargetSections}, like <code>%%Include</code> or
 * <code>%%InterWikiImport</code>. Before rendering an included section, the renderer follows these types to find out
 * whether the included section would include itself again.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 27.11.2026
 */
interface IncludingType extends Type {

	/**
	 * The sections the given section of this type renders within itself for the given user.
	 *
	 * @param section a section of this type
	 * @param user    the user the section is rendered for
	 * @return the included sections, empty if there are none (yet)
	 */
	@NotNull
	List<Section<?>> getIncludedSections(Section<?> section, UserContext user);
}
