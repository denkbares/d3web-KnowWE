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
package de.knowwe.diaflux.type;

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.diaflux.DiaFluxRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * 
 * @author Reinhard Hatko
 * @created 15.11.2010
 */
public class DiaFluxType extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	static {

		MARKUP = new DefaultMarkup("DiaFlux");
		MARKUP.addContentType(new FlowchartType());
		PackageManager.addPackageAnnotation(MARKUP);

	}

	public DiaFluxType() {
		super(MARKUP);
		setRenderer(new DiaFluxRenderer());
	}

	public static String getFlowchartName(Section<DiaFluxType> diaFluxSection) {

		Section<FlowchartType> flowchart = Sections.successor(diaFluxSection,
				FlowchartType.class);

		if (flowchart == null) {
			return "";
		}
		else {
			return FlowchartType.getFlowchartName(flowchart);
		}

	}
}
