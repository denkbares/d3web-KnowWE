/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.knowwe.diaflux.type;

import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.renderer.StyleRenderer.MaskMode;
import de.knowwe.kdom.xml.AbstractXMLType;
import de.knowwe.kdom.xml.XMLContent;

/**
 * 
 * @author Reinhard Hatko
 * @created 05.08.10
 */
public class SnapshotType extends AbstractXMLType {

	private static SnapshotType instance;

	private SnapshotType() {
		super("snapshot");
		XMLContent content = new XMLContent();
		addChildType(content);
		// content is already xml encoded,
		// being compatible with html entity encoding
		content.setRenderer(StyleRenderer.DEFAULT.withMaskMode(MaskMode.none));
	}

	public static SnapshotType getInstance() {
		if (instance == null) instance = new SnapshotType();

		return instance;
	}

}
