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
package de.d3web.we.kdom.questionTree.indication;

import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.manage.RuleFactory;
import de.d3web.utils.Log;
import de.d3web.we.kdom.questionTree.NumericCondLine;
import de.d3web.we.kdom.questionTree.QuestionDashTree;
import de.d3web.we.kdom.questionTree.QuestionDashTreeUtils;
import de.d3web.we.kdom.questionTree.QuestionTreeAnswerDefinition;
import de.d3web.we.knowledgebase.D3webCompileScript;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.D3webTerm;
import de.d3web.we.object.QuestionDefinition;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.object.QuestionnaireReference;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.dashtree.DashTreeElement;
import de.knowwe.kdom.dashtree.DashTreeUtils;

public class IndicationHandler implements D3webCompileScript<D3webTerm<NamedObject>> {

	private final String indicationStoreKey = "INDICATION_STORE_KEY";

	private static IndicationHandler instance = null;

	public static IndicationHandler getInstance() {
		if (instance == null) {
			instance = new IndicationHandler();
		}
		return instance;
	}

	@Override
	public void destroy(D3webCompiler compiler, Section<D3webTerm<NamedObject>> section) {
		Rule kbr = (Rule) section.getObject(compiler,
				indicationStoreKey);
		if (kbr != null) kbr.remove();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void compile(D3webCompiler compiler, Section<D3webTerm<NamedObject>> section) throws CompilerMessage {

		Section<DashTreeElement> element = Sections.ancestor(section,
				DashTreeElement.class);

		if (element == null) {
			Log.warning("The " + this.getClass().getSimpleName()
							+ " only works inside "
							+ QuestionDashTree.class.getSimpleName()
							+ "s. It seems the handler is used with the wrong Type.");
			Messages.clearMessages(compiler, section, getClass());
			return;
		}

		Section<? extends DashTreeElement> dashTreeFather = DashTreeUtils
				.getParentDashTreeElement(element);

		if (dashTreeFather == null) {
			// In case, that the element is already root element, no indication
			// rule has to be defined, this a warning is not reasonable
			Messages.clearMessages(compiler, section, getClass());
			return;
			// return Arrays.asList((KDOMReportMessage) new
			// CreateRelationFailed(
			// D3webModule.getKwikiBundle_d3web().
			// getString("KnowWE.rulesNew.indicationnotcreated")
			// + " - no dashTreeFather found"));
		}

		Section<QuestionTreeAnswerDefinition> answerSec =
				Sections.successor(dashTreeFather, QuestionTreeAnswerDefinition.class);
		Section<NumericCondLine> numCondSec =
				Sections.successor(dashTreeFather, NumericCondLine.class);

		if (answerSec != null || numCondSec != null) {

			if (section.hasErrorInSubtree(compiler)) {
				Message msg = Messages.creationFailedWarning(
						D3webUtils.getD3webBundle().getString(
								"KnowWE.rulesNew.indicationnotcreated"));
				throw new CompilerMessage(msg);
			}

			// retrieve the QASet for the different Types that might
			// use this handler
			QASet qaset = null;
			@SuppressWarnings("rawtypes")
			Section<? extends D3webTerm> termRef = Sections.successor(element,
					D3webTerm.class);
			if (termRef != null) {
				if (termRef.get() instanceof QuestionnaireReference) {
					Section<QuestionnaireReference> qnref = (Section<QuestionnaireReference>) termRef;
					qaset = qnref.get().getTermObject(compiler, qnref);
				}
				else if (termRef.get() instanceof QuestionDefinition) {
					Section<QuestionDefinition> qdef = (Section<QuestionDefinition>) termRef;
					qaset = qdef.get().getTermObject(compiler, qdef);
				}
				else if (termRef.get() instanceof QuestionReference) {
					Section<QuestionReference> qref = (Section<QuestionReference>) termRef;
					qaset = qref.get().getTermObject(compiler, qref);
				}
			}

			if (qaset != null) {

				Condition cond = QuestionDashTreeUtils.createCondition(compiler,
						DashTreeUtils.getAncestorDashTreeElements(element));

				if (cond != null) {
					Rule r = RuleFactory.createIndicationRule(qaset, cond);

					if (r != null) {
						KnowWEUtils.storeObject(compiler, section, indicationStoreKey, r);
						return;
					}
				}
			}
			throw new CompilerMessage(
					Messages.creationFailedWarning(
							Rule.class.getSimpleName()));
		}
		throw new CompilerMessage();
	}

}
