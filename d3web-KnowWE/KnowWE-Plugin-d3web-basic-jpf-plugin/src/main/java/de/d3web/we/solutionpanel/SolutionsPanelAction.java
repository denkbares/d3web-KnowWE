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

package de.d3web.we.solutionpanel;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.knowledge.terminology.Rating.State;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.Session;
import de.d3web.utilities.ISetMap;
import de.d3web.we.action.AbstractAction;
import de.d3web.we.action.ActionContext;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.basic.Information;
import de.d3web.we.basic.InformationType;
import de.d3web.we.basic.SolutionState;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.core.knowledgeService.KnowledgeService;
import de.d3web.we.terminology.term.Term;
import de.d3web.we.terminology.term.TermInfoType;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.utils.KnowWERenderUtils;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.xcl.InferenceTrace;
import de.d3web.xcl.XCLModel;
import de.d3web.xcl.inference.PSMethodXCL;

public class SolutionsPanelAction extends AbstractAction {

	private final String iconURL;

	private ResourceBundle rb;

	private final ResourceBundle configRB;

	private final DecimalFormat dc = new DecimalFormat("0.0#");

	public SolutionsPanelAction() {
		configRB = ResourceBundle.getBundle("KnowWE_config");

		String path = "";

		iconURL = path + configRB.getString("knowwe.config.path.images") + "tree/";
	}

	@Override
	public void execute(ActionContext context) throws IOException {

		rb = D3webModule.getKwikiBundle_d3web(context.getKnowWEParameterMap().getRequest());

		String web = context.getParameter(KnowWEAttributes.WEB);
		String user = context.getParameter(KnowWEAttributes.USER);
		String index = context.getParameter("ArticleSelection");
		String topic = context.getKnowWEParameterMap().getTopic();
		if (index == null || index.equals("")) {
			index = "0";
		}
		int i = Integer.parseInt(index);
		SolutionPanelTagHandler.setSelected(web, user, i);

		context.setContentType("text/html; charset=UTF-8");

		if (configRB.getString("dps.active").equals("true")) {
			context.getWriter().write(renderSolutionStates(web, user, topic, i));
		}
		else {
			context.getWriter().write(renderSolutionStatesNonDPS(web, user, topic, i));
		}
	}

	// public String renderSolutionStates2(String web, String user, String
	// topic, int index) {
	// D3webUtils.getSession(topic, user, web)
	// return sb.toString();
	// }

	private String renderSolutionStatesNonDPS(String web, String user, String topic, int index) {
		StringBuffer sb = new StringBuffer();

		List<Solution> established = new ArrayList<Solution>();
		List<Solution> suggested = new ArrayList<Solution>();
		List<Solution> excluded = new ArrayList<Solution>();
		List<Solution> unclear = new ArrayList<Solution>();

		Collection<Session> sessions = new ArrayList<Session>();

		if (index == 0) {
			sessions = D3webUtils.getSessions(user, web);
		}
		else if (index == 1) {
			sessions.add(D3webUtils.getSession(topic, user, web));

		}
		else {
			sessions.add(D3webUtils.getSession(SolutionPanelTagHandler
					.getArticleNames(web, user).get(index - 2), user, web));
		}

		for (Session session : sessions) {
			established.addAll(session.getBlackboard().getSolutions(State.ESTABLISHED));
			suggested.addAll(session.getBlackboard().getSolutions(State.SUGGESTED));
			excluded.addAll(session.getBlackboard().getSolutions(State.EXCLUDED));

			// take unclears in M.Freiberg Aug/2010
			// unclear.addAll(session.getBlackboard().getSolutions(State.UNCLEAR));
		}

		boolean painted = false;
		if (!established.isEmpty()) {
			sb.append("<div>");
			sb.append("<strong>"
					+ rb.getString("KnowWE.solution.establishedSolutions")
					+ ":</strong>");
			sb.append(getSolutionLinkListNonDPS(user, web, sessions, State.ESTABLISHED));
			sb.append("</div>");
			painted = true;
		}

		if (!suggested.isEmpty()) {
			if (painted) {
				sb.append("<hr/>");
				painted = false;
			}
			sb.append("<div>");

			sb.append("<strong>"
					+ rb.getString("KnowWE.solution.suggestedSolutions")
					+ ":</strong>");
			// sb.append("</a>");
			sb.append(getSolutionLinkListNonDPS(user, web, sessions, State.SUGGESTED));
			sb.append("</div>");
			painted = true;
		}

		/*
		 * added unclear and excluded solutions to solution panel M. Freiberg
		 * Aug/2010
		 */
		/*
		 * if (!unclear.isEmpty()) { if (painted) { sb.append("<hr/>"); painted
		 * = false; } sb.append("<div>");
		 *
		 * sb.append("<strong>" +
		 * rb.getString("KnowWE.solution.unclearSolutions") + ":</strong>"); //
		 * sb.append("</a>"); sb.append(getSolutionLinkListNonDPS(user, web,
		 * sessions, State.UNCLEAR)); sb.append("</div>"); painted = true; }
		 */

		if (!excluded.isEmpty()) {
			if (painted) {
				sb.append("<hr/>");
				painted = false;
			}
			sb.append("<div>");

			sb.append("<strong>"
					+ rb.getString("KnowWE.solution.excludedSolutions")
					+ ":</strong>");
			// sb.append("</a>");
			sb.append(getSolutionLinkListNonDPS(user, web, sessions, State.EXCLUDED));
			sb.append("</div>");
			painted = true;
		}

		return sb.toString();
	}

	private StringBuffer getSolutionLinkListNonDPS(String user, String web, Collection<Session> sessions,
			State state) {
		StringBuffer sb = new StringBuffer();
		sb.append("<ul>");
		for (Session session : sessions) {
			for (Solution solution : session.getBlackboard().getSolutions(state)) {
				sb.append("<li>");
				String exactPrefix = KnowWEUtils.replaceUmlaut(solution.getName())
						+ "buttomLink";

				String nameSpace = session.getKnowledgeBase().getId();

				String topicName = nameSpace.substring(0, nameSpace.indexOf(".."));

				sb.append("<a href=\"Wiki.jsp?page=" + topicName + "\">"
						+ solution.getName() + "</a> ");

				String url = "encoding solution failed";
				url = "KnowWE.jsp?renderer=XCLExplanation&KWikiTerm="
									+ solution.getId()
									+ "&KWikisessionid=" + nameSpace + "&KWikiWeb=" + web
									+ "&KWikiUser=" + user;

				sb.append("<a href=\"#\" class=\"sstate-show-explanation\""
							+ " rel=\"{term : '" + solution.getId() + "', session : '" + nameSpace
						+ "', web : '" + web + "', user: '" + user + "'}\" ></a>");

				StringBuffer inner = new StringBuffer();
				inner.append(KnowWERenderUtils.getTopicLink(web, solution.getName(), iconURL,
						"dps", true, true));

				Collection<KnowledgeSlice> models = session.getKnowledgeBase()
						.getAllKnowledgeSlicesFor(PSMethodXCL.class);
				for (KnowledgeSlice knowledgeSlice : models) {
					if (knowledgeSlice instanceof XCLModel) {
						if (((XCLModel) knowledgeSlice).getSolution().equals(
								solution)) {
							InferenceTrace trace = ((XCLModel) knowledgeSlice)
									.getInferenceTrace(session);
							if (trace != null) {
								sb.append("<a href=\"#\" class=\"sstate-show-explanation\""
										+ " rel=\"{term : '" + solution.getName()
										+ "', session : '" + nameSpace + "', web : '" + web
										+ "', user: '" + user + "'}\" >\n"
										+ "<span title='"
										+ rb.getString("KnowWE.solution.degreeSC") + " / "
										+ rb.getString("KnowWE.sulution.recall")
										+ "'> [" + dc.format(trace.getScore()) + ":"
										+ dc.format(trace.getSupport())
										+ "]</span>\n</a>");
							}
						}
					}
				}

				sb.append("</li>");
			}
		}
		sb.append("</ul>");
		return sb;
	}

	// --------- DPS-METHODS BELOW THIS LINE ---------

	protected Comparator<Information> infComp = new InferenceComparator();

	private class DerivationComparator implements Comparator<Term> {

		private final ISetMap<Term, Information> assumptionMap;
		private final Broker broker;

		public DerivationComparator(ISetMap<Term, Information> assumptionMap,
				Broker b) {
			super();
			this.assumptionMap = assumptionMap;
			this.broker = b;
		}

		public int compare(Term o1, Term o2) {
			Collection<Information> dummy1 = broker.getSession()
					.getBlackboard().getInferenceInformation(o1);
			if (dummy1 == null || dummy1.isEmpty()) {
				return -1;
			}
			List<Information> infos1 = new ArrayList<Information>(dummy1);
			Collections.sort(infos1, infComp);

			Collection<Information> dummy2 = broker.getSession()
					.getBlackboard().getInferenceInformation(o2);
			if (dummy2 == null || dummy2.isEmpty()) {
				return 1;
			}
			List<Information> infos2 = new ArrayList<Information>(dummy2);
			Collections.sort(infos2, infComp);

			Information info1 = null;
			for (Information information : infos1) {
				if (information.getInformationType().equals(
						InformationType.XCLInferenceInformation)) {
					info1 = information;
				}
			}
			Information info2 = null;
			for (Information information : infos2) {
				if (information.getInformationType().equals(
						InformationType.XCLInferenceInformation)) {
					info2 = information;
				}
			}

			int res = new InferenceComparator().compare(info1, info2);
			if (res == 0) {
				int i1 = count(o1);
				int i2 = count(o2);
				int comp = i2 - i1;
				if (comp > 0) {
					return 1;
				}
				else if (comp < 0) {
					return -1;
				}
				else {
					return o1.compareTo(o2);
				}
			}
			else {
				return -res;
			}

		}

		private int count(Term o1) {
			Collection<Information> a = assumptionMap.get(o1);
			return 2 * countEtablished(a) + countSuggested(a) - 2
					* countExcluded(a);
		}
	};

	public class InferenceComparator implements Comparator<Information> {

		public int compare(Information o1, Information o2) {
			if (o1 == null) return -1;
			if (o2 == null) return +1;

			if (o1.getInformationType().equals(InformationType.XCLInferenceInformation)
					&& o2.getInformationType().equals(InformationType.XCLInferenceInformation)) {

				double confidence1 = (Double) o1.getValues().get(0);
				double confidence2 = (Double) o2.getValues().get(0);

				// Try to sort by confidence
				if (confidence1 > confidence2) return 1;
				else if (confidence1 < confidence2) return -1;
				else { // confidence1 == confidence2 -> we sort by support

					double support1 = (Double) o1.getValues().get(1);
					double support2 = (Double) o2.getValues().get(1);

					if (support1 > support2) return 1;
					else if (support1 < support2) return -1;

					return 0;
				}
			}

			if (!o1.getInformationType().equals(InformationType.XCLInferenceInformation)) return -1;

			if (!o2.getInformationType().equals(InformationType.XCLInferenceInformation)) return 1;

			return 0;
		}

	}

	private StringBuffer getSolutionLinkList(String user, String web, Broker b,
			List<Term> list, ISetMap<Term, Information> assumptionMap) {
		StringBuffer sb = new StringBuffer();
		sb.append("<ul>");
		for (Term term : list) {
			sb.append("<li>");
			String exactPrefix = KnowWEUtils.replaceUmlaut(((String) term
					.getInfo(TermInfoType.TERM_NAME)))
					+ "buttomLink";
			// sb.append("<a href=\"javascript:doNothing()\"
			// onclick=\"showPopupButtons('"+exactPrefix+"', event)\">");
			// sb.append("<img width=\"16\" height=\"16\" border=\"0\"
			// style=\"background-color: rgb(208, 208, 208);\" alt=\"\"
			// src=\""+iconURL+"windowIcon.gif"+"\"/> ");
			String topicName = findTopicNameForSolution((String) term
					.getInfo(TermInfoType.TERM_NAME), web);
			sb.append("<a href=\"Wiki.jsp?page=" + topicName + "\">"
					+ term.getInfo(TermInfoType.TERM_NAME) + "</a>");
			sb.append(" ");
			// sb.append(" </a>");
			for (Information info : assumptionMap.get(term)) {
				String string = info.getNamespace();
				String url = "encoding solution failed";
				url = "KnowWE.jsp?renderer=XCLExplanation&KWikiTerm="
								+ info.getObjectID()
								+ "&KWikisessionid=" + string + "&KWikiWeb=" + web
								+ "&KWikiUser=" + user;

				sb.append("<a href=\"#\" class=\"sstate-show-explanation\""
						+ " rel=\"{term : '" + info.getObjectID() + "', session : '" + string
						+ "', web : '" + web + "', user: '" + user + "'}\" >"
						+ getInferenceInfo(b, term, string) + "</a>");
			}

			// sb.append(getAssumptionsLink(user, web, term, assumptionMap));

			StringBuffer inner = new StringBuffer();
			inner.append(KnowWERenderUtils.getTopicLink(web,
					(String) term.getInfo(TermInfoType.TERM_NAME),
					iconURL, "dps", true, true));
			// inner.append(KnowWERenderUtils.getKopicLinks(web, term, iconURL,
			// "dps", true, true));
			// inner.append(KnowWERenderUtils.getExplanationLinks(user, web,
			// term, iconURL, "dps", true, true));
			// inner.append(KnowWERenderUtils.getClarificationLinks(user, web,
			// term, iconURL, "dps", true, true));
			// inner.append(KnowWERenderUtils.getSolutionLogLinks(user, web,
			// term, iconURL, true, true));
			// inner.append(KnowWERenderUtils.getDialogLinks(user, web, term,
			// iconURL, "dps", true, true));

			// sb.append(KnowWERenderUtils.getButtomLink(term, exactPrefix,
			// inner)); /* not used???*/
			sb.append("</li>");
		}
		sb.append("</ul>");
		return sb;
	}

	// private String findSolutionID(String solution, String topicName, String
	// web) {
	// D3webKnowledgeService b =
	// D3webModule.getInstance().getAD3webKnowledgeServiceInTopic(web,
	// topicName);
	// Diagnosis d = b.getBase().searchDiagnosis(solution);
	// List<Diagnosis> list = b.getBase().getDiagnoses();
	// for (Diagnosis diagnosis : list) {
	// if(diagnosis.getText().equals(solution)) {
	// return diagnosis.getId();
	// }
	// }
	//
	// return null;
	// }

	private String findTopicNameForSolution(String solution, String web) {
		Collection<KnowledgeService> services = D3webModule.getKnowledgeServices(web);
		for (KnowledgeService knowledgeService : services) {
			if (knowledgeService instanceof D3webKnowledgeService) {
				D3webKnowledgeService ks = ((D3webKnowledgeService) knowledgeService);
				KnowledgeBaseManagement mgn = KnowledgeBaseManagement
						.createInstance(ks.getBase());
				Solution d = mgn.findSolution(solution);
				if (d != null) {
					return knowledgeService.getId().substring(0,
							knowledgeService.getId().indexOf(".."));
				}

			}
		}
		return null;
	}

	private StringBuffer getInferenceInfo(Broker b, Term term, String namespace) {
		StringBuffer sb = new StringBuffer();
		Comparator<Information> comp = new InferenceComparator();
		Collection<Information> orginal = b.getSession().getBlackboard()
				.getInferenceInformation(term);

		if (term == null) {
			return sb;
			// return sb.append("term is null");
		}

		if (orginal == null) return sb;
		// return
		// sb.append("No Info found for Term:"+term.getInfo(TermInfoType.TERM_NAME));
		List<Information> infos = new ArrayList<Information>(orginal);

		Collections.sort(infos, comp);
		if (!infos.isEmpty()) {
			for (Information info : infos) {
				if (info.getNamespace().equals(namespace)) {
					if (info.getInformationType().equals(
							InformationType.SetCoveringInferenceInformation)) {
						double c1 = (Double) info.getValues().get(0);
						double c2 = (Double) info.getValues().get(1);
						c1 = (double) ((int) (c1 * 100)) / 100;
						c2 = (double) ((int) (c2 * 100)) / 100;
						sb.append("<span title='" + getScoreToolTip() + "'> ["
								+ c1 + ";" + c2 + "] </span>");
					}
					if (info.getInformationType().equals(
							InformationType.XCLInferenceInformation)
							&& info.getValues().size() > 0) {
						double c1 = (Double) info.getValues().get(0);
						double c2 = (Double) info.getValues().get(1);
						c1 = (double) ((int) (c1 * 100)) / 100;
						c2 = (double) ((int) (c2 * 100)) / 100;

						sb.append("<span title='" + getScoreToolTip()
								+ "'> XCL:[" + c1 + ";" + c2 + "] </span>");
					}
				}
			}
		}
		return sb;
	}

	private String getScoreToolTip() {
		return rb.getString("KnowWE.solution.degreeSC") + " / "
				+ rb.getString("KnowWE.sulution.recall");

	}

	// private StringBuffer getAssumptionsLink(String user, String web, Term
	// term,
	// ISetMap<Term, Information> assumptionMap) {
	//
	// StringBuffer sb = new StringBuffer();
	// Collection<Information> assumptions = assumptionMap.get(term);
	// int etas = countEtablished(assumptions);
	// int suggs = countSuggested(assumptions);
	// int excs = countExcluded(assumptions);
	//
	// // String user = (String) BasicUtils.getModelAttribute(model,
	// // KnowWEAttributes.USER, String.class, true);
	// // String web = (String) BasicUtils.getModelAttribute(model,
	// // KnowWEAttributes.WEB, String.class, true);
	// String link = "";
	// try {
	//
	// link = " rel=\"{term : '"
	// + URLEncoder.encode((String) term.getInfo(TermInfoType.TERM_NAME),
	// "ISO-8859-1")
	// + "', web : '" + web + "', user: '" + user + "'}\"";
	//
	// // link =
	// //
	// "javascript:kwiki_window('KnowWE.jsp?renderer=KWiki_solutionLog&KWikiUser="
	// // + user
	// // + "&KWikiWeb="
	// // + web
	// // + "&KWikiTerm="
	// // + URLEncoder.encode((String)
	// // term.getInfo(TermInfoType.TERM_NAME), "ISO-8859-1")
	// // + "')";
	// }
	// catch (UnsupportedEncodingException e) {
	//
	// }
	//
	// if (shouldDisplay(etas, suggs, excs)) {
	// sb.append("<div class=\"show-solutions-log pointer\" " + link + ">");
	// sb.append("(");
	// sb.append("<span title='" + rb.getString("KnowWE.solution.established")
	// + "' style='color:#007700'>");
	// sb.append(etas);
	// sb.append("</span>");
	// sb.append("|");
	// sb.append("<span title='" + rb.getString("KnowWE.solution.suggested")
	// + "' style='color:#FF6600'>");
	// sb.append(suggs);
	// sb.append("</span>");
	// sb.append("|");
	// sb.append("<span title='" + rb.getString("KnowWE.solution.excluded")
	// + "' style='color:#CC0000'>");
	// sb.append(excs);
	// sb.append("</span>");
	// sb.append(")");
	// sb.append("</div>");
	// }
	//
	// return sb;
	// }

	private boolean shouldDisplay(int etas, int suggs, int excs) {
		if ((etas + suggs + excs) <= 1) {
			return false;
		}
		return true;
	}

	private int countEtablished(Collection<Information> assumptions) {
		int result = 0;
		for (Information information : assumptions) {
			if (SolutionState.ESTABLISHED
					.equals(information.getValues().get(0))) {
				result++;
			}
		}
		return result;
	}

	private int countSuggested(Collection<Information> assumptions) {
		int result = 0;
		for (Information information : assumptions) {
			if (SolutionState.SUGGESTED.equals(information.getValues().get(0))) {
				result++;
			}
		}
		return result;
	}

	private int countExcluded(Collection<Information> assumptions) {
		int result = 0;
		for (Information information : assumptions) {
			if (SolutionState.EXCLUDED.equals(information.getValues().get(0))) {
				result++;
			}
		}
		return result;
	}

	private String renderSolutionStates(String web, String user, String topic, int index) {
		StringBuffer sb = new StringBuffer();
		Broker broker = D3webModule.getBroker(user, web);

		Map<Term, SolutionState> globalSolutions = broker.getSession()
				.getBlackboard().getGlobalSolutions();
		final ISetMap<Term, Information> assumptionMap = broker.getSession()
				.getBlackboard().getAssumptions();

		List<Term> established = new ArrayList<Term>();
		List<Term> suggested = new ArrayList<Term>();
		List<Term> excluded = new ArrayList<Term>();
		List<Term> conflict = new ArrayList<Term>();
		for (Entry<Term, SolutionState> entry : globalSolutions.entrySet()) {
			Term term = entry.getKey();

			boolean skip = true;
			if (index > 0) {
				for (Information info : assumptionMap.get(term)) {
					if (info.getNamespace().substring(0, info.getNamespace().indexOf(".."))
							.equals(
									index == 1
											? topic
											: SolutionPanelTagHandler.getArticleNames(web, user).get(
													index - 2))) {
						skip = false;
						continue;
					}
				}
				if (skip) {
					continue;
				}
			}

			SolutionState solutionState = entry.getValue();
			if (solutionState.equals(SolutionState.ESTABLISHED)) {
				established.add(term);
			}
			else if (solutionState
					.equals(SolutionState.SUGGESTED)) {
				suggested.add(term);
			}
			else if (solutionState.equals(SolutionState.EXCLUDED)) {
				excluded.add(term);
			}
			else if (solutionState.equals(SolutionState.CONFLICT)) {
				conflict.add(term);
			}
		}

		Comparator<Term> derivationComarator = new DerivationComparator(
				assumptionMap, broker);

		Collections.sort(established, derivationComarator);
		Collections.sort(suggested, derivationComarator);
		Collections.sort(excluded, derivationComarator);
		boolean painted = false;
		if (!established.isEmpty()) {
			sb.append("<div>");
			// sb.append("<a
			// href=\"/bin/view/"+web+"/Established\"><b>"+rb.getString("KnowWE.solution.establishedSolutions")+":</b>");
			sb.append("<strong>"
					+ rb.getString("KnowWE.solution.establishedSolutions")
					+ ":</strong>");
			// sb.append("</a>");
			sb.append(getSolutionLinkList(user, web, broker, established,
					assumptionMap));
			sb.append("</div>");
			painted = true;
		}

		if (!suggested.isEmpty()) {
			if (painted) {
				sb.append("<hr/>");
				painted = false;
			}
			sb.append("<div>");
			// sb.append("<a
			// href=\"/bin/view/"+web+"/Suggested\"><b>"+rb.getString("KnowWE.solution.suggestedSolutions")+":</b>");
			sb.append("<strong>"
					+ rb.getString("KnowWE.solution.suggestedSolutions")
					+ ":</strong>");
			// sb.append("</a>");
			sb.append(getSolutionLinkList(user, web, broker, suggested,
					assumptionMap));
			sb.append("</div>");
			painted = true;
		}
		/*
		 * if (!excluded.isEmpty()) { if(painted) { sb.append("<hr/>"); painted
		 * = false; } sb.append("<div>"); sb.append("<a
		 * href=\"/bin/view/"+web+"/Excluded\"><b>"
		 * +rb.getString("KnowWE.solution.excludedSolutions")+":</b>");
		 * sb.append("</a>"); sb.append(getSolutionLinkList(model, excluded,
		 * assumptionMap)); sb.append("</div>"); painted = true; }
		 *
		 * if (!conflict.isEmpty()) { if(painted) { sb.append("<hr/>"); painted
		 * = false; } sb.append("<div>"); sb.append("<a
		 * href=\"/bin/view/"+web+"/Conflict\"><b>"
		 * +rb.getString("KnowWE.solution.conflictSolutions")+":</b>");
		 * sb.append("</a>"); sb.append(getSolutionLinkList(model, conflict,
		 * assumptionMap)); sb.append("</div>"); painted = true; }
		 */
		return sb.toString();
	}
}
