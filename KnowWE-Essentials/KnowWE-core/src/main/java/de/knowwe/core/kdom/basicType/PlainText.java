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

package de.knowwe.core.kdom.basicType;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.DefaultTextRenderer;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.core.report.CompilerMessage;

/**
 * This type is the terminal-type of the Knowledge-DOM. All leafs and only the
 * leafs of the KDMO-tree are of this type. If a type has no findings for the
 * allowed children or has no allowed children one son of this type is created
 * to end the recursion.
 *
 * @author Jochen
 */
public class PlainText extends AbstractType {

	private static PlainText instance;

	public static synchronized PlainText getInstance() {
		if (instance == null) {
			instance = new PlainText();
		}
		return instance;
	}

	public PlainText() {
		this.setRenderer(DefaultTextRenderer.getInstance());
		this.setSectionFinder(AllTextFinder.getInstance());
	}

	/**
	 * prevent cloning
	 */
	@Override
	public Object clone()
			throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}
