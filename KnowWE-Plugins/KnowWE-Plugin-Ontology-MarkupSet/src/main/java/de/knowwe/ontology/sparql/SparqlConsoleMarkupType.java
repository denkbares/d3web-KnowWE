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


import java.util.List;

import com.denkbares.strings.Strings;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.NothingRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.ContentType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.renderer.AsynchronousRenderer;

/**
 * @author Tobias Schmee (denkbares GmbH)
 * @created 07.10.19
 */
public class SparqlConsoleMarkupType extends DefaultMarkupType {

	private static final String RENDER_MODE = "renderMode";
	private static DefaultMarkup MARKUP;
	private static final String MARKUP_NAME = "SparqlConsole";

	static {
		MARKUP = new DefaultMarkup(MARKUP_NAME);
		MARKUP.addContentType(new SparqlConsoleContentType());
		MARKUP.addContentType(new SparqlContentType());
		MARKUP.addAnnotation(RENDER_MODE, false, "PlainText", "HTML", "ToolMenu");
		MARKUP.addAnnotationRenderer(RENDER_MODE, NothingRenderer.getInstance());
		MARKUP.addAnnotation(AsynchronousRenderer.ASYNCHRONOUS, false, "true", "false");
		MARKUP.addAnnotationRenderer(AsynchronousRenderer.ASYNCHRONOUS, NothingRenderer.getInstance());
		PackageManager.addPackageAnnotation(MARKUP);
	}

	public SparqlConsoleMarkupType() {
		super(MARKUP);
		this.setRenderer(new SparqlConsoleMarkupRenderer());
	}

	private static class SparqlConsoleMarkupRenderer extends Rdf2GoCoreCheckRenderer {
		@Override
		protected void renderContents(Section<? extends DefaultMarkupType> markupSection, List<Section<ContentType>> contentSections, UserContext user, RenderResult result) {
			String currentSparql = KnowWEUtils.getCookie("sparqlConsole_" + contentSections.get(0).getChildren().get(0).getID(), user);
			if (Strings.isBlank(currentSparql)) {
				currentSparql = "";
			}
			currentSparql = Strings.decodeURL(currentSparql);
			result.appendHtml("<textarea class=\"sparqlEditor\" placeholder=\"Enter Sparql here\" onkeydown=\"KNOWWE.plugin.sparqlConsole.keyUpTrigger(event,'" + contentSections.get(0).getChildren().get(0).getID() + "')\" onChange=\"KNOWWE.plugin.sparqlConsole.updateConsole('" + contentSections.get(0).getChildren().get(0).getID() + "')\">" + currentSparql +"</textarea>");
			// DelegateRenderer.getInstance().render(markupSection, user, result);
			super.renderContents(markupSection, contentSections, user, result);
		}
	}
}
