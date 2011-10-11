/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

package de.knowwe.d3web.event;

import java.util.Collection;

import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionDate;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionText;
import de.d3web.core.session.Value;
import de.d3web.core.session.values.ChoiceID;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.core.session.values.DateValue;
import de.d3web.core.session.values.MultipleChoiceValue;
import de.d3web.core.session.values.NumValue;
import de.d3web.core.session.values.TextValue;
import de.d3web.core.session.values.Unknown;
import de.knowwe.core.event.Event;

/**
 * The Finding Set Event. Which will be fired each time a finding is set in the
 * embedded dialog.
 * 
 * @author Sebastian Furth
 * 
 */
public class FindingSetEvent extends Event {

	private final Question question;
	private Value value;
	private final String namespace;
	private final String web, user;

	/**
	 * Standard Constructor which encapsulates a question and the applied value.
	 * 
	 * @param question the underlying question
	 * @param value the applied value
	 * @param namespace defines in which session the value was set (optional!)
	 */
	public FindingSetEvent(Question question, Value value, String namespace, String web, String user) {

		this.web = web;
		this.user = user;
		// Check parameters for validity
		if (question == null || value == null) throw new IllegalArgumentException(
				"Paramters mustn't be null!");
		else if (value instanceof Unknown) this.value = Unknown.getInstance();
		else if (question instanceof QuestionChoice) {

			// TODO check with Joba if it's correct to check for HashSet here
			// rather than for List<?> as it was before!
			// Please also cross-check SetSingleFindingAction

			if (question instanceof QuestionMC && value instanceof MultipleChoiceValue) {
				Collection<ChoiceID> choiceIDs = ((MultipleChoiceValue) value).getChoiceIDs();
				for (ChoiceID cv : choiceIDs) {
					Choice choice = cv.getChoice((QuestionChoice) question);
					if (choice == null) {
						throw new IllegalArgumentException(
								"choice " + value +
										" is not an allowed value for "
										+ question.getName());
					}
				}
			}
			else {
				Choice choice = ((ChoiceValue) value).getChoice((QuestionChoice) question);
				if (choice == null) {
					throw new IllegalArgumentException(
							value + " is not an allowed value for " +
									question.getName());
				}
			}
		}
		else if (question instanceof QuestionNum && !(value instanceof NumValue)) throw new IllegalArgumentException(
				"The committed value must be a NumValue!");
		else if (question instanceof QuestionDate && !(value instanceof DateValue)) throw new IllegalArgumentException(
				"The committed value must be an DateValue!");
		else if (question instanceof QuestionText && !(value instanceof TextValue)) throw new IllegalArgumentException(
				"The committed value must be an TextValue!");

		// Set parameters
		this.question = question;
		this.value = value;
		this.namespace = namespace;
	}

	public Question getQuestion() {
		return question;
	}

	public Value getValue() {
		return value;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getWeb() {
		return web;
	}

	public String getUsername() {
		return user;
	}

}
