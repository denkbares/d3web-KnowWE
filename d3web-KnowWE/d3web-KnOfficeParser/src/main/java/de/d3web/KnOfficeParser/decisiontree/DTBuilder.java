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

package de.d3web.KnOfficeParser.decisiontree;
import java.util.List;

/**
 * Dieses Interface muss implementiert werden, um die Klasse dem Entscheidungsbaumparser mitzugeben
 * und somit den Output zu erzeugen
 * @author Markus Friedrich
 *
 */
public interface DTBuilder {
	
	void newLine();
	
	void line(String text);
	
	void addQuestionclass(String name, int line, String linetext, List<String> attributes, List<String> values);
	
	void addQuestion(int dashes, String name, String longname, boolean abs, String type, String ref, Double lowerbound, Double upperbound, String unit, List<String> syn, int line, String linetext, String idlink, List<String> attributes, List<String> values);
	
	void addAnswerOrQuestionLink(int dashes, String name, String ref, List<String> syn, boolean def, boolean init, int line, String linetext, String idlink);
	
	void addDiagnosis(int dashes, List<String> diags, boolean set, String value, String link, String linkdes, int line, String linetext, String idlink);
	
	void addNumericAnswer(int dashes, Double a, Double b, String op, int line, String linetext);
	
	void addDescription(String id, String type, String des, String text, int line, String linetext, String language);
	
	void addInclude(String url, int line, String linetext);
	
	void addQuestionLink(int dashes, String name, int line, String linetext);

	void setallowedNames(List<String> allowedNames, int line, String linetext);
	
	public void finishOldQuestionsandConditions(int dashes);

	void addManyQuestionClassLink(int dashes, List<String> qcs, int line,
			String string);

}
