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
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import de.d3web.we.basic.IdentifiableInstance;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.d3webModule.DPSEnvironmentManager;
import de.d3web.we.d3webModule.ProblemSolverType;
import de.d3web.we.d3webModule.TerminologyAlignmentLinkFilter;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.terminology.term.Term;
import de.d3web.we.terminology.term.TermInfoType;
import de.d3web.we.utils.KnowWEUtils;

public class KnowWERenderUtils {
	
	public static final Comparator<IdentifiableInstance> iiNamespceComparator = new Comparator<IdentifiableInstance>() {

		public int compare(IdentifiableInstance o1, IdentifiableInstance o2) {
			return o1.getNamespace().compareTo(o2.getNamespace());
		}
		
	};
	
	
	public static StringBuffer getTopicLink(String web, Term term, String iconURL, String usagePrefix, boolean withTitle, boolean asButton) {
		ResourceBundle rb = D3webModule.getKwikiBundle_d3web();
		StringBuffer sb = new StringBuffer();
		//String web = (String) BasicUtils.getModelAttribute(model, KnowWEAttributes.WEB, String.class, true);
		String link = getLinkToTopic(term, web);
		if(asButton) {
			sb.append("<tr>");
			sb.append("<td>");
			sb.append("<div class='patternButton' style='clear:left; text-align:left; width:100%; overflow:visible;'>");		
		}
		sb.append("<a href='" + link + "'>");
		sb.append("<img border='0' style='margin-left:1px;margin-right:1px' src=\"");
		sb.append(iconURL);
		sb.append("table_go.png");
		sb.append("\" border=0");
		sb.append(" title=\"");
		sb.append(rb.getString("KnowWE.topic.show"));
		sb.append("\" />");
		if(withTitle) sb.append(rb.getString("KnowWE.topic"));
		sb.append("</a>");
		if(asButton) {
			sb.append("</div>");
			sb.append("</td>");
			sb.append("</tr>");
		}
		return sb;
	}

	

	public static String getLinkToTopic(Term term, String web) {
		String link = "Wiki.jsp?page=" + term.getInfo(TermInfoType.TERM_NAME);
		return link;
	}

	
	
	public static StringBuffer getKopicLinks(String web, Term term, String iconURL, String usagePrefix, boolean withTitle, boolean asButton) {
		ResourceBundle rb = D3webModule.getKwikiBundle_d3web();
		StringBuffer sb = new StringBuffer();
		DPSEnvironment dpse = DPSEnvironmentManager.getInstance().getEnvironments(web);
		//DPSEnvironment dpse = KnowWEUtils.getEnvironment(model);
		//String web = (String) BasicUtils.getModelAttribute(model, KnowWEAttributes.WEB, String.class, true);
		List<IdentifiableInstance> iis = dpse.getTerminologyServer().getBroker().getAlignedIdentifiableInstances(term, TerminologyAlignmentLinkFilter.getInstance());
		iis = getOnlyIIOs(iis);
		String exactPrefix = KnowWEUtils.replaceUmlaut(((String)term.getInfo(TermInfoType.TERM_NAME))) + usagePrefix;
		if(asButton) {
			sb.append("<tr>");
			sb.append("<td>");
			sb.append("<div class='patternButton' style='clear:left; text-align:left; width:100%; overflow:visible;'>");		
		}
		if(iis == null || iis.isEmpty()) {
			return sb;
		} else if(iis.size() == 1) {
			String link = getLinkToKopic(iis.iterator().next(), web);
			sb.append("<a href='" + link + "'>");
			sb.append("<img border='0' style='margin-left:1px;margin-right:1px' src=\"");
			sb.append(iconURL);
			sb.append("table_go.png");
			sb.append("\" border=0");
			sb.append(" title=\"");
			sb.append(rb.getString("KnowWE.topic.defining.show"));
			sb.append("\" />");
			if(withTitle) sb.append(rb.getString("KnowWE.topic.defining"));
			sb.append("</a>");
		} else {
			sb.append("<a href='javascript:doNothing()' onClick=\"showKopicLinks('"+exactPrefix+"', event)\">");
			sb.append("<img border='0' style='margin-left:1px;margin-right:1px' src=\"");
			sb.append(iconURL);
			sb.append("table_go.png");
			sb.append("\" border=0");
			sb.append(" title=\"");
			sb.append(rb.getString("KnowWE.topic.links.defining.show"));
			sb.append("\" />");
			if(withTitle) sb.append(rb.getString("KnowWE.topic.links.defining"));
			sb.append("</a>");
			sb.append("<div KWikiPopupLinksDiv=\"KWikiPopupLinksDiv\" id='"+exactPrefix+"kopicLinks' class='patternToolBar' onmouseover=\"replanToHide(this,event)\" onmouseout=\"planToHide(this,event)\" style='position:absolute;top:16px;right:0px;visibility:hidden;overflow:visible;'>");
			sb.append("<table>");
			Collections.sort(iis, iiNamespceComparator);
			//TODO refactor
			Set<String> firstParts = new HashSet<String>();
			for (IdentifiableInstance eachII : iis) {
				String firstPart = eachII.getNamespace().split("\\.\\.")[0];
				if(firstParts.contains(firstPart)) continue;
				sb.append("<tr>");
				sb.append("<td>");
				sb.append("<div class='patternButton' style='clear:left; text-align:left; width:100%; overflow:visible;'><a href='"+getLinkToKopic(eachII, web)+"' rel='nofollow' title='"+rb.getString("KnowWE.topic.defining.show")+"'>"+eachII.getNamespace().split("\\.\\.")[0]+"</a></div>");		
				sb.append("</td>");
				sb.append("</tr>");
				firstParts.add(firstPart);
			}
			sb.append("</table>");
			sb.append("</div>");
		}
		if(asButton) {
			sb.append("</div>");
			sb.append("</td>");
			sb.append("</tr>");
		}
		return sb;
	}

	

	public static String getLinkToKopic(IdentifiableInstance ii, String web) {
		String namespace = ii.getNamespace().split("\\.\\.")[0];
		String link = "Wiki.jsp?page=" + namespace;
		return link;
	}
	

	public static List<String> getLinksToKopics(List<IdentifiableInstance> iis, String web) {
		List<String> result = new ArrayList<String>();
		for (IdentifiableInstance ii : iis) {
			result.add(getLinkToKopic(ii, web));
		}
		return result;
	}


	public static StringBuffer getDialogLinks(String user, String web, Term term, String iconURL, String usagePrefix, boolean withTitle, boolean asButton) {
		ResourceBundle rb = D3webModule.getKwikiBundle_d3web();
		StringBuffer sb = new StringBuffer();
		//String user = (String) BasicUtils.getModelAttribute(model, KnowWEAttributes.USER, String.class, true);
		//String web = (String) BasicUtils.getModelAttribute(model, KnowWEAttributes.WEB, String.class, true);
		DPSEnvironment dpse = DPSEnvironmentManager.getInstance().getEnvironments(web);
		//DPSEnvironment dpse = KnowWEUtils.getEnvironment(model);
		List<IdentifiableInstance> iis = dpse.getTerminologyServer().getBroker().getAlignedIdentifiableInstances(term, TerminologyAlignmentLinkFilter.getInstance());
		iis = getOnlyIIOs(iis);
		String exactPrefix = KnowWEUtils.replaceUmlaut(((String)term.getInfo(TermInfoType.TERM_NAME))) + usagePrefix;
		if(asButton) {
			sb.append("<tr>");
			sb.append("<td>");
			sb.append("<div class='patternButton' style='clear:left; text-align:left; width:100%; overflow:visible;'>");		
		}
		if(iis == null || iis.isEmpty()) {
			return sb;
		} else if(iis.size() == 1) {
			String link = getLinkToDialog(iis.iterator().next(), user, web);
			sb.append("<a href='" + link + "' target='"+KnowWEAttributes.KNOWWE_DIALOG+"'>");
			sb.append("<img border='0' style='margin-left:1px;margin-right:1px' src=\"");
			sb.append(iconURL);
			sb.append("windowIcon.gif");
			sb.append("\" border=0");
			sb.append(" title=\"");
			sb.append(rb.getString("KnowWE.dialog.show"));
			sb.append("\" />");
			if(withTitle) sb.append(rb.getString("KnowWE.dialog"));
			sb.append("</a>");
		} else {
			sb.append("<a href='javascript:doNothing()' onClick=\"showDialogLinks('"+exactPrefix+"', event)\">");
			sb.append("<img border='0' style='margin-left:1px;margin-right:1px' src=\"");
			sb.append(iconURL);
			sb.append("windowIcon.gif");
			sb.append("\" border=0");
			sb.append(" title=\"");
			sb.append(rb.getString("KnowWE.dialog.links.show"));
			sb.append("\" />");
			if(withTitle) sb.append(rb.getString("KnowWE.dialog.links"));
			sb.append("</a>");
			sb.append("<div KWikiPopupLinksDiv=\"KWikiPopupLinksDiv\" id='"+exactPrefix+"dialogLinks' class='patternToolBar' onmouseover=\"replanToHide(this,event)\" onmouseout=\"planToHide(this,event)\" style='position:absolute;top:16px;right:0px;visibility:hidden;overflow:visible;'>");
			sb.append("<table>");
			for (IdentifiableInstance eachII : iis) {
				sb.append("<tr>");
				sb.append("<td>");
				sb.append("<div class='patternButton' style='clear:left; text-align:left; width:100%; overflow:visible;'><a href='"+getLinkToDialog(eachII, user, web)+"' target='"+KnowWEAttributes.KNOWWE_DIALOG+"' rel='nofollow' title='"+rb.getString("KnowWE.dialog.show")+"'>"+eachII.getNamespace()+"</a></div>");		
				sb.append("</td>");
				sb.append("</tr>");
			}
			sb.append("</table>");
			sb.append("</div>");
		}
		if(asButton) {
			sb.append("</div>");	
			sb.append("</td>");
			sb.append("</tr>");
		}
		return sb;
	}


	public static String getLinkToDialog(IdentifiableInstance ii, String user, String web) {
		String link = knowweUrlPrefix+"?renderer=KWiki_dialog&action=KWiki_requestDialog&KWikisessionid="+ii.getNamespace()+"&KWikiUser="+user+"&KWikiWeb="+web;
		return link;
	}
	
	public static List<String> getLinksToDialogs(List<IdentifiableInstance> iis, String user, String web) {
		List<String> result = new ArrayList<String>();
		for (IdentifiableInstance ii : iis) {
			result.add(getLinkToDialog(ii, user, web));
		}
		return result;
	}
	
	
	public static StringBuffer getSolutionLogLinks(String user, String web, Term term, String iconURL, boolean withTitle, boolean asButton) {
		ResourceBundle rb = D3webModule.getKwikiBundle_d3web();
		StringBuffer sb = new StringBuffer();
		//String user = (String) BasicUtils.getModelAttribute(model, KnowWEAttributes.USER, String.class, true);
		//String web = (String) BasicUtils.getModelAttribute(model, KnowWEAttributes.WEB, String.class, true);
		String link = "#";
		try {
			link = "javascript:kwiki_window('"+knowweUrlPrefix+"?renderer=KWiki_solutionLog&KWikiUser="+user+"&KWikiWeb="+web+"&KWikiTerm="+URLEncoder.encode((String) term.getInfo(TermInfoType.TERM_NAME), "ISO-8859-1")+"')";
		} catch (UnsupportedEncodingException e) {
			
		}
		if(asButton) {
			sb.append("<tr>");
			sb.append("<td>");
			sb.append("<div class='patternButton' style='clear:left; text-align:left; width:100%; overflow:visible;'>");		
		}
		sb.append("<a href=\"" + link + "\">");
		sb.append("<img border='0' style='margin-left:1px;margin-right:1px' src=\"");
		sb.append(iconURL);
		sb.append("application_view_list.png");
		sb.append("\" border=0");
		sb.append(" title=\"");
		sb.append(rb.getString("KnowWE.solution.log.show"));
		sb.append("\" />");
		if(withTitle) sb.append(rb.getString("KnowWE.solution.log"));
		sb.append("</a>");
		if(asButton) {
			sb.append("</div>");	
			sb.append("</td>");
			sb.append("</tr>");
		}
		return sb;
	}
	
	public static StringBuffer getExplanationLinks(String user, String web, Term term, String iconURL, String usagePrefix, boolean withTitle, boolean asButton) {
		ResourceBundle rb = D3webModule.getKwikiBundle_d3web();
		StringBuffer sb = new StringBuffer();
		//String user = (String) BasicUtils.getModelAttribute(model, KnowWEAttributes.USER, String.class, true);
		//String web = (String) BasicUtils.getModelAttribute(model, KnowWEAttributes.WEB, String.class, true);
		//DPSEnvironment dpse = KnowWEUtils.getEnvironment(model);
		DPSEnvironment dpse = DPSEnvironmentManager.getInstance().getEnvironments(web);
		List<IdentifiableInstance> iis = dpse.getTerminologyServer().getBroker().getAlignedIdentifiableInstances(term, TerminologyAlignmentLinkFilter.getInstance());
		iis = getOnlyIIOs(iis);
		String exactPrefix = KnowWEUtils.replaceUmlaut(((String)term.getInfo(TermInfoType.TERM_NAME))) + usagePrefix;
		if(asButton) {
			sb.append("<tr>");
			sb.append("<td>");
			sb.append("<div class='patternButton' style='clear:left; text-align:left; width:100%; overflow:visible;'>");		
		}
		if(iis == null || iis.isEmpty()) {
			return sb;
		} else if(iis.size() == 1) {
			List<String> links = getLinksToExplanations(iis, user, web);
			sb.append("<a href='" + links.get(0) + "' target='"+KnowWEAttributes.KNOWWE_DIALOG+"'>");
			sb.append("<img border='0' style='margin-left:1px;margin-right:1px' src=\"");
			sb.append(iconURL);
			sb.append("comments.png");
			sb.append("\" border=0");
			sb.append(" title=\"");
			sb.append(rb.getString("KnowWE.explanation.show"));
			sb.append("\" />");
			if(withTitle) sb.append(rb.getString("KnowWE.explanation"));
			sb.append("</a>");
		} else {
			sb.append("<a href='javascript:doNothing()' onClick=\"showExplanationLinks('"+exactPrefix+"', event)\">");
			sb.append("<img border='0' style='margin-left:1px;margin-right:1px' src=\"");
			sb.append(iconURL);
			sb.append("comments.png");
			sb.append("\" border=0");
			sb.append(" title=\"");
			sb.append(rb.getString("KnowWE.explanation.links.show"));
			sb.append("\" />");
			if(withTitle) sb.append(rb.getString("KnowWE.explanation.links"));
			sb.append("</a>");
			sb.append("<div KWikiPopupLinksDiv=\"KWikiPopupLinksDiv\" id='"+exactPrefix+"explanationLinks' class='patternToolBar' onmouseover=\"replanToHide(this,event)\" onmouseout=\"planToHide(this,event)\" style='position:absolute;top:16px;right:0px;visibility:hidden;overflow:visible;'>");
			sb.append("<table>");
			for (IdentifiableInstance eachII : iis) {
				sb.append("<tr>");
				sb.append("<td>");
				sb.append("<div class='patternButton' style='clear:left; text-align:left; width:100%; overflow:visible;'><a href='"+getLinkToExplanation(eachII, user, web)+"' target='"+KnowWEAttributes.KNOWWE_DIALOG+"' rel='nofollow' title='"+rb.getString("KnowWE.explanation.show")+"'>"+eachII.getNamespace()+"</a></div>");		
				sb.append("</td>");
				sb.append("</tr>");
			}
			sb.append("</table>");
			sb.append("</div>");
		}
		if(asButton) {
			sb.append("</div>");
			sb.append("</td>");
			sb.append("</tr>");
		}
		return sb;
	}


	public static List<String> getLinksToExplanations(List<IdentifiableInstance> iis, String user, String web) {
		List<String> result = new ArrayList<String>();
		for (IdentifiableInstance ii : iis) {
			result.add(getLinkToExplanation(ii, user, web));
		}
		return result;
	}
	
	public static String getLinkToExplanation(IdentifiableInstance ii, String user, String web, ProblemSolverType type) {
		String link = knowweUrlPrefix+"?renderer=KWiki_explain&action=KWiki_prepareDialog&KWikiNamespace="+ii.getNamespace()+"&KWikiExplain=" + ii.getObjectId() +"&KWikisessionid="+ii.getNamespace()+"&KWikiUser="+user+"&KWikiWeb="+web+"&ProblemSolverType="+type.getIdString();
		return link;
	}
	
	public static final String knowweUrlPrefix = "KnowWE.jsp";
	
	public static String getLinkToExplanation(IdentifiableInstance ii, String user, String web) {
		String link = knowweUrlPrefix+"?renderer=KWiki_explain&action=KWiki_prepareDialog&KWikiNamespace="+ii.getNamespace()+"&KWikiExplain=" + ii.getObjectId() +"&KWikisessionid="+ii.getNamespace()+"&KWikiUser="+user+"&KWikiWeb="+web;
		return link;
	}
	
	
	public static StringBuffer getClarificationLinks(String user, String web, Term term, String iconURL, String usagePrefix, boolean withTitle, boolean asButton) {
		ResourceBundle rb = D3webModule.getKwikiBundle_d3web();
		StringBuffer sb = new StringBuffer();
		//String user = (String) BasicUtils.getModelAttribute(model, KnowWEAttributes.USER, String.class, true);
		//String web = (String) BasicUtils.getModelAttribute(model, KnowWEAttributes.WEB, String.class, true);
		DPSEnvironment dpse = DPSEnvironmentManager.getInstance().getEnvironments(web);
		List<IdentifiableInstance> iis = dpse.getTerminologyServer().getBroker().getAlignedIdentifiableInstances(term, TerminologyAlignmentLinkFilter.getInstance());
		iis = getOnlyIIOs(iis);
		String exactPrefix = KnowWEUtils.replaceUmlaut(((String)term.getInfo(TermInfoType.TERM_NAME))) + usagePrefix;
		if(asButton) {
			sb.append("<tr>");
			sb.append("<td>");
			sb.append("<div class='patternButton' style='clear:left; text-align:left; width:100%; overflow:visible;'>");		
		}
		if(iis == null || iis.isEmpty()) {
			return sb;
		} else if(iis.size() == 1) {
			String link = getLinkToClarification(iis.iterator().next(), user, web);
			sb.append("<a href='" + link + "' target='"+KnowWEAttributes.KNOWWE_DIALOG+"'>");
			sb.append("<img border='0' style='margin-left:1px;margin-right:1px' src=\"");
			sb.append(iconURL);
			sb.append("comments.png");
			sb.append("\" border=0");
			sb.append(" title=\"");
			sb.append(rb.getString("KnowWE.clarification.show"));
			sb.append("\" />");
			if(withTitle) sb.append(rb.getString("KnowWE.clarification"));
			sb.append("</a>");
		} else {
			sb.append("<a href='javascript:doNothing()' onClick=\"showClarificationLinks('"+exactPrefix+"', event)\">");
			sb.append("<img border='0' style='margin-left:1px;margin-right:1px' src=\"");
			sb.append(iconURL);
			sb.append("comments.png");
			sb.append("\" border=0");
			sb.append(" title=\"");
			sb.append(rb.getString("KnowWE.clarification.links.show"));
			sb.append("\" />");
			if(withTitle) sb.append(rb.getString("KnowWE.clarification.links"));
			sb.append("</a>");
			sb.append("<div KWikiPopupLinksDiv=\"KWikiPopupLinksDiv\" id='"+exactPrefix+"clarificationLinks' class='patternToolBar' onmouseover=\"replanToHide(this,event)\" onmouseout=\"planToHide(this,event)\" style='position:absolute;top:16px;right:0px;visibility:hidden;overflow:visible;'>");
			sb.append("<table>");
			for (IdentifiableInstance eachII : iis) {
				sb.append("<tr>");
				sb.append("<td>");
				sb.append("<div class='patternButton' style='clear:left; text-align:left; width:100%; overflow:visible;'><a href='"+getLinkToClarification(eachII, user, web)+"' target='"+KnowWEAttributes.KNOWWE_DIALOG+"' rel='nofollow' title='"+rb.getString("KnowWE.clarification.show")+"'>"+eachII.getNamespace()+"</a></div>");		
				sb.append("</td>");
				sb.append("</tr>");
			}
			sb.append("</table>");
			sb.append("</div>");
		}
		if(asButton) {
			sb.append("</div>");
			sb.append("</td>");
			sb.append("</tr>");
		}
		return sb;
	}


	public static List<String> getLinksToClarifications(List<IdentifiableInstance> iis, String user, String web) {
		List<String> result = new ArrayList<String>();
		for (IdentifiableInstance ii : iis) {
			result.add(getLinkToClarification(ii, user, web));
		}
		return result;
	}
	
	public static String getLinkToClarification(IdentifiableInstance ii, String user, String web) {
		String link = knowweUrlPrefix+"?renderer=diagnosisClarification&action=KWiki_prepareDialog&KWikiNamespace="+ii.getNamespace()+"&diagId=" + ii.getObjectId() +"&KWikisessionid="+ii.getNamespace()+"&KWikiUser="+user+"&KWikiWeb="+web;
		return link;
	}
	
	private static List<IdentifiableInstance> getOnlyIIOs(Collection<IdentifiableInstance> iis) {
		Collection<IdentifiableInstance> result = new HashSet<IdentifiableInstance>();
		for (IdentifiableInstance each : iis) {
			if(!each.isValued()) {
				result.add(each);
			}
		}
		return new ArrayList<IdentifiableInstance>(result);
	}

	public static StringBuffer getButtomLink( Term term, String usagePrefix, StringBuffer inner) {
		StringBuffer sb = new StringBuffer();
		//String exactPrefix = KWikiUtils.replaceUmlaut(((String)term.getInfo(TermInfoType.TERM_NAME))) + usagePrefix;
		sb.append("<div KWikiPopupLinksDiv=\"KWikiPopupLinksDiv\" id='"+usagePrefix+"Popup' class='patternToolBar' onmouseover=\"replanToHide(this,event)\" onmouseout=\"planToHide(this,event)\" style='position:absolute;top:16px;right:0px;visibility:hidden;overflow:visible;'>");
		sb.append("<table>");
		sb.append(inner);
		sb.append("</table>");
		sb.append("</div>");
		return sb;
	}
	
	
}
