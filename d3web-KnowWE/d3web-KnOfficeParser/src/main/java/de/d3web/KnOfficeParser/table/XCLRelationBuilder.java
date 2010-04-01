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

package de.d3web.KnOfficeParser.table;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import de.d3web.report.Message;
import de.d3web.xcl.XCLModel;
import de.d3web.xcl.XCLRelationType;
import de.d3web.KnOfficeParser.util.MessageKnOfficeGenerator;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.IDObjectManagement;

/**
 * Erstellt XCL Relationen aus einer Tabellenzelle
 * 
 * @author Markus Friedrich
 * 
 */
public class XCLRelationBuilder implements CellKnowledgeBuilder {

	private ResourceBundle properties;
	private boolean createUncompleteFindings = true;

	public boolean isCreateUncompleteFindings() {
		return createUncompleteFindings;
	}

	public void setCreateUncompleteFindings(boolean createUncompleteFindings) {
		this.createUncompleteFindings = createUncompleteFindings;
	}

	public XCLRelationBuilder(String file) {
		if (file != null) {
			properties = ResourceBundle.getBundle(file);
		}
	}

	@Override
	public Message add(IDObjectManagement idom, int line, int column,
			String file, Condition cond, String text, Solution diag,
			boolean errorOccured) {
		
		if (!createUncompleteFindings) {
			if (errorOccured) {
				System.out.println(text);
				return null;
			}
		}
		String s;
		if (text!=null) {
			try {
				s = properties.getString(text);
			}
			catch (MissingResourceException e) {
				s = text;
			}
			catch (ClassCastException e) {
				s = text;
			}
		} else {
			s = text;
		}
		String relationID;
		
		if (s.equals("--")) {
			relationID = XCLModel.insertXCLRelation(idom.getKnowledgeBase(), cond, diag,
					XCLRelationType.contradicted, file);
		} else if (s.equals("!")) {
			relationID = XCLModel.insertXCLRelation(idom.getKnowledgeBase(), cond, diag,
					XCLRelationType.requires, file);
		} else if (s.equals("++")) {
			relationID = XCLModel.insertXCLRelation(idom.getKnowledgeBase(), cond, diag,
					XCLRelationType.sufficiently, file);
		} else {
			Double value;
			try {
				value = Double.parseDouble(s);
			} catch (NumberFormatException e) {
				return MessageKnOfficeGenerator.createNoValidWeightException(
						file, line, column, "", text);
			}
			relationID = XCLModel.insertXCLRelation(idom.getKnowledgeBase(), cond, diag,
					XCLRelationType.explains, value, file);

		}
		return new Message("relID:"+relationID);
	}

}
