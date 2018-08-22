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

package de.d3web.we.kdom.rules;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.basicType.CommentLineType;
import de.knowwe.core.kdom.basicType.UnrecognizedSyntaxType;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;

/**
 * A type for the content of the RuleMarkup-block. It allocates all the text
 * tries to create Rules from the content
 * 
 * @author Jochen Reutelshöfer, Albrecht Striffler (denkbares GmbH)
 */
public class RuleContentType extends AbstractType {

	/**
	 * Here the type is configured. It takes (mostly) all the text it gets. A
	 * ConditionActionRule-type is initialized and inserted as child-type (which
	 * itself gets a child-type: ConditionActionRuleContent).
	 * 
	 */
	public RuleContentType() {
		// take nearly all the text that is passed (kind of trimmed)
		this.setSectionFinder(AllTextFinder.getInstance());
		this.addChildType(new RuleType());

		this.addChildType(new CommentLineType());
		// everything that remains will be unrecognized syntax
		this.addChildType(UnrecognizedSyntaxType.getInstance());
	}

}
