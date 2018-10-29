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

package de.knowwe.ontology.sparql;

import java.util.regex.Pattern;

import com.denkbares.strings.Strings;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.basicType.TimeStampType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.NothingRenderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.renderer.AsynchronousRenderer;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.util.Color;

public class SparqlMarkupType extends DefaultMarkupType {

	public static final String RAW_OUTPUT = "rawOutput";
	public static final String NAVIGATION = "navigation";
	public static final String ZEBRAMODE = "zebramode";
	public static final String TREE = "tree";
	public static final String SORTING = "sorting";
	public static final String BORDER = "border";
	public static final String NAME = "name";
	public static final String RENDER_QUERY = "showQuery";
	public static final String RENDER_MODE = "renderMode";
	public static final String TIMEOUT = "timeout";
	public static final String LOG_LEVEL = "logLevel";
	private static DefaultMarkup m = null;

	public static final String MARKUP_NAME = "Sparql";


	static {
		m = new DefaultMarkup(MARKUP_NAME);
		m.addContentType(new SparqlContentType());
		m.addAnnotation(RAW_OUTPUT, false, "true", "false");
		m.addAnnotation(NAVIGATION, false, "true", "false");
		m.addAnnotationRenderer(NAVIGATION, NothingRenderer.getInstance());
		m.addAnnotation(RENDER_QUERY, false, "true", "false");
		m.addAnnotationRenderer(RENDER_QUERY,  NothingRenderer.getInstance());
		m.addAnnotation(ZEBRAMODE, false, "true", "false");
		m.addAnnotationRenderer(ZEBRAMODE, NothingRenderer.getInstance());
		m.addAnnotation(LOG_LEVEL, false, Color.WARNING.name(),
				Color.ERROR.name());
		m.addAnnotationRenderer(LOG_LEVEL, NothingRenderer.getInstance());
		m.addAnnotation(TREE, false, "true", "false");
		m.addAnnotationRenderer(TREE, NothingRenderer.getInstance());
		m.addAnnotation(SORTING, false, "true", "false");
		m.addAnnotationRenderer(SORTING, NothingRenderer.getInstance());
		m.addAnnotation(BORDER, false, "true", "false");
		m.addAnnotationRenderer(BORDER, NothingRenderer.getInstance());
		m.addAnnotation(RENDER_MODE, false, "PlainText", "HTML", "ToolMenu");
		m.addAnnotationRenderer(RENDER_MODE, NothingRenderer.getInstance());
		m.addAnnotation(AsynchronousRenderer.ASYNCHRONOUS, false, "true", "false");
		m.addAnnotationRenderer(AsynchronousRenderer.ASYNCHRONOUS, NothingRenderer.getInstance());
		m.addAnnotation(TIMEOUT, false, Pattern.compile("\\d+(\\.\\d+)?|" + TimeStampType.DURATION));
		m.addAnnotationRenderer(TIMEOUT, NothingRenderer.getInstance());
		m.addAnnotation(Rdf2GoCore.GLOBAL, false, "true", "false");
		m.addAnnotationRenderer(Rdf2GoCore.GLOBAL, NothingRenderer.getInstance());
		m.addAnnotation(NAME, false);
		m.addAnnotationRenderer(NAME, NothingRenderer.getInstance());
		// TODO: replace class SparqlNameRegistrationScript by content type
		// m.addAnnotationContentType(NAME, new SparqlNameDefinition());
		PackageManager.addPackageAnnotation(m);
	}

	public SparqlMarkupType() {
		super(m);
		this.setRenderer(new SparqlMarkupRenderer());
	}

	private static class SparqlMarkupRenderer extends Rdf2GoCoreCheckRenderer {

		@Override
		protected String getTitleName(Section<?> section, UserContext user) {
			String title = super.getTitleName(section, user);
			String name = getAnnotation(section, NAME);
			if (!Strings.isBlank(name)) title += ": " + name;
			return title;
		}
	}
}
