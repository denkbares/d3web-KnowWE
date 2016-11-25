package de.knowwe.rdfs.vis.markup;

import java.io.IOException;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.rdfs.vis.OntoGraphDataBuilder;
import de.knowwe.rdfs.vis.PreRenderWorker;

/*
 * Copyright (C) 2012 denkbares GmbH
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

/**
 * Created by Dmitrij Kozlov on 25.11.16.
 */
public class OntoVisReRenderAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		// Execute clean up and clearCache methods in ConceptVisualizationRenderer
		Section<?> section = Sections.get(context.getParameter("SectionID"));
		OntoGraphDataBuilder builder = (OntoGraphDataBuilder) section.getObject(ConceptVisualizationRenderer.VISUALIZATION_RENDERER_KEY);
		if (builder != null) builder.getGraphRenderer().cleanUp();
		PreRenderWorker.getInstance().clearCache(section);
	}
}
