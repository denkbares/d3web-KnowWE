/*
 * Copyright (C) 2022 denkbares GmbH, Germany
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

package de.knowwe.include;

import java.util.List;
import java.util.Set;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;

/**
 * Renders information to HeaderType sections in case they are imported from other wikis
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 01.02.22
 */
public class ImportedHeaderRenderer implements Renderer {

	@Override
	public void render(Section<?> section, UserContext user, RenderResult result) {
		DelegateRenderer.getInstance().render(section, user, result);
		List<ImportMarker> importMarkers = ImportMarker.getImportMarkers(section);
		for (ImportMarker marker : importMarkers) {
			result.append(marker.getInfoText("section"));
		}
	}
}
