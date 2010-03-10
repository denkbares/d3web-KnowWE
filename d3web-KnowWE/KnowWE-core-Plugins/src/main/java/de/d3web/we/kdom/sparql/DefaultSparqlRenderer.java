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

package de.d3web.we.kdom.sparql;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.utils.KnowWEUtils;

public class DefaultSparqlRenderer implements SparqlRenderer {

	private static DefaultSparqlRenderer instance;

	public static DefaultSparqlRenderer getInstance() {
		if (instance == null) {
			instance = new DefaultSparqlRenderer();
		}
		return instance;
	}

	private int myID;

	public String render(TupleQueryResult result, Map<String, String> params) {

		boolean links = false;
		if (params.containsKey("render")) {
			links = params.get("render").equals("links");
		}
		return renderResults(result, links);
	}

	public static String renderResults(TupleQueryResult result, boolean links) {
		ResourceBundle rb = KnowWEEnvironment.getInstance().getKwikiBundle();
		boolean empty = true;
		StringBuffer table = new StringBuffer();
		String output = "";
		boolean tablemode = false;

		table.append(KnowWEUtils.maskHTML("<ul>"));		
		try {
			while (result.hasNext()) {
				BindingSet b = result.next();
				empty = false;
				Set<String> names = b.getBindingNames();
				if (!tablemode) {
					tablemode = names.size() > 1;
				}
				if (tablemode) {
					table.append(KnowWEUtils.maskHTML("<tr>"));
				}

				for (String cur : names) {
					String erg = b.getBinding(cur).getValue().toString();
					if (erg.split("#").length == 2)
						erg = erg.split("#")[1];
					if (links) {
						try {
							if (KnowWEEnvironment.getInstance()
									.getWikiConnector().doesPageExist(erg)
									|| KnowWEEnvironment.getInstance()
											.getWikiConnector().doesPageExist(
													URLDecoder.decode(erg,
															"UTF-8"))) {
								erg = KnowWEUtils
										.maskHTML("<a href=\"Wiki.jsp?page="
												+ erg + "\">" + erg + "</a>");
							}
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					try {
						erg = URLDecoder.decode(erg, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (tablemode) {
						table.append(KnowWEUtils.maskHTML("<td>") + erg
								+ KnowWEUtils.maskHTML("</td>"));
					} else {
						table.append(KnowWEUtils.maskHTML("<li>") + erg
								+ KnowWEUtils.maskHTML("</li>\n"));
					}

				}

				if (tablemode) {
					table.append(KnowWEUtils.maskHTML("</tr>\n"));
				}
			}
		} catch (QueryEvaluationException e) {
			return rb.getString("KnowWE.owl.query.evalualtion.error") + ":"
					+ e.getMessage();
		} finally {
			try {
				result.close();
			} catch (QueryEvaluationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (!tablemode) {
			table.append(KnowWEUtils.maskHTML("</ul>"));
		}

		if (empty) {
			output += rb.getString("KnowWE.owl.query.no_result");
			return KnowWEUtils.maskHTML(output);
		} else {
			if (tablemode) {
				output += KnowWEUtils.maskHTML("<table>") + table
						+ KnowWEUtils.maskHTML("</table>");
			} else {
				output += table.toString();
			}
		}
		return output;
	}

	@Override
	public String getName() {
		
		return "default";
	}

	@Override
	public int getID() {
		
		return myID;
	}

	@Override
	public void setID(int id) {
	myID=id;
		
	}
}
