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

package cc.wiki.plugin;

import java.util.Map;

import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.plugin.PluginException;
import com.ecyrd.jspwiki.plugin.WikiPlugin;

public class InsertSectionPlugin implements WikiPlugin {

	private static int idCounter = 0; 
	
	@Override
	public String execute(WikiContext context, Map params) throws PluginException {
		String id = String.valueOf(idCounter++);
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("<div class='InsertSectionMenuItem'>");
		buffer.append("<a class='action' style='position:relative; top:0px; left:0px;' id='insertItem").append(id).append("' href='javascript:alert(&quot;Not implemented yet!&quot;);'>");
		appendMenuItem(context, params, buffer);
		buffer.append("</a>");
		buffer.append("</div>");
		
		buffer.append("<link rel='stylesheet' type='text/css' href='cc/plugin/insert-section-marker.css'/>");
		buffer.append("<script type='text/javascript' src='cc/plugin/mootools.drag.js'></script>");
		buffer.append("<script type='text/javascript' src='cc/plugin/insert-section-marker.js'></script>");
		buffer.append("<script>InsertSectionMarker.createDrag('insertItem"+id+"','insertItem"+id+"');</script>");
		return buffer.toString();
	}

	private void appendMenuItem(WikiContext context, Map params, StringBuffer buffer) throws PluginException {
		String type = (String) params.get("type");
		if (type == null) throw new PluginException("type not specified");
		type = type.toLowerCase();
		
		String menuText, iconFile;
		if (type.equals("flowchart")) {
			menuText = "Neues Flussdiagramm";
			iconFile = "flowchart.gif";
		}
		else if (type.equals("qset")) {
			menuText = "Neuer Fragebogen";
			iconFile = "qset.gif";
		}
		else if (type.equals("solution")) {
			menuText = "Neue L&ouml;sungen";
			iconFile = "diagnosis.gif";
		}
		else {
			throw new PluginException("unexpected type '"+type+"'");
		}
		
		buffer.append("<img src='cc/image/kbinfo/").append(iconFile).append("'></img>");
		buffer.append(menuText);
	}
}
