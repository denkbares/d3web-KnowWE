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

public class DefaultRenderer implements SparqlRenderer {

	public String render(TupleQueryResult result, Map<String, String> params) {

		ResourceBundle rb = KnowWEEnvironment.getInstance().getKwikiBundle();
		boolean empty = true;
		StringBuffer table = new StringBuffer();
		String output = "";
		boolean links = false;
		if (params.containsKey("render")) {
			links = params.get("render").equals("links");
		}
		boolean tablemode = false;

		table.append(KnowWEEnvironment.maskHTML("<ul>"));

		try {
			while (result.hasNext()) {
				BindingSet b = result.next();
				empty = false;
				Set<String> names = b.getBindingNames();
				if (!tablemode) {
					tablemode = names.size() > 1;
				}
				if (tablemode) {
					table.append(KnowWEEnvironment.maskHTML("<tr>"));
				}

				for (String cur : names) {
					String erg = b.getBinding(cur).toString();
					if (erg.split("#").length == 2)
						erg = erg.split("#")[1];
					if (links) {
						erg = "[" + KnowWEEnvironment.maskHTML(erg) + "]";
					}
					try {
						erg = URLDecoder.decode(erg, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (tablemode) {
						table.append(KnowWEEnvironment.maskHTML("<td>") + erg
								+ KnowWEEnvironment.maskHTML("</td>"));
					} else {
						table.append(KnowWEEnvironment.maskHTML("<li>") + erg
								+ KnowWEEnvironment.maskHTML("</li>\n"));
					}

				}

				if (tablemode) {
					table.append(KnowWEEnvironment.maskHTML("</tr>\n"));
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
			table.append(KnowWEEnvironment.maskHTML("</ul>"));
		}

		if (empty) {
			output += rb.getString("KnowWE.owl.query.no_result");
			return KnowWEEnvironment.maskHTML(output);
		} else {
			if (tablemode) {
				output += KnowWEEnvironment.maskHTML("<table>") + table
						+ KnowWEEnvironment.maskHTML("</table>");
			} else {
				output += table.toString();
			}
		}
		return output;
	}
}
