/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
package de.knowwe.timeline.tree;

import de.knowwe.timeline.Timeset;
import de.knowwe.timeline.parser.ASTand_op;
import de.knowwe.timeline.parser.ASTor_op;
import de.knowwe.timeline.parser.ASTquery;
import de.knowwe.timeline.parser.ASTsimpleElement;
import de.knowwe.timeline.parser.ASTtimeFilter;
import de.knowwe.timeline.parser.QueryLangVisitor;
import de.knowwe.timeline.parser.SimpleNode;

/**
 * 
 * @author Tobias Bleifuss, Steffen Hoefner
 */
public abstract class AbstractVisitor implements QueryLangVisitor {

	@Override
	public Timeset visit(SimpleNode node, Object data) {
		return null;
	}

	@Override
	public Timeset visit(ASTquery node, Object data) {
		node.childrenAccept(this, data);
		return null;
	}

	@Override
	public Timeset visit(ASTor_op node, Object data) {
		node.childrenAccept(this, data);
		return null;
	}

	@Override
	public Timeset visit(ASTand_op node, Object data) {
		node.childrenAccept(this, data);
		return null;
	}

	@Override
	public Timeset visit(ASTsimpleElement node, Object data) {
		node.childrenAccept(this, data);
		return null;
	}

	@Override
	public Timeset visit(ASTtimeFilter node, Object data) {
		node.childrenAccept(this, data);
		return null;
	}

}