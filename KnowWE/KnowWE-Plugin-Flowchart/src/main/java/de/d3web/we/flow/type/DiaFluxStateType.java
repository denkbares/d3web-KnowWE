/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.we.flow.type;

import de.d3web.we.flow.FlowchartStateRender;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkup;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.knowwe.core.renderer.ReRenderSectionMarkerRenderer;

/**
 * 
 * @author Reinhard Hatko
 * @created 10.12.2010
 */
public class DiaFluxStateType extends DefaultMarkupType {

	public static final String ANNOTATION_MASTER = "master";
	public static final String ANNOTATION_SHOW_ALL = "showall";
	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("DiaFluxState");
		MARKUP.addAnnotation(ANNOTATION_MASTER, false);
		MARKUP.addAnnotation(ANNOTATION_SHOW_ALL, false, "true", "false");
	}

	public DiaFluxStateType() {
		super(MARKUP);
	}

	@Override
	public KnowWEDomRenderer<DiaFluxStateType> getRenderer() {
		return new ReRenderSectionMarkerRenderer<DiaFluxStateType>(new FlowchartStateRender());
	}

	public static String getMaster(Section<DiaFluxStateType> section) {
		return DefaultMarkupType.getAnnotation(section, ANNOTATION_MASTER);
	}

}
