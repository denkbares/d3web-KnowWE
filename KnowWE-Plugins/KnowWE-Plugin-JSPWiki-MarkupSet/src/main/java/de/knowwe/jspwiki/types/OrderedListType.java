/*
 * Copyright (C) 2012 denkbares GmbH
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
package de.knowwe.jspwiki.types;

import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Types;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.kdom.dashtree.DashSubtree;
import de.knowwe.kdom.dashtree.DashTree;

/**
 * 
 * @author Lukas Brehl
 * @created 10.08.2012
 */
public class OrderedListType extends AbstractType {

	public OrderedListType() {
		Pattern pattern = Pattern.compile("(^|\n+)(#.+?)(?=\n[^(#)]|\\z)",
				Pattern.MULTILINE + Pattern.DOTALL);
		this.setSectionFinder(new RegexSectionFinder(pattern));
		DashTree dashTree = new DashTree('#', 1);
		Types.injectRendererToChildren(dashTree, DashSubtree.class, DelegateRenderer.getInstance());
		this.addChildType(dashTree);
	}
}
