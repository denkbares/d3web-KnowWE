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
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.answers.AnswerUnknown;
import de.d3web.we.basic.IdentifiableInstance;
import de.d3web.we.basic.Information;
import de.d3web.we.basic.InformationNamespaceComparator;
import de.d3web.we.basic.InformationType;
import de.d3web.we.basic.TerminologyType;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.d3webModule.ProblemSolverType;
import de.d3web.we.terminology.local.LocalTerminologyAccess;
import de.d3web.we.terminology.term.Term;
import de.d3web.we.terminology.term.TermInfoType;

public class SolutionLogRenderer implements KnowWEAction {

	private String htmlHeader;
	private SimpleDateFormat dateFormat;
//	private String link = "KnowWE.jsp?renderer=KWiki_dialog&action=KWiki_requestDialog&KWikisessionid=%id%&KWikiUser=%user%&KWikiWeb=%web%";
	private String jumplink = "KnowWE.jsp?renderer=KWiki_dialog&action=KWiki_requestDialog&KWikiJumpId=%jumpId%&KWikisessionid=%id%&KWikiUser=%user%&KWikiWeb=%web%";
	private String iconURL;
	
	public SolutionLogRenderer() {
		htmlHeader = "<meta content='text/html; charset=iso-8859-1' http-equiv='content-type'><head><script src='../javascript/d3dialog.js'></script><script src='../javascript/dt.js'></script><link rel='stylesheet' type='text/css' href='../css/d3dialog.css' ></link><link rel='stylesheet' type='text/css' href='../css/dt.css' ><link rel='stylesheet' type='text/css' href='../css/KnowWE.css'></link></head>";
		dateFormat = new SimpleDateFormat();
		dateFormat.applyPattern("dd.MM.yyyy HH:mm:ss");
		ResourceBundle rb = ResourceBundle.getBundle("KnowWE_config");
		String path = "/KWiki/servlet/";
		iconURL = path + rb.getString("KWiki.config.path.images") + "tree/";
	}

	public String perform(KnowWEParameterMap map) {
		ResourceBundle rb = D3webModule.getKwikiBundle_d3web(map.getRequest());
		String userString = map.get(KnowWEAttributes.USER);
		String web = map.getWeb();
		String termString = map.get(KnowWEAttributes.TERM);
		
		if(termString == null) return "ERROR : no term specified";
		try {
			termString = URLDecoder.decode(termString, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return e.toString() + e.getStackTrace();
		}
		
		
		Broker broker = D3webModule.getBroker(map);
		DPSEnvironment dpse = D3webModule.getDPSE(map);
		Term term = dpse.getTerminologyServer().getBroker().getGlobalTerminology(TerminologyType.diagnosis).getTerm(termString, null);
		if(term == null) {
			return "<font color='red'> Problem with Character encoding (Umlauts?)</font>";
		}
		List<IdentifiableInstance> iis = dpse.getTerminologyServer().getBroker().getAlignedIdentifiableInstances(term);
		
		List<Information> allInfo = broker.getSession().getBlackboard()
				.getAllInformation();
		
		Collection<Information> neededInfo = new HashSet<Information>();
		
		for (Information info : allInfo) {
			if(info.getTerminologyType().equals(TerminologyType.diagnosis) 
					&& (info.getInformationType().equals(InformationType.HeuristicInferenceInformation) || info.getInformationType().equals(InformationType.SetCoveringInferenceInformation)|| info.getInformationType().equals(InformationType.CaseBasedInferenceInformation)
							|| info.getInformationType().equals(InformationType.SolutionInformation) || info.getInformationType().equals(InformationType.ClusterInformation))) {
				if(info.getInformationType().equals(InformationType.ClusterInformation) && term.getInfo(TermInfoType.TERM_NAME).equals(info.getObjectID())) {
					neededInfo.add(info);
				} else {
					for (IdentifiableInstance ii : iis) {
						if(ii.getNamespace().equals(info.getNamespace()) 
								&& ii.getObjectId().equals(info.getObjectID())) {
							neededInfo.add(info);
						}
					}
				}
			}
		}
		
		List<Information> shownInfos = new ArrayList<Information>(neededInfo);
		Collections.sort(shownInfos, new InformationNamespaceComparator());
		StringBuffer sb = new StringBuffer();
		
		sb.append("<html>");

		if (htmlHeader != null) {
			sb.append(htmlHeader);
		}
		int i = 0;
		sb.append("<BODY style='font-size:94%;font-family:arial, \"Lucida Grande\", verdana, sans-serif;color:#666;background-color:#F6FAFD;' onLoad='window.focus()'>");
		sb.append("<h2 style='text-align:left;color:#666;'>");
		sb.append(rb.getString("KnowWE.solution.log") + " " + term.getInfo(TermInfoType.TERM_NAME));
		sb.append("</h2>");
		sb.append("<div style='border-width:2px 0 0 0;border-style:solid;border-color:#E2DCC8;clear:both;'></div>");
		sb.append("<table cellspacing='0' border='1' rules='rows' style='font-size:94%;width:100%;border-color:#E2DCC8'>");
		sb.append("<tr>");
		sb.append("<th style='text-align:left;'>"+rb.getString("KnowWE.solution.namespace")+"</th>");
		sb.append("<th style='text-align:left;'>"+rb.getString("KnowWE.solution.value")+"</th>");
		sb.append("</tr>");
		for (Information information : shownInfos) {
			sb.append("<tr style='border-width:1px 0 0 0;border-style:solid;border-color:#E2DCC8;clear:both;background-color:#fff;valign:top'>");
			sb.append("<td style='text-align:left;valign:top;'>");
			if(i % 2 == 0) {
				sb.append("<div class='kwikiDefaultLink' style='background-color:#fff'>");
			} else {
				sb.append("<div class='kwikiDefaultLink' style='background-color:#F6FAFD'>");
			}
			sb.append("<a class='twikiLink' target='_parent' href='");
			sb.append(KnowWERenderUtils.getLinkToKopic(information.getIdentifiableObjectInstance(), web));
			sb.append("'>");
			sb.append(information.getNamespace());
			sb.append("</a>");
			sb.append("</div>");
			sb.append("</td>");
			sb.append("<td style='text-align:left;valign:top'>");
			if(i % 2 == 0) {
				sb.append("<div class='kwikiDefaultLink' style='background-color:#fff'>");
			} else {
				sb.append("<div class='kwikiDefaultLink' style='background-color:#F6FAFD'>");
			}
			if(information.getInformationType().equals(InformationType.ClusterInformation)) {
				sb.append(getValueText(information, map));
			} else {
				sb.append(getJumpLink(information.getNamespace(), information.getObjectID(), getValueText(information, map), userString, web));
				sb.append(" ");
				if(information.getInformationType().equals(InformationType.HeuristicInferenceInformation) 
						|| information.getInformationType().equals(InformationType.SetCoveringInferenceInformation)
						|| information.getInformationType().equals(InformationType.CaseBasedInferenceInformation)) {
					sb.append("<a href='" + KnowWERenderUtils.getLinkToExplanation(information.getIdentifiableObjectInstance(), userString, web, ProblemSolverType.getType(information.getInformationType())) + "' target='"+KnowWEAttributes.KNOWWE_DIALOG+"'>");
					sb.append("<img border='0' style='margin-left:1px;margin-right:1px' src=\"");
					sb.append(iconURL);
					sb.append("comments.png");
					sb.append("\" border=0");
					sb.append(" title=\"");
					sb.append(rb.getString("KnowWE.explanation.show"));
					sb.append("\" />");
					sb.append("</a>");
				}
			}
			sb.append("</div>");
			sb.append("</td>");
			sb.append("</tr>");
			
			i++;
		}
		sb.append("</table>");
		sb.append("</body>");
		sb.append("</html>");
		
		return sb.toString();
	}
	
	
	

	private String getJumpLink(String namespaceID, String objectId, String text, String userString, String web) {
		StringBuffer sb = new StringBuffer();
		String l = jumplink;
		l = l.replaceAll("%id%", namespaceID);
		l = l.replaceAll("%user%", userString);
		l = l.replaceAll("%web%", web);
		l = l.replaceAll("%jumpId%", objectId);
		sb.append("<a class='twikiLink' href='");
		sb.append(l);
		sb.append("' target='"+KnowWEAttributes.KNOWWE_DIALOG+"'>");
		sb.append(text);
		sb.append("</a>");
		return sb.toString();
	}
//	FL: commented out, because it was never used	
//	private String getObjectText(Information info, KnowWEParameterMap map) {
//		StringBuffer result = new StringBuffer();
//		DPSEnvironment env = KnowWEUtils.getDPSE(map);
//		
//		LocalTerminologyAccess terminology = env.getTerminologyServer().getStorage().getTerminology(info.getTerminologyType(), info.getNamespace());
//		
//		IdentifiableInstance iio = info.getIdentifiableObjectInstance();
//			
//		Object ido = terminology.getObject(iio.getObjectId(), null);
//		
//		if(ido instanceof NamedObject) {
//			result.append(((NamedObject)ido).getText());
//		}
//		return result.toString();
//	}
	
	private String getValueText(Information info, KnowWEParameterMap map) {
		ResourceBundle rb = D3webModule.getKwikiBundle_d3web(map.getRequest());
		StringBuffer result = new StringBuffer();
		DPSEnvironment env = D3webModule.getDPSE(map);
		
		if(info.getInformationType().equals(InformationType.SetCoveringInferenceInformation)) {
			StringBuffer sc = new StringBuffer();
			double c = (Double) info.getValues().get(0);
			double o = (Double) info.getValues().get(1);
			sc.append(c + "/" + o);
			if(o != 0) {
				//double covering = Math.round(c/o*10000);
				sc.append(" = " + c*100 + "%");
			}
			return sc.toString();
		} else if(info.getInformationType().equals(InformationType.CaseBasedInferenceInformation)) {
			StringBuffer sc = new StringBuffer();
			double c = (Double) info.getValues().get(0);
			double sim = Math.round(c*10000);
			sc.append(sim/100 + "%");
			return sc.toString();
		}
		if(info.getInformationType().equals(InformationType.SolutionInformation) || info.getInformationType().equals(InformationType.ClusterInformation)) {
			return rb.getString("KnowWE.solution."+info.getValues().get(0));
		}
		
		LocalTerminologyAccess terminology = env.getTerminologyServer().getStorage().getTerminology(info.getTerminologyType(), info.getNamespace());
		IdentifiableInstance iio = info.getIdentifiableObjectInstance();
		Collection<IdentifiableInstance> iivs = info.getIdentifiableValueInstances();
		if(iivs.isEmpty()) return result.toString();
		Iterator<IdentifiableInstance> iter = iivs.iterator();
		while (iter.hasNext()) {
			IdentifiableInstance iiv = iter.next();
			Object value = iiv.getValue();
			if(value instanceof String) {
				Object valueObject = terminology.getObject(iio.getObjectId(), (String) value);
				if(valueObject instanceof AnswerChoice) {
					result.append(((AnswerChoice)valueObject).getText());
				} else if(valueObject instanceof AnswerUnknown) {
					result.append("unkown");
				}
			} else {
				if(value.equals(Double.NEGATIVE_INFINITY)) {
					result.append("-999");
				} else {
					result.append(value.toString());
				}
			}	
			if(iter.hasNext()) {
				result.append(", ");
			}
		}
		return result.toString();
	}
	
}
