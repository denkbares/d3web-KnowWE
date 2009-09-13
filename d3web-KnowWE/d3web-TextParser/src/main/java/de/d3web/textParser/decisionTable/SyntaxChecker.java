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

/* Created on 25. Januar 2005, 19:37 */
package de.d3web.textParser.decisionTable;

import de.d3web.report.Report;

/**
 * @author  Andreas Klar
 */
public interface SyntaxChecker {
    
    /**
     * Checks the syntax of a table, which means the existence of all necessary values and
     * the absence of values in cells which are not supposed to contain a value.
     * @param table table to be proofed
     * @return List of all found errors. If no errors are found the list will be empty
     */
    public abstract Report checkSyntax(DecisionTable table);
    
}
