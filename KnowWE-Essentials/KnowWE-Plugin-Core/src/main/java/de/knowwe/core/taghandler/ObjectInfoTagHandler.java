/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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
package de.knowwe.core.taghandler;

import java.util.Map;
import java.util.Set;

import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.objects.TermInfo;
import de.knowwe.core.kdom.objects.TermUtils;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.objectinfo.ObjectInfoRenderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.tools.ToolSet;
import de.knowwe.tools.ToolUtils;

/**
 * ObjectInfo TagHandler
 * <p/>
 * This TagHandler gathers information about a specified Object. The TagHanlder shows the article in which the object is
 * defined and all articles with references to this object.
 * <p/>
 * Additionally there is a possibility to rename this object in all articles and to create a wiki page for this object.
 *
 * @author Sebastian Furth
 * @created 01.12.2010
 */
public class ObjectInfoTagHandler extends AbstractTagHandler {

	// Parameter used in the request
	public static final String OBJECT_NAME = "objectname";
	public static final String TERM_IDENTIFIER = "termIdentifier";

	private static final DefaultMarkupRenderer defaultMarkupRenderer = new DefaultMarkupRenderer();

	public ObjectInfoTagHandler(String tag) {
		super(tag, true);
	}

	public ObjectInfoTagHandler() {
		this("ObjectInfo");
	}

	@Override
	public String getExampleString() {
		StringBuilder example = new StringBuilder();
		example.append("[{KnowWEPlugin objectInfo [");
		example.append(", objectname=\u00ABname of object\u00BB ");
		example.append(", termIdentifier=\u00ABexternal term identifier form of object\u00BB ");
		example.append("}])\n ");
		example.append("The parameters in [ ] are optional.");
		return example.toString();
	}

	@Override
	public final synchronized void render(Section<?> section, UserContext userContext,
										  Map<String, String> parameters, RenderResult result) {

		RenderResult content = new RenderResult(userContext);

		Identifier termIdentifier = ObjectInfoRenderer.getTermIdentifier(userContext);
		renderContent(termIdentifier, userContext, content);

		Section<TagHandlerTypeContent> tagNameSection = Sections.successor(
				section, TagHandlerTypeContent.class);
		ToolSet tools = ToolUtils.getTools(tagNameSection, userContext);

		RenderResult jspMasked = new RenderResult(result);
		defaultMarkupRenderer.renderDefaultMarkupStyled("ObjectInfo",
				content.toStringRaw(),
				section, tools, userContext, result);
		result.appendJSPWikiMarkup(jspMasked);
	}

	private void renderHR(RenderResult result) {
		result.appendHtml("<div style=\"margin-left:-4px; height:1px; width:102%; background-color:#DDDDDD;\"></div>");
	}

	private void renderContent(Identifier termIdentifier, UserContext user,
							   RenderResult result) {

		//ObjectInfoRenderer.renderLookUpForm(user, result);
		if (termIdentifier != null) {
			ObjectInfoRenderer.renderHeader(termIdentifier, user, result);
			//ObjectInfoRenderer.renderRenamingForm(termIdentifier, user, result);
			ObjectInfoRenderer.renderTermDefinitions(termIdentifier, user, result);
			ObjectInfoRenderer.renderTermReferences(termIdentifier, user, result);
			//renderHR(result);
			//ObjectInfoRenderer.renderPlainTextOccurrences(termIdentifier, user, result);
		}
	}

	protected void findTermSections(String web, Identifier termIdentifier, Set<Section<?>> definitions, Set<Section<?>> references) {
		TermInfo termInfo = TermUtils.getTermInfo(web, termIdentifier, false);
		if (termInfo == null) return;
		for (TermCompiler compiler : termInfo) {
			TerminologyManager termManager = compiler.getTerminologyManager();
			definitions.addAll(termManager.getTermDefiningSections(termIdentifier));
			references.addAll(termManager.getTermReferenceSections(termIdentifier));
		}
	}

	protected String getRenamingAction() {
		return "TermRenamingAction";
	}

}
