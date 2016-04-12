/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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
package de.d3web.we.object;

import de.d3web.strings.Strings;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.sectionFinder.UnquotedExpressionFinder;

/**
 * Type representing the value 'Unknown'.
 * 
 * @author Reinhard Hatko
 * @created 18.06.2011
 */
public class UnknownValueType extends AbstractType {

	public UnknownValueType() {
		setSectionFinder(new UnquotedExpressionFinder("unknown", Strings.CASE_INSENSITIVE | Strings.UNQUOTED | Strings.SKIP_COMMENTS));
		setRenderer(StyleRenderer.OPERATOR);
	}

}
