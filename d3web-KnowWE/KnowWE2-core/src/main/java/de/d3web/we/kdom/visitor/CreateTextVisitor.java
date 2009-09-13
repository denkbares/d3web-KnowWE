/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom.visitor;

import java.util.List;

import de.d3web.we.kdom.PlainText;
import de.d3web.we.kdom.Section;

public class CreateTextVisitor implements Visitor {
	
	private static CreateTextVisitor instance;
	
	public static synchronized  CreateTextVisitor getInstance() {
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
		throws CloneNotSupportedException
	  {
	    throw new CloneNotSupportedException(); 	   
	  }
	
	private StringBuffer buffi;

	@Override
	public void visit(Section s) {
		buffi = new StringBuffer();
		renderSubtree(s, buffi);

	}

	public String getText() {
		return buffi.toString();
	}

	private void renderSubtree(Section s, StringBuffer buffi) {
		if (s.getObjectType() instanceof PlainText) {
			buffi.append(s.getOriginalText());
		}
		List<Section> children = s.getChildren();

		for (Section section : children) {
			renderSubtree(section, buffi);
		}

	}

}
