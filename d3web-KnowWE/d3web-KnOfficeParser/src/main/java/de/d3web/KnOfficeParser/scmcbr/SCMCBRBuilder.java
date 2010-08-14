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

package de.d3web.KnOfficeParser.scmcbr;

import java.util.List;

/**
 * Builderinterface für den SCMCR Parser
 * 
 * @author Markus Friedrich
 * 
 */
public interface SCMCBRBuilder {

	void solution(int line, String text, String name);

	void question(int line, String text, String name);

	void setAmount(int line, String text, String name, Double value);

	void threshold(int line, String text, String name, Double value1,
			Double value2);

	void questionclass(int line, String text, String name);

	void answer(int line, String text, String name, String weight, String operator);

	void in(int line, String text, List<Double> values1, List<Double> values2, boolean startincluded,
			boolean endincluded);

	void into(int line, String text, List<Double> values1, List<Double> values2,
			boolean startincluded, boolean endincluded);

	void and(int line, String text, List<String> names, String weight,
			boolean or);

	void not(int line, String text, String name, String weight);

}
