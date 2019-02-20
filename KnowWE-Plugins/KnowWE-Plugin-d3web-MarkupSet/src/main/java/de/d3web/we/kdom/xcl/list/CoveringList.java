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

package de.d3web.we.kdom.xcl.list;

import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.basicType.CommentLineType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinderUnquoted;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.AnonymousType;
import de.knowwe.kdom.renderer.ReRenderSectionMarkerRenderer;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.sectionFinder.StringSectionFinderUnquoted;

/**
 * A covering-list markup parser
 * <p>
 * In the first line the solution is defined @see ListSolutionType The rest of the content is split by ',' (commas) and
 * the content in between is taken as CoveringRelations
 *
 * @author Jochen
 */
public class CoveringList extends AbstractType {

	public CoveringList() {
		this.setSectionFinder(AllTextFinder.getInstance());
		this.setRenderer(new ReRenderSectionMarkerRenderer(this::renderWithId));

		this.addChildType(new XCLHeader());

		// cut the optional closing }
		this.addChildType(new AnonymousType("opening", new StringSectionFinderUnquoted("{"), StyleRenderer.COMMENT));
		this.addChildType(new AnonymousType("closing", new StringSectionFinderUnquoted("}"), StyleRenderer.COMMENT));

		// allow for comment lines
		this.addChildType(new CommentLineType());

		// split by search for commas
		Pattern splitPattern = Pattern.compile("([\\h\\s\\v]+[,;][\\h\\s\\v]*)|([\\h\\s\\v]*[,;][\\h\\s\\v]+)");
		this.addChildType(new AnonymousType("comma", new RegexSectionFinderUnquoted(splitPattern), StyleRenderer.COMMENT));

		// the rest is CoveringRelations
		this.addChildType(new XCLRelation());

		// anything left is comment
		AnonymousType residue = new AnonymousType("remaining");
		residue.setSectionFinder(new AllTextFinderTrimmed());
		residue.setRenderer(StyleRenderer.COMMENT);
		this.addChildType(residue);
	}

	private void renderWithId(Section<?> section, UserContext user, RenderResult result) {
		KnowWEUtils.renderAnchor(section, result);
		result.appendHtml("<span id='" + section.getID() + "'>");
		DelegateRenderer.getInstance().render(section, user, result);
		result.appendHtml("</span>");
	}
}
