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
package de.d3web.we.flow.type;

import de.d3web.we.core.packaging.KnowWEPackageManager;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkup;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;


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
		MARKUP.addAnnotation(KnowWEPackageManager.ATTRIBUTE_NAME, false);

	}

	/**
	 * @param markup
	 */
	public DiaFluxType() {
		super(MARKUP);
		setCustomRenderer(new DiaFluxRenderer());
	}

	public static String getFlowchartName(Section<DefaultMarkupType> diaFluxSection) {

		Section<FlowchartType> flowchart = diaFluxSection.findSuccessor(FlowchartType.class);

		if (flowchart == null) {
			return "";
		}
		else {
			return FlowchartType.getFlowchartName(flowchart);
		}

	}
}
