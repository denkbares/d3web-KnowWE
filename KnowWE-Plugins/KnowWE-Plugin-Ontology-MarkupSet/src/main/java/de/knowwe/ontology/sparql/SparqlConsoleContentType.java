/*
 * Copyright (C) 2019 denkbares GmbH, Germany
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

package de.knowwe.ontology.sparql;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderPlusEmpty;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.renderer.PaginationRenderer;
import de.knowwe.kdom.renderer.ReRenderSectionMarkerRenderer;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.sparql.utils.RenderOptions;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

/**
 * @author Tobias Schmee (denkbares GmbH)
 * @created 21.10.19
 */
public class SparqlConsoleContentType extends AbstractType implements SparqlType {

	public SparqlConsoleContentType() {
		this.setSectionFinder(AllTextFinderPlusEmpty.getInstance());
		this.setRenderer(new ReRenderSectionMarkerRenderer(new PaginationRenderer(new SparqlContentRenderer())));
	}

	@Override
	public String getSparqlQuery(Section<? extends SparqlType> section, UserContext context) {
		String cookie = KnowWEUtils.getCookie("sparqlConsole_" + section.getID(), context);
		Rdf2GoCore core = Rdf2GoCore.getInstance(section);
		if (Strings.isBlank(cookie) || core == null) {
			return "SELECT * WHERE { ?x ?y ?z } LIMIT 0"; // A query that always returns empty
		}
		else {
			cookie = Strings.decodeURL(cookie);
			return Rdf2GoUtils.createSparqlString(core, cookie);
		}
	}

	@Override
	public RenderOptions getRenderOptions(Section<? extends SparqlType> section, UserContext context) {
		RenderOptions renderOpts = new RenderOptions(section.getID());
		renderOpts.setRdf2GoCore(Rdf2GoCore.getInstance(section));
		renderOpts.setNavigation(true);
		return renderOpts;
	}
}
