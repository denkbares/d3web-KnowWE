/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.we.wisec.taghandler;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Set;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.semantic.ISemanticCore;
import de.d3web.we.core.semantic.SPARQLUtil;
import de.d3web.we.core.semantic.SemanticCoreDelegator;
import de.d3web.we.taghandler.AbstractHTMLTagHandler;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * 
 * @author Jochen
 * @created 19.08.2010
 */
public class ShowSubstancesHandler extends AbstractHTMLTagHandler {

	private static final String QUERY = "SELECT ?substance WHERE { ?substance rdf:type w:Substance } ORDER BY ?substance";

	public ShowSubstancesHandler() {
		super("showsubstancelist");
	}

	@Override
	public String renderHTML(String topic, KnowWEUserContext user, Map<String, String> values, String web) {
		ISemanticCore sc = SemanticCoreDelegator.getInstance();

		String querystring = QUERY;

		TupleQueryResult queryResult = SPARQLUtil.executeTupleQuery(querystring);
		
		StringBuffer listStringBuffer = new StringBuffer();
		listStringBuffer.append(KnowWEUtils.maskHTML("<ul>"));
		boolean empty = true;
		
		try {
			while (queryResult.hasNext()) {
				BindingSet b = queryResult.next();
				empty = false;
				Set<String> names = b.getBindingNames();
			

				for (String cur : names) {
					String erg = b.getBinding(cur).getValue().toString();
					if (erg.split("#").length == 2) erg = erg.split("#")[1];
					String pagename = "WI_SUB_"+URLDecoder.decode(erg,
					"UTF-8");
							
//							if (KnowWEEnvironment.getInstance()
//									.getWikiConnector().doesPageExist(pagename)
//									|| KnowWEEnvironment.getInstance()
//											.getWikiConnector().doesPageExist(
//													URLDecoder.decode(erg,
//															"UTF-8"))) {
								erg = KnowWEUtils
										.maskHTML("<a href=\"Wiki.jsp?page="
												+ pagename + "\">" + erg + "</a>");
//						}
						

						erg = URLDecoder.decode(erg, "UTF-8");
					
						listStringBuffer.append(KnowWEUtils.maskHTML("<li>") + erg
								+ KnowWEUtils.maskHTML("</li>\n"));

				}

			}
		}
		catch (QueryEvaluationException e) {
			return KnowWEEnvironment.getInstance().getKwikiBundle(user).getString(
					"KnowWE.owl.query.evalualtion.error")
					+ ":"
					+ e.getMessage();
		}
		catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {

		}
		listStringBuffer.append(KnowWEUtils.maskHTML("</ul>"));
		if (empty) {
			listStringBuffer.append("no results");
		}

		return listStringBuffer.toString();
	}
}
