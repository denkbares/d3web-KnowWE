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

package de.d3web.we.action;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import de.d3web.utilities.ISetMap;
import de.d3web.we.basic.Information;
import de.d3web.we.basic.SolutionState;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.terminology.term.Term;
import de.d3web.we.terminology.term.TermInfoType;
import de.d3web.we.utils.KnowWEUtils;

public class AllSolutionsRenderer implements KnowWEAction {

private String iconURL;
	
	private static ResourceBundle rb;

	public AllSolutionsRenderer(String newId) {
		ResourceBundle rb = ResourceBundle.getBundle("KnowWE_config");
		String path = "/KWiki/servlet/";
		iconURL = path + rb.getString("KWiki.config.path.images") + "tree/";
	}

	public String perform(KnowWEParameterMap parameterMap) {
		rb = D3webModule.getKwikiBundle_d3web(parameterMap.getRequest());
		StringBuffer sb = new StringBuffer();
		//String web = (String) BasicUtils.getModelAttribute(model, KnowWEAttributes.WEB, String.class, true);
		String web = parameterMap.get(KnowWEAttributes.WEB);
		
		Broker broker = D3webModule.getBroker(parameterMap);
		Map<Term, SolutionState> globalSolutions = broker.getSession()
				.getBlackboard().getGlobalSolutions();
		final ISetMap<Term, Information> assumptionMap = broker.getSession().getBlackboard().getAssumptions();
		
		List<Term> established = new ArrayList<Term>();
		List<Term> suggested = new ArrayList<Term>();
		List<Term> unclear = new ArrayList<Term>();
		List<Term> excluded = new ArrayList<Term>();
		List<Term> conflict = new ArrayList<Term>();
		for (Entry<Term,SolutionState> current : globalSolutions.entrySet()) {
			if (current.getValue().equals(SolutionState.ESTABLISHED)) {
				established.add(current.getKey());
			} else if (current.getValue().equals(SolutionState.SUGGESTED)) {
				suggested.add(current.getKey());
			} else if (current.getValue().equals(SolutionState.UNCLEAR)) {
				unclear.add(current.getKey());
			} else if (current.getValue().equals(SolutionState.EXCLUDED)) {
				excluded.add(current.getKey());
			} else if (current.getValue().equals(SolutionState.CONFLICT)) {
				conflict.add(current.getKey());
			} 
		}
		
		Comparator<Term> derivationComarator = new Comparator<Term>() {
			public int compare(Term o1, Term o2) {
				int i1 = count(o1);
				int i2 = count(o2);
				int comp = i2 - i1;
				if(comp > 0) {
					return 1;
				} else if(comp < 0) {
					return -1;
				} else {
					return o1.compareTo(o2);
				}
			}
			private int count(Term o1) {
				Collection<Information> a = assumptionMap.get(o1);
				return 2 * countEtablished(a) + countSuggested(a) - 2 * countExcluded(a);
			}
		};
		Collections.sort(established, derivationComarator);
		Collections.sort(unclear, derivationComarator);
		Collections.sort(suggested, derivationComarator);
		Collections.sort(excluded, derivationComarator);
		Collections.sort(conflict, derivationComarator);
		boolean painted = false;
		
		sb.append("<html>");
		sb.append("<head><link rel='stylesheet' type='text/css' href='../css/style.css'/>" +
						"<link rel='stylesheet' type='text/css' href='../css/colors.css'/>" +
						"<link rel='stylesheet' type='text/css' href='../css/layout.css'/>" +
						"<script type='text/javascript' src='../javascript/KnowledgeWiki.js'/></head>");
		
	
		sb.append("<BODY id='patternScreen' onLoad='window.focus()'>");
//		sb.append("<h2>");
//		sb.append("<div style='text-align:left;color:#666;'>");
//		sb.append(rb.getString("KnowWE.dialog.history"));
//		sb.append("</div>");
//		sb.append("</h2>");
		
		sb.append("<div id='patternPage'>");
		sb.append("<div id='patternRightBarContents'>");
		
		if (!established.isEmpty()) {
			sb.append("<div style='padding:1em'>");
			//sb.append("<a href=\"/bin/view/"+web+"/Established\"><b>"+rb.getString("KnowWE.solution.establishedSolutions")+":</b>");
			sb.append("<b>"+rb.getString("KnowWE.solution.establishedSolutions")+":</b>");
			//sb.append("</a>");
			sb.append(getSolutionLinkList(parameterMap, established, assumptionMap));
			sb.append("</div>");
			painted = true;
		}
		
		if (!suggested.isEmpty()) {
			if(painted) {
				sb.append("<hr/>");	
				painted = false;
			}
			sb.append("<div style='padding:1em'>");
			//sb.append("<a href=\"/bin/view/"+web+"/Suggested\"><b>"+rb.getString("KnowWE.solution.suggestedSolutions")+":</b>");
			sb.append("<b>"+rb.getString("KnowWE.solution.suggestedSolutions")+":</b>");
			
			//sb.append("</a>");
			sb.append(getSolutionLinkList(parameterMap, suggested, assumptionMap));
			sb.append("</div>");
			painted = true;
		}
		
		if (!unclear.isEmpty()) {
			if(painted) {
				sb.append("<hr/>");	
				painted = false;
			}
			sb.append("<div style='padding:1em'>");
			sb.append("<a href=\"/bin/view/"+web+"/Unclear\"><b>"+rb.getString("KnowWE.solution.unclearSolutions")+":</b>");
			sb.append("</a>");
			sb.append(getSolutionLinkList(parameterMap, unclear, assumptionMap));
			sb.append("</div>");
			painted = true;
		}
		
		if (!excluded.isEmpty()) {
			if(painted) {
				sb.append("<hr/>");	
				painted = false;
			}
			sb.append("<div style='padding:1em'>");
			sb.append("<a href=\"/bin/view/"+web+"/Excluded\"><b>"+rb.getString("KnowWE.solution.excludedSolutions")+":</b>");
			sb.append("</a>");
			sb.append(getSolutionLinkList(parameterMap, excluded, assumptionMap));
			sb.append("</div>");
			painted = true;
		}

		if (!conflict.isEmpty()) {
			if(painted) {
				sb.append("<hr/>");	
				painted = false;
			}
			sb.append("<div style='padding:1em'>");
			sb.append("<a href=\"/bin/view/"+web+"/Conflict\"><b>"+rb.getString("KnowWE.solution.conflictSolutions")+":</b>");
			sb.append("</a>");
			sb.append(getSolutionLinkList(parameterMap, conflict, assumptionMap));
			sb.append("</div>");
			painted = true;
		}
		sb.append("</div>");
		sb.append("</body>");
		sb.append("</html>");
		return sb.toString();
	}

	private StringBuffer getSolutionLinkList(Map<String,String> parameterMap, List<Term> list, ISetMap<Term, Information> assumptionMap) {
		StringBuffer sb = new StringBuffer();
		String user = parameterMap.get(KnowWEAttributes.USER);
		String web = parameterMap.get(KnowWEAttributes.WEB);
		
		sb.append("<ul>");
		for (Term term : list) {
			sb.append("<li>");
			sb.append("<div style='padding-left:1em'>");
			String exactPrefix = KnowWEUtils.replaceUmlaut(((String)term.getInfo(TermInfoType.TERM_NAME))) + "buttomLink";
			sb.append("<a href=\"javascript:doNothing()\" onclick=\"showPopupButtons('"+exactPrefix+"', event)\">");
			//sb.append("<img width=\"16\" height=\"16\" border=\"0\" style=\"background-color: rgb(208, 208, 208);\" alt=\"\" src=\""+iconURL+"windowIcon.gif"+"\"/> ");
			sb.append(term.getInfo(TermInfoType.TERM_NAME));
			sb.append(" ");
			sb.append(" </a>");
			sb.append(getAssumptionsLink(parameterMap, term, assumptionMap));
			
			StringBuffer inner = new StringBuffer();
			inner.append(KnowWERenderUtils.getTopicLink(web, term, iconURL, "dps", true, true));
			inner.append(KnowWERenderUtils.getKopicLinks( web, term, iconURL, "dps", true, true));
			inner.append(KnowWERenderUtils.getExplanationLinks(user, web, term, iconURL, "dps", true, true));
			inner.append(KnowWERenderUtils.getClarificationLinks(user, web, term, iconURL, "dps", true, true));
			inner.append(KnowWERenderUtils.getSolutionLogLinks(user, web, term, iconURL, true, true));
			inner.append(KnowWERenderUtils.getDialogLinks(user, web, term, iconURL, "dps", true, true));
			
			sb.append(KnowWERenderUtils.getButtomLink( term, exactPrefix, inner));
			sb.append("</div>");
			sb.append("</li>");
		}
		sb.append("</ul>");
		return sb;
	}

	private StringBuffer getAssumptionsLink(Map<String,String> parameterMap, Term term, ISetMap<Term, Information> assumptionMap) {
		StringBuffer sb = new StringBuffer();
		Collection<Information> assumptions = assumptionMap.get(term);
		int etas = countEtablished(assumptions);
		int suggs = countSuggested(assumptions);
		int excs = countExcluded(assumptions);
		String user = parameterMap.get(KnowWEAttributes.USER);
		String web = parameterMap.get(KnowWEAttributes.WEB);
		
		String link = "javascript:void(0)";
		try {
			link = "javascript:kwiki_window('KnowWE.jsp?renderer=KWiki_solutionLog&KWikiUser="+user+"&KWikiWeb="+web+"&KWikiTerm="+URLEncoder.encode((String) term.getInfo(TermInfoType.TERM_NAME), "ISO-8859-1")+"')";
		} catch (UnsupportedEncodingException e) {
			
		}

		if(shouldDisplay(etas, suggs, excs)) {
			sb.append("<a href=\"#\" onclick=\""+link+"\" >");
			sb.append("<span>");
			sb.append("(");
			sb.append("</span>");
			sb.append("<span title='"+rb.getString("KnowWE.solution.established")+"' style='color:#007700'>");
			sb.append(etas);
			sb.append("</span>");
			sb.append("<span>");
			sb.append("|");
			sb.append("</span>");
			sb.append("<span title='"+rb.getString("KnowWE.solution.suggested")+"' style='color:#FF6600'>");
			sb.append(suggs);
			sb.append("</span>");
			sb.append("<span>");
			sb.append("|");
			sb.append("</span>");
			sb.append("<span title='"+rb.getString("KnowWE.solution.excluded")+"' style='color:#CC0000'>");
			sb.append(excs);
			sb.append("</span>");
			sb.append("<span>");
			sb.append(")");
			sb.append("</span>");
			sb.append("</a>");
		}
		
		return sb;
	}

	private boolean shouldDisplay(int etas, int suggs, int excs) {
		if((etas + suggs + excs) <= 1) {
			return false;
		}
		return true;
	}

	private int countEtablished(Collection<Information> assumptions) {
		int result = 0;		
		for (Information information : assumptions) {
			if(SolutionState.ESTABLISHED.equals(information.getValues().get(0))) {
				result++;
			}
		}
		return result;
	}

	private int countSuggested(Collection<Information> assumptions) {
		int result = 0;		
		for (Information information : assumptions) {
			if(SolutionState.SUGGESTED.equals(information.getValues().get(0))) {
				result++;
			}
		}
		return result;
	}

	private int countExcluded(Collection<Information> assumptions) {
		int result = 0;		
		for (Information information : assumptions) {
			if(SolutionState.EXCLUDED.equals(information.getValues().get(0))) {
				result++;
			}
		}
		return result;
	}



}
