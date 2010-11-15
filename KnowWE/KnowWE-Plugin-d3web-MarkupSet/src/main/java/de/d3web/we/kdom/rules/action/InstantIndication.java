/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.we.kdom.rules.action;

import java.util.ArrayList;
import java.util.List;

import de.d3web.core.inference.PSAction;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.indication.ActionInstantIndication;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rules.action.ContraIndicationAction.QuestionReferenceInBrackets;
import de.d3web.we.object.QuestionReference;

/**
 * 
 * @author Jochen
 * @created 30.07.2010
 */
public class InstantIndication extends BracketsAction<InstantIndication> {

	public InstantIndication() {
		super(new String[] {
				"INSTANT", "SOFORT" });

	}

	@Override
	protected KnowWEObjectType getObjectReference() {
		return new QuestionReferenceInBrackets();
	}

	@Override
	public PSAction createAction(KnowWEArticle article, Section<InstantIndication> s) {
		Section<QuestionReference> qSec = s.findSuccessor(QuestionReference.class);
		Question termObject = qSec.get().getTermObject(article, qSec);

		ActionInstantIndication actionContraIndication = new ActionInstantIndication();
		List<QASet> obs = new ArrayList<QASet>();
		obs.add(termObject);
		actionContraIndication.setQASets(obs);
		return actionContraIndication;
	}

}
