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

package de.d3web.KnOfficeParser;

import java.util.List;

/**
 * Interface, welches implementiert werden muss, um Conditionen aus Grammatiken zu erzeugen, welche ComplexCondition.g importieren
 * @author Markus Friedrich
 *
 */
public interface ConditionBuilder {
	void condition(int line, String linetext, String qname, String type, String op, String value);
	void condition(int line, String linetext, String qname, String type, double left, double right, boolean in);
	void knowncondition(int line, String linetext, String name, String type, boolean unknown);
	void notcond(String text);
	void andcond(String text);
	void orcond(String text);
	void minmax(int line, String linetext, int min, int max, int anzahlcond);
	void in(int line, String linetext, String question, String type, List<String> answers);
	void all(int line, String linetext, String question, String type, List<String> answers);
	void complexcondition(String text);
	
	
}
