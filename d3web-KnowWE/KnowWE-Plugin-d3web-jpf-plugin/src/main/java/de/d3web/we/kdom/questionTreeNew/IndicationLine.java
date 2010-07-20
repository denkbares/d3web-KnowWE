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

package de.d3web.we.kdom.questionTreeNew;


import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.objects.QuestionnaireReference;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.subtreeHandler.Priority;

public class IndicationLine extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		this.sectionFinder = new AllTextFinderTrimmed();

		QuestionnaireReference qc = new QuestionnaireReference();
		qc.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR7));
		qc.setSectionFinder(new AllTextFinderTrimmed());
		// qc.addSubtreeHandler(Priority.LOW, new CreateIndication());
		qc.addSubtreeHandler(Priority.LOW, IndicationHandler.getInstance());
		this.childrenTypes.add(qc);
	}

	// static class CreateIndication extends
	// D3webSubtreeHandler<QuestionnaireRef> {
	//
	// private final String indicationStoreKey = "INDICATION_STORE_KEY";
	//
	// @Override
	// public boolean needsToCreate(KnowWEArticle article,
	// Section<QuestionnaireRef> s) {
	//
	// return super.needsToCreate(article, s)
	// || DashSubtree.subtreeAncestorHasNotReusedObjectDefs(article, s);
	// }
	//
	// @Override
	// public Collection<KDOMReportMessage> create(KnowWEArticle article,
	// Section<QuestionnaireRef> s) {
	// KnowledgeBaseManagement mgn = getKBM(article);
	//
	// // current DashTreeElement
	// Section<DashTreeElement> element = KnowWEObjectTypeUtils
	// .getAncestorOfType(s, DashTreeElement.class);
	//
	// String name = s.get().getTermName(s);
	//
	// QContainer qc = mgn.findQContainer(name);
	//
	// if (qc != null) {
	// String newRuleID = mgn.createRuleID();
	// Condition cond = Utils.createCondition(article,
	// DashTreeElement.getDashTreeAncestors(element));
	// if (cond != null) {
	// Rule r = RuleFactory.createIndicationRule(newRuleID, qc,
	// cond);
	// KnowWEUtils.storeSectionInfo(article, s, indicationStoreKey, r);
	// if (r != null) {
	// return Arrays.asList((KDOMReportMessage) new
	// ObjectCreatedMessage(r.getClass()
	// + " : " + r.getId()));
	// }
	//
	// }
	// return Arrays.asList((KDOMReportMessage) new
	// CreateRelationFailed(Rule.class
	// .getSimpleName()));
	// } else {
	//
	// return Arrays.asList((KDOMReportMessage) new NoSuchObjectError(name));
	// }
	//
	// }
	//
	// @Override
	// public boolean needsToDestroy(KnowWEArticle article,
	// Section<QuestionnaireRef> s) {
	// return super.needsToDestroy(article, s)
	// || DashSubtree.subtreeAncestorHasNotReusedObjectDefs(article, s);
	// }
	//
	// @Override
	// public void destroy(KnowWEArticle article, Section<QuestionnaireRef>
	// rule) {
	// Rule kbr = (Rule) KnowWEUtils.getObjectFromLastVersion(article, rule,
	// indicationStoreKey);
	// if (kbr != null) kbr.remove();
	// }
	//
	// }
}
