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

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import de.d3web.kernel.domainModel.NamedObject;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.answers.AnswerUnknown;
import de.d3web.we.basic.IdentifiableInstance;
import de.d3web.we.basic.Information;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.terminology.local.LocalTerminologyAccess;

public class UserFindingsRenderer implements KnowWEAction {

	private String htmlHeader;
	private SimpleDateFormat dateFormat;
//	private String link = "/KnowWE.jsp?renderer=KWiki_dialog&action=KWiki_requestDialog&KWikisessionid=%id%&KWikiUser=%user%&KWikiWeb=%web%";
	private String jumplink = "KnowWE.jsp?renderer=KWiki_dialog&action=KWiki_requestDialog&KWikiJumpId=%jumpId%&KWikisessionid=%id%&KWikiUser=%user%&KWikiWeb=%web%";
	
	
	public UserFindingsRenderer() {
		htmlHeader = "<head>"
			+ "<link rel=\"stylesheet\" type=\"text/css\" href=\"KnowWEExtension/css/general.css\"/>"
			+ "<link href=\"templates/knowweTmps/jspwiki.css\" type=\"text/css\" media=\"screen, projection, print\" rel=\"stylesheet\">"
//			+ "<script src='../javascript/d3dialog.js'></script>"
//			+ "<script src='../javascript/dt.js'></script><link rel='stylesheet' type='text/css' href='../css/d3dialog.css' >"
//			+ "</link><link rel='stylesheet' type='text/css' href='../css/dt.css' >"
//			+ "<link rel='stylesheet' type='text/css' href='../css/KnowWE.css'></link>"
			+ "</head>";
		dateFormat = new SimpleDateFormat();
		dateFormat.applyPattern("dd.MM.yyyy HH:mm:ss");
	}

	public String perform(KnowWEParameterMap map) {
		ResourceBundle rb = D3webModule.getKwikiBundle_d3web(map.getRequest());
		String userString = map.getUser();
		String web = map.getWeb();
		Broker broker = D3webModule.getBroker(map);

		List<Information> userInfos = broker.getSession().getBlackboard().getOriginalUserInformation();
		Collections.reverse(userInfos);
		StringBuffer sb = new StringBuffer();
		
		sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		sb.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/transitional.dtd\">");
		sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"de\">");

		if (htmlHeader != null) {
			sb.append(htmlHeader);
		}
		boolean even = false;
		sb.append("<body onLoad='window.focus()'>");
		sb.append("<div id=\"popup-findings\"");
		sb.append("<h3>" + rb.getString("KnowWE.findings") + "</h3>");
		sb.append("<table rules='rows'>");
		sb.append("<thead><tr>");
		sb.append("<th>"+rb.getString("KnowWE.finding.question")+"</th>");
		sb.append("<th></th>");
		sb.append("<th>"+rb.getString("KnowWE.finding.value")+"</th>");
		sb.append("<th>"+rb.getString("KnowWE.dialog.namespace")+"</th>");
		sb.append("</tr></thead><tbody>");
		for (Information information : userInfos) {	
			if(even){
				sb.append("<tr class=\"even\">");
			} else {
				sb.append("<tr>");
			}
			
			String[] content = {getObjectText(information, map), "=", getValueText(information, map),
					getJumpLink(information.getNamespace(), information.getObjectID(), information.getNamespace(), userString,web)};
			
			for (String string : content) {
				sb.append("<td>" + string + "</td>");
			}
			
			even = !even;
			sb.append("</tr>");
		}
		sb.append("</tbody></table></div></body></html>");
		return sb.toString();
	}
	
//	FL: commented out, because it was never used	
//	private String getLink(String namespaceID, String userString, String web) {
//		StringBuffer sb = new StringBuffer();
//		String l = link;
//		l = l.replaceAll("%id%", namespaceID);
//		l = l.replaceAll("%user%", userString);
//		l = l.replaceAll("%web%", web);
//		sb.append("<a href='");
//		sb.append(l);
//		sb.append("' target='"+KnowWEAttributes.KNOWWE_DIALOG+"'>");
//		sb.append(namespaceID);
//		sb.append("</a>");
//		return sb.toString();
//	}

	private String getJumpLink(String namespaceID, String objectId, String text, String userString, String web) {
		StringBuffer sb = new StringBuffer();
		String l = jumplink;
		l = l.replaceAll("%id%", namespaceID);
		l = l.replaceAll("%user%", userString);
		l = l.replaceAll("%web%", web);
		l = l.replaceAll("%jumpId%", objectId);
		sb.append("<a href='");
		sb.append(l);
		
		sb.append("' target='"+KnowWEAttributes.KNOWWE_DIALOG+"'>");
		sb.append(text);
		sb.append("</a>");
		return sb.toString();
	}
	
	private String getObjectText(Information info, KnowWEParameterMap map) {
		StringBuffer result = new StringBuffer();
		DPSEnvironment env = D3webModule.getDPSE(map);
		
		LocalTerminologyAccess terminology = env.getTerminologyServer().getStorage().getTerminology(info.getTerminologyType(), info.getNamespace());
		
		IdentifiableInstance iio = info.getIdentifiableObjectInstance();
			
		Object ido = terminology.getObject(iio.getObjectId(), null);
		
		if(ido instanceof NamedObject) {
			result.append(((NamedObject)ido).getText());
		}
		return result.toString();
	}
	
	private String getValueText(Information info, KnowWEParameterMap parameterMap) {
		StringBuffer result = new StringBuffer();
		DPSEnvironment env = D3webModule.getDPSE(parameterMap);
		
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
				result.append(value.toString());
			}	
			if(iter.hasNext()) {
				result.append(", ");
			}
		}
		return result.toString();
	}
	
}
