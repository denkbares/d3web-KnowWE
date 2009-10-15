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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ResourceBundle;

import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.utils.KnowWEUtils;

public class SessionChooserRenderer implements KnowWEAction {

	public FilenameFilter filter;
	
	public SessionChooserRenderer(String newId) {
		filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
		};
	}

	public String perform(KnowWEParameterMap map) {
		ResourceBundle rb = KnowWEEnvironment.getInstance().getKwikiBundle(map.getRequest());
		StringBuffer sb = new StringBuffer();
		String sessionPath = KnowWEUtils.getSessionPath(map);
		File sessionPathFile = new File(sessionPath);
		String linkAction = map.get(KnowWEAttributes.LINK_ACTION);
		String user = map.get(KnowWEAttributes.USER);
		String web = map.get(KnowWEAttributes.WEB);
		sb.append("<table>");
		File[] files = sessionPathFile.listFiles(filter);
		if(files == null || files.length == 0) {
			sb.append("<tr>");
			sb.append("<div class='patternButton' style='clear:left; text-align:right; width:100%;'><a href='#' rel='nofollow' title='"+rb.getString("KnowWE.session.noSessionsAvailable")+"'>"+rb.getString("KnowWE.session.noSessionsAvailable")+"</a></div>");		
			sb.append("</tr>");
		} else {
			for (File file : sessionPathFile.listFiles(filter)) {
				//String link = "kwiki_call(\"/KnowWE.jsp?action="+linkAction+"&KWikiUser="+user+"&KWikiWeb="+web+"&"+KnowWEAttributes.SESSION_FILE+"="+file.getName()+"\");hideSessionChooser();";
				sb.append("<tr>");
				sb.append("<div class='patternButton' style='clear:left; text-align:right; width:100%;'><a href='#' rel='nofollow' title='Choose session'>"+file.getName()+"</a></div>");		
				sb.append("</tr>");
			}
		}
		sb.append("</table>");
		sb.append("</div>");
		
		return sb.toString();
	}

}
