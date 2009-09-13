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

package de.d3web.we.kdom.table.xcl;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.table.TableColumnContext;

/**
 * @author jochen
 * 
 * A context allowing a cell to easily access the column attributes (solution) 
 * (which cannot easily be provided be the KDOM structure itself, because tables are split 
 * by lines) 
 *
 */
public class SolutionColumnContext extends TableColumnContext {
	
	private String solution;

	public SolutionColumnContext(Section table, int colNumber, String solution) {
		super(table, colNumber);
		this.solution = solution;
	}

	public String getSolution() {
		return solution;
	}
	
	

}
