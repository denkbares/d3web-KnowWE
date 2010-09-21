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

package de.d3web.we.kdom.questionTreeNew.dialog;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.CustomRenderer;
import de.d3web.we.kdom.rendering.RenderingMode;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * DashesPrefixRenderer Renders the Dashes of in the decision tree dialog view.
 * 
 * @author smark
 * @since 2010/03/09
 */
public class DashesPrefixRenderer extends CustomRenderer {

	@Override
	public boolean doesApply(String user, String topic, RenderingMode type) {
		return true;
	}

	@Override
	public void render(KnowWEArticle article, Section sec,
			KnowWEUserContext user, StringBuilder string) {
		// do nothing since the dashes should not be visible in the view
	}
}
