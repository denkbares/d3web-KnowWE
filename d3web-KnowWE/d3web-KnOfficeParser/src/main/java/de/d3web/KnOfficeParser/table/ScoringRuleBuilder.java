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

import java.util.ResourceBundle;

import de.d3web.report.Message;
import de.d3web.scoring.Score;
import de.d3web.KnOfficeParser.util.MessageKnOfficeGenerator;
import de.d3web.KnOfficeParser.util.Scorefinder;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.AbstractCondition;
import de.d3web.core.manage.IDObjectManagement;
import de.d3web.core.manage.RuleFactory;
import de.d3web.core.terminology.Diagnosis;
/**
 * Erstellet Scoring Rules aus Tabellenzellen
 * @author Markus Friedrich
 *
 */
public class ScoringRuleBuilder implements CellKnowledgeBuilder {

	private ResourceBundle properties;
	
	public ScoringRuleBuilder(String file) {
		properties = ResourceBundle.getBundle(file);
	}
	
	@Override
	public Message add(IDObjectManagement idom, int line, int column, String file,
			AbstractCondition cond, String text, Diagnosis diag, boolean errorOccured) {
		Score score = Scorefinder.getScore(text);
		if (score==null) {
			String s;
			try {
				s = properties.getString(text);
				score = Scorefinder.getScore(s);
			} catch (Exception e) {
				return MessageKnOfficeGenerator.createScoreDoesntExistError(file, line, column, "", text);
			}
		}
		String newRuleID = idom.findNewIDFor(Rule.class);
		RuleFactory.createHeuristicPSRule(newRuleID, diag, score, cond);
		return null;
	}

}
