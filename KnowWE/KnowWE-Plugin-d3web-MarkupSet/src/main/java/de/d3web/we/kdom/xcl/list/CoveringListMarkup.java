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

package de.d3web.we.kdom.xcl.list;

import de.knowwe.core.compile.packaging.KnowWEPackageManager;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

public class CoveringListMarkup extends DefaultMarkupType {

	public static final String ESTABLISHED_THRESHOLD = "establishedThreshold";
	public static final String SUGGESTED_THRESHOLD = "suggestedThreshold";
	public static final String MIN_SUPPORT = "minSupport";
	public static final String DESCRIPTION = "description";

	private static DefaultMarkup m = null;

	static {
		m = new DefaultMarkup("CoveringList");
		m.addContentType(new CoveringList());
		m.addAnnotation(ESTABLISHED_THRESHOLD, false);
		m.addAnnotation(SUGGESTED_THRESHOLD, false);
		m.addAnnotation(MIN_SUPPORT, false);
		m.addAnnotation(KnowWEPackageManager.ATTRIBUTE_NAME, false);
		m.addAnnotation(DESCRIPTION, false);
	}

	public CoveringListMarkup() {
		super(m);
	}
}
