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

package de.d3web.we.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.kernel.verbalizer.VerbalizationManager;
import de.d3web.kernel.verbalizer.VerbalizationManager.RenderingFormat;
import de.d3web.kernel.verbalizer.Verbalizer;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.D3webKnowledgeServiceSession;
import de.d3web.xcl.InferenceTrace;
import de.d3web.xcl.XCLModel;
import de.d3web.xcl.XCLRelation;
import de.d3web.xcl.inference.PSMethodXCL;

public class XCLExplanationAction extends DeprecatedAbstractKnowWEAction {

	// properties only
	private static final String SOLUTION = "SOLUTION";
	private static final String SCORE = "SCORE";
	private static final String SUPPORT = "SUPPORT";
	private static final String TITLE = "TITLE";

	// properties and template
	private static final String STATE = "STATE";
	private static final String KB = "KB";
	private static final String SUFFICIENTLY = "SUFFICIENTLY";
	private static final String CONTRADICTED = "CONTRADICTED";
	private static final String REQUIRED = "REQUIRED";
	private static final String REQUIREDFALSE = "REQUIREDFALSE";
	private static final String EXPLAINED = "EXPLAINED";
	private static final String NOTEXPLAINED = "NOTEXPLAINED";
	private static final String GIVENANSWER = "GIVENANSWER";
	private static final String EXPECTEDANSWER = "EXPECTEDANSWER";
	private static final String QUESTION = "QUESTION";

	private StringBuffer template;
	private ResourceBundle labels;
	private Map<String, Object> parameterMap;
	private String kbId;
	private Session currentCase;

	@Override
	public String perform(KnowWEParameterMap parameterMap) {

		ResourceBundle rb = D3webModule.getKwikiBundle_d3web(parameterMap.getRequest());

		String id = parameterMap.get(KnowWEAttributes.SESSION_ID);
		String solutionid = parameterMap.get(KnowWEAttributes.TERM);
		Broker broker = D3webModule.getBroker(parameterMap);
		D3webKnowledgeServiceSession serviceSession = broker.getSession()
				.getServiceSession(id);

		if (serviceSession instanceof D3webKnowledgeServiceSession) {

			// String namespace;
			//
			// namespace = parameterMap.get(KnowWEAttributes.TARGET);
			// if (namespace == null) {
			// namespace = parameterMap.get(KnowWEAttributes.NAMESPACE);
			// }

			D3webKnowledgeServiceSession d3webKSS = serviceSession;
			KnowledgeBaseManagement baseManagement = d3webKSS.getBaseManagement();

			Session c = d3webKSS.getSession();
			this.currentCase = c;
			this.kbId = c.getKnowledgeBase().getId();

			// Question q;
			// AbstractCondition c;
			// c.eval(c);
			Solution solution = baseManagement.findSolution(solutionid);
			if (solution == null) {
				return rb.getString("xclrenderer.nosolution") + solutionid;
			}

			Collection<KnowledgeSlice> models = c.getKnowledgeBase()
					.getAllKnowledgeSlicesFor(PSMethodXCL.class);
			for (KnowledgeSlice knowledgeSlice : models) {
				if (knowledgeSlice instanceof XCLModel) {
					if (((XCLModel) knowledgeSlice).getSolution().equals(
							solution)) {
						InferenceTrace trace = ((XCLModel) knowledgeSlice)
								.getInferenceTrace(c);
						if (trace == null) {
							return rb.getString("xclrenderer.notrace");
						}
						return verbalizeTrace(trace, solution.getName());
					}
				}
			}
		}

		return rb.getString("xclrenderer.findingtrace");
	}

	private String verbalizeTrace(InferenceTrace trace, String solution) {
		this.template = new StringBuffer();
		this.labels = ResourceBundle.getBundle("KnowWE_config", Locale.getDefault());
		loadTemplate();

		this.parameterMap = new HashMap<String, Object>();
		parameterMap.put(Verbalizer.IS_SINGLE_LINE, Boolean.TRUE);

		// Score and Support
		Double score = trace.getScore();
		Double support = trace.getSupport();
		String stateString = "no state found";
		if (trace.getState() != null) {
			stateString = trace.getState().toString();
		}
		renderState(stateString, roundDouble(score), roundDouble(support), solution);

		// sufficiently derived by
		Collection<XCLRelation> suff = trace.getSuffRelations();
		renderTable(SUFFICIENTLY, suff);

		// is contradicted by
		Collection<XCLRelation> contr = trace.getContrRelations();
		renderTable(CONTRADICTED, contr);

		// is required by
		Collection<XCLRelation> reqPos = trace.getReqPosRelations();
		renderTable(REQUIRED, reqPos);

		// is required by NOT FULFILLED
		Collection<XCLRelation> reqNeg = trace.getReqNegRelations();
		renderTable(REQUIREDFALSE, reqNeg);

		// explains
		Collection<XCLRelation> relPos = trace.getPosRelations();
		renderTable(EXPLAINED, relPos);

		// explains negative
		Collection<XCLRelation> relNeg = trace.getNegRelations();
		renderTable(NOTEXPLAINED, relNeg);

		return this.template.toString();
	}

	/**
	 * method replaces wild card in template with corresponding table (HTML
	 * syntax)
	 * 
	 * @param type the table type
	 * @param content the content of the table
	 */
	private void renderTable(String type, Collection<XCLRelation> content) {
		StringBuffer table = new StringBuffer();
		if (content != null && content.size() > 0) {

			if (!type.equals(NOTEXPLAINED)) {
				if (!type.equals(CONTRADICTED)) {
					table.append("<table>");
				}
				else {
					table.append("<table class=emphasized>");
				}
				table.append("<thead><tr><th>" + this.labels.getString(type) + "</th></tr></thead>");
				table.append("<tbody>" + renderContent(content) + "</tbody></table>");
			}
			else {
				table.append("<table class=emphasized><thead><tr><th colspan=3>"
						+ this.labels.getString(type) + "</th></tr></thead>");
				table.append("<tbody>" + renderContentNotExplained(content) + "</tbody></table>");
			}
		}
		replaceWildcard(type, table.toString());
	}

	/**
	 * Renders the table content, new row for every answer
	 * 
	 * @param content the answers to be rendered
	 * @return a table representation of the content
	 */
	private String renderContent(Collection<XCLRelation> content) {
		StringBuffer cont = new StringBuffer();

		for (XCLRelation rel : content) {
			Condition cond = rel.getConditionedFinding();
			cont.append("<tr><td>"
					+ VerbalizationManager.getInstance().verbalize(cond,
							RenderingFormat.HTML, parameterMap) + "</td></tr>");
		}
		return cont.toString();
	}

	/**
	 * Content for "Not Explained" has to be rendered differently since it
	 * contains the question, the answer given and the expected answer
	 * (verbalized condition)
	 * 
	 * @param content the content to get rendered
	 * @return a table representation of the content
	 */
	private String renderContentNotExplained(Collection<XCLRelation> content) {
		StringBuffer cont = new StringBuffer();
		cont.append("<tr class=emphasized><td>" + this.labels.getString(QUESTION) + "</td>");
		cont.append("<td>" + this.labels.getString(GIVENANSWER) + "</td>");
		cont.append("<td class=emphasized>" + this.labels.getString(EXPECTEDANSWER) + "</td></tr>");
		for (XCLRelation rel : content) {
			cont.append("<tr><td>");
			Condition cond = rel.getConditionedFinding();
			List<? extends TerminologyObject> questions = cond.getTerminalObjects();
			ListIterator<? extends TerminologyObject> condIt = questions.listIterator();
			List<Question> askedQuestions = new ArrayList<Question>();
			int count = 0;
			Question cq = null;
			while (condIt.hasNext()) {
				if (count > 0) {
					cont.append("<br>");
				}
				cq = (Question) condIt.next();
				if (!askedQuestions.contains(cq)) {
					cont.append(cq.getName());
					count = count + 1;
					askedQuestions.add(cq);
				}
			}
			cont.append("</td><td>");
			Value theanswer = this.currentCase.getBlackboard().getValue(cq);
			cont.append(theanswer.getValue());

			// ListIterator iterator = answers.listIterator();
			// count = 0;
			// while(iterator.hasNext()){
			// if(count > 0){
			// cont.append("<br>");
			// }
			// Answer a = (Answer) iterator.next();
			// cont.append(a.getValue(this.currentCase));
			// count = count+1;
			// }

			cont.append("</td>");

			cont.append("<td class=emphasized>"
					+ VerbalizationManager.getInstance().verbalize(cond,
							RenderingFormat.HTML, parameterMap) + "</td></tr>");
		}
		return cont.toString();
	}

	/**
	 * Replaces a wildcard in the generated template with the actual (rendered)
	 * content
	 * 
	 * @param type the wildcard to be replaced
	 * @param renderedTable the content to be inserted
	 */
	private void replaceWildcard(String type, String renderedTable) {
		int start = this.template.indexOf(type);
		if (start != -1) {
			int end = start + type.length();
			this.template.replace(start, end, renderedTable);
		}
	}

	/**
	 * Renders the state of the explanation
	 * 
	 * @param state the state to be rendered
	 * @param score the explanation score
	 * @param support the explanation support
	 */
	private void renderState(String state, Double score, Double support, String solution) {
		StringBuffer cont = new StringBuffer();
		if (score != null && support != null) {
			cont.append("<table><thead><tr><th colspan=2>" + this.labels.getString(SOLUTION) + ":"
					+ solution + "</th></tr></thead>");
			cont.append("<tbody><tr><td colspan=2><strong>" + this.labels.getString(STATE)
					+ " : </strong>" + state + "</td></tr>");
			cont.append("<tr><td colspan=2><strong>" + this.labels.getString(KB) + " : </strong>"
					+ this.kbId + "</td></tr>");
			cont.append("<tr><td><strong>" + this.labels.getString(SCORE) + " : </strong>" + score
					+ "</td>"
					+ "<td><strong>" + this.labels.getString(SUPPORT) + " : </strong>" + support
					+ "</td></tr></tbody></table>");
		}
		replaceWildcard(STATE, cont.toString());
	}

	/**
	 * Generates a template with wildcards
	 */
	private void loadTemplate() {
		this.template = new StringBuffer();
		this.template.append("<html><head><title>"
				+ this.labels.getString(TITLE)
				+ "</title>"
				+ "<link rel=\"stylesheet\" type=\"text/css\" href=\"KnowWEExtension/css/general.css\"/>"
				+ "<script type=\"text/javascript\" src=\"KnowWEExtension/scripts/KnowWE-helper.js\">"
				+ "<script type=\"text/javascript\" src=\"KnowWEExtension/scripts/KnowWE.js\">"
				+ "</script></head>");
		this.template.append("<body><div id=\"popup-xcle\">");
		this.template.append(STATE);
		this.template.append(SUFFICIENTLY);
		this.template.append(CONTRADICTED);
		this.template.append(REQUIRED);
		this.template.append(REQUIREDFALSE);
		this.template.append(EXPLAINED);
		this.template.append(NOTEXPLAINED);
		this.template.append("</div></body></html>");
	}

	private Double roundDouble(Double d) {
		return d = Math.round(d * 100.) / 100.;
	}

}
