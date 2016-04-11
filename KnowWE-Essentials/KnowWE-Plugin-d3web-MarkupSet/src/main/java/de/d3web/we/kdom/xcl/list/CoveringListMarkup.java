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

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

public class CoveringListMarkup extends DefaultMarkupType {

	public static final String OTHER_QUESTIONS = "otherQuestions";
	public static final String OTHER_QUESTIONS_IGNORE = "ignore";
	public static final String OTHER_QUESTIONS_NORMAL_VALUE_COVERED = "normalValueCovered";
	public static final String ESTABLISHED_THRESHOLD = "establishedThreshold";
	public static final String SUGGESTED_THRESHOLD = "suggestedThreshold";
	public static final String MIN_SUPPORT = "minSupport";
	public static final String DESCRIPTION = "description";

	private static DefaultMarkup MARKUP = null;

	static {
		MARKUP = new DefaultMarkup("CoveringList");
		MARKUP.addContentType(new CoveringList());
		MARKUP.addAnnotation(ESTABLISHED_THRESHOLD, false);
		MARKUP.addAnnotation(SUGGESTED_THRESHOLD, false);
		MARKUP.addAnnotation(MIN_SUPPORT, false);
		PackageManager.addPackageAnnotation(MARKUP);
		MARKUP.addAnnotation(DESCRIPTION, false);
		MARKUP.addAnnotation(OTHER_QUESTIONS, false,
				OTHER_QUESTIONS_IGNORE, OTHER_QUESTIONS_NORMAL_VALUE_COVERED);
	}

	public CoveringListMarkup() {
		super(MARKUP);
	}
}
