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
package de.d3web.we.object;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.kdom.sectionFinder.OneOfStringFinder;

public class ScoreValue extends AbstractType {

	public enum ScoreKey {
		N7, N6, N5, N4, N3, N2, N1, N0, P0, P1, P2, P3, P4, P5, P6, P7, ESTABLISHED, ETABLIERT,
		SUGGESTED, VERDAECHTIGT
	};

	protected String[] values;

	public ScoreValue() {
		values = new String[ScoreKey.values().length];
		for (int i = 0; i < ScoreKey.values().length; i++) {
			values[i] = ScoreKey.values()[i].toString();
		}

		this.setSectionFinder(new OneOfStringFinder(values));
	}

}
