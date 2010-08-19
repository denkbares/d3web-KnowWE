/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

package de.d3web.we.hermes.kdom.conceptMining;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

import de.d3web.we.hermes.util.TimeEventSPARQLUtils;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public abstract class ConceptFinder extends SectionFinder {

	private static final String INSTANCE_SPARQL = "SELECT ?x WHERE { ?x rdf:type lns:CLASS .}";

	protected abstract String[] getClassNames();

	private Set<String> objectNames = null;

	@Override
	public List<SectionFinderResult> lookForSections(String arg0, Section arg1, KnowWEObjectType type) {

		String text = arg0;

		// if(objectNames == null) {
		fillObjectNameList();
		// }

		List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();

		for (String objectName : objectNames) {
			int index = text.indexOf(objectName);

			if (index == -1) continue;

			result.add(new SectionFinderResult(index, index + objectName.length()));
		}

		return result;

	}

	private void fillObjectNameList() {
		objectNames = new HashSet<String>();

		String[] classes = this.getClassNames();
		for (String clazz : classes) {
			String query = INSTANCE_SPARQL.replace("CLASS", clazz);

			TupleQueryResult result = TimeEventSPARQLUtils.executeQuery(query);

			try {
				while (result.hasNext()) {
					BindingSet set = result.next();

					Binding tB = set.getBinding("x");
					String name = tB.getValue().stringValue();

					try {
						name = URLDecoder.decode(name, "UTF-8");
					}
					catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					objectNames.add(name.substring(name.lastIndexOf("#") + 1));

				}
			}
			catch (QueryEvaluationException e) {
				// moo
				e.printStackTrace();
			}
			finally {
				try {
					result.close();
				}
				catch (QueryEvaluationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

	}

}
