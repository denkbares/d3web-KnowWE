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

import de.d3web.diaFlux.flow.Flow;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.kdom.objects.SimpleReference;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * 
 * @author Reinhard Hatko
 * @created 08.12.2010
 */
public class FlowchartReference extends SimpleReference {

	public FlowchartReference() {
		super(D3webCompiler.class, Flow.class);
		setRenderer(StyleRenderer.FLOWCHART);
	}

}
