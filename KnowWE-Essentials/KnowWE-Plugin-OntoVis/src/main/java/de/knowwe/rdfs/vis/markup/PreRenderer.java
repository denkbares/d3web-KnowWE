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

package de.knowwe.rdfs.vis.markup;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.rdfs.vis.PreRenderWorker;
import de.knowwe.rdfs.vis.util.Utils;

/**
 * Renderer that has a slow pre rendering step with a result that can be cached. To be used together with {@link
 * PreRenderWorker}.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 07.05.15
 */
public interface PreRenderer extends Renderer {

	void preRender(Section<?> section, UserContext user);

	default String getCacheFileID(Section<? extends Type> section) {
		return Utils.getFileID(section);
	}

	void cleanUp(Section<?> section);
}
