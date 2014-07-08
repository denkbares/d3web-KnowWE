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

package de.knowwe.kdom.visitor;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.basicType.PlainText;
import de.knowwe.core.kdom.parsing.Section;

import java.util.List;

public class CreateTextVisitor {

	private static CreateTextVisitor instance;

	public static synchronized CreateTextVisitor getInstance() {
		if (instance == null) {
			instance = new CreateTextVisitor();

		}

		return instance;
	}

	/**
	 * prevent cloning
	 */
	@Override
	public Object clone()
			throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	private StringBuffer buffi;

	public void visit(Section<? extends Type> s) {
		buffi = new StringBuffer();
		renderSubtree(s, buffi);

	}

	public String getText() {
		return buffi.toString();
	}

	private void renderSubtree(Section<? extends Type> s, StringBuffer buffi) {
		if (s.get() instanceof PlainText) {
			buffi.append(s.getText());
		}
		List<Section<?>> children = s.getChildren();

		for (Section<? extends Type> section : children) {
			renderSubtree(section, buffi);
		}

	}

}
