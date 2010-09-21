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

import java.util.ResourceBundle;

import de.d3web.we.basic.D3webModule;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.dialog.Dialog;
import de.d3web.we.core.dialog.DialogControl;
import de.d3web.we.core.knowledgeService.KnowledgeServiceSession;

public class UserInterventionRenderer extends DeprecatedAbstractKnowWEAction {

	private static ResourceBundle rb;

	@Override
	public String perform(KnowWEParameterMap map) {
		rb = D3webModule.getKwikiBundle_d3web(map.getRequest());
		StringBuffer sb = new StringBuffer();
		Broker broker = D3webModule.getBroker(map);
		DialogControl dc = broker.getDialogControl();
		Dialog nextActiveDialog = dc.getNextActiveDialog();
		String text = nextActiveDialog.getComment();
		KnowledgeServiceSession target = nextActiveDialog.getDialog();
		KnowledgeServiceSession reason = nextActiveDialog.getReason();

		String renderer = map.get(KnowWEAttributes.STEP_RENDERER);
		String action = KnowWEAttributes.ACTION_SWITCH_CASE;
		String noAction = "KWiki_noDelegate";

		sb.append("<div style='text-align:center'>");
		if (reason != null) {
			sb.append("<h3>");
			sb.append(getVerbalisation(reason));
			sb.append("</h3>");
			sb.append(rb.getString("KnowWE.intervention.delegate"));
			sb.append("<h3>");
			sb.append(getVerbalisation(target));
			sb.append("</h3>");
			sb.append("<br/>");
			sb.append("<div>");
			sb.append("<h2>");
			sb.append(text);
			sb.append("</h2>");
		}
		else {
			sb.append(rb.getString("KnowWE.intervention.wannaDelegate"));
		}
		sb.append("</div>");

		sb.append("<br/>");

		sb.append("<div>");
		sb.append("<a style='padding-left:2em;padding-right:2em;decoration:none;color:#000;border-width:0px;font-size:150%' href='KnowWE.jsp?action="
				+ action
				+ "&renderer="
				+ renderer
				+ "'alt='"
				+ rb.getString("KnowWE.intervention.no") + "'>");
		sb.append("<img width='2em' src='../images/tree/yes.png' alt='"
				+ rb.getString("KnowWE.intervention.yes") + "'>");
		sb.append("</img>");
		sb.append("</a>");
		sb.append("<a style='padding-left:2em;padding-right:2em;decoration:none;color:#000;border-width:0px;font-size:150%' href='KnowWE.jsp?action="
				+ noAction
				+ "&renderer="
				+ renderer
				+ "&"
				+ KnowWEAttributes.NAMESPACE
				+ "="
				+ target.getNamespace() + "'>");
		sb.append("<img width='2em' src='../images/tree/no.png' alt='"
				+ rb.getString("KnowWE.intervention.no") + "'>");
		sb.append("</img>");
		sb.append("</a>");
		sb.append("</div>");

		return sb.toString();
	}

	private String getVerbalisation(KnowledgeServiceSession kss) {
		String[] split = kss.getNamespace().split("\\.\\.");
		String result = "";
		if (split.length == 0) {
			result = rb.getString("KnowWE.intervention.kss");
			result = result.replaceAll("&service", split[0]);
		}
		else {
			result = rb.getString("KnowWE.intervention.kss");
			result += " ";
			result += rb.getString("KnowWE.intervention.topic");
			result = result.replaceAll("&topic", split[0]);
			result = result.replaceAll("&service", split[1]);
		}
		return result;
	}

}
