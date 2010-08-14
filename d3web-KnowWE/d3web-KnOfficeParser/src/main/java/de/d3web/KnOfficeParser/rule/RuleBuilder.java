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

package de.d3web.KnOfficeParser.rule;

import java.util.List;

/**
 * Inteface für Builder des Complexrules Parser
 * 
 * @author Markus Friedrich
 * 
 */
public interface RuleBuilder {

	void indicationrule(int line, String linetext, List<String> names,
			List<String> types, boolean except, boolean instant, boolean not);

	void suppressrule(int line, String linetext, String qname, String type,
			List<String> anames, boolean except);

	void numValue(int line, String linetext, boolean except, String op);

	void questionOrDiagnosis(int line, String linetext, String s, String type);

	void choiceOrDiagValue(int line, String linetext, String op, String value,
			boolean except);

	void formula(int line, String linetext, String value);

	void formulaAdd();

	void formulaSub();

	void formulaMult();

	void formulaDiv();

}
