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
package de.d3web.we.kdom.bibtex;

import java.util.List;

import org.bibsonomy.model.BibTex;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class BibTexContentRenderer extends KnowWEDomRenderer {

	@Override
	public void render(Section sec, KnowWEUserContext user, StringBuilder string) {
		String parseerror=(String)KnowWEUtils.getStoredObject(sec, BibTexContent.PARSEEXCEPTION);		
		String ioerror=(String)KnowWEUtils.getStoredObject(sec, BibTexContent.IOEXCEPTION);
		List<BibTex> bibtexs=(List<BibTex>)KnowWEUtils.getStoredObject(sec, BibTexContent.BIBTEXs);
		
		if (parseerror!=null && parseerror.length()>0){
			string.append(KnowWEEnvironment.maskHTML("<p class\"=box error\">"));
			string.append(KnowWEEnvironment.maskHTML(parseerror.replaceAll("\n", "<br />")));
			string.append(KnowWEEnvironment.maskHTML("</p>"));
		} else if (ioerror!=null && ioerror.length()>0){
			string.append(KnowWEEnvironment.maskHTML("<p class\"=box error\">"));
			string.append(KnowWEEnvironment.maskHTML(ioerror.replaceAll("\n", "<br />")));
			string.append(KnowWEEnvironment.maskHTML("</p>"));
		} else {
			String header="<div id='knoffice-panel' class='panel'>";
			header+="<h3>" + "BibTex" + "</h3>";
			String footer="</div>";
			string.append(KnowWEEnvironment.maskHTML(header));
			for (BibTex cur:bibtexs){
				string.append(KnowWEEnvironment.maskHTML("<p>"));
				string.append(cur.toString());
				//TODO: use jabref to produce a nice htmloutput
				string.append(KnowWEEnvironment.maskHTML("</p>"));
			}
			
			string.append(KnowWEEnvironment.maskHTML(footer));
			
		}
		
		
	}

}
