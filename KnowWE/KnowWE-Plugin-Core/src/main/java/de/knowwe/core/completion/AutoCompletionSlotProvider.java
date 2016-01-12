/*
 * Copyright (C) 2015 denkbares GmbH, Germany
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

package de.knowwe.core.completion;

import java.io.IOException;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;

/**
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 16.10.14.
 */
public interface AutoCompletionSlotProvider {

    String EXTENSION_POINT_COMPLETION_PROVIDER = "AutoCompletionSlotProvider" ;

	/**
	 * Initializes the AutoCompletionSlotProvider to be ready for use
	 *
	 * @param section the section the slot is rendered for
	 * @param user the user context
	 * @throws IOException
	 */
	void init(Section<?> section, UserContext user) throws IOException;

	/**
	 * Renders a AutoCompletionSlot as HTML
	 *
	 * @param content The html result stream where the slot is appended
	 * @param section the section the slot is rendered for
	 */
    void renderAutoCompletionSlot(RenderResult content, Section<?> section);
}
