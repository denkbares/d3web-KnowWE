/*
 * Copyright (C) 2010 denkbares GmbH, Germany
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
package de.knowwe.core.tools;

import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.taghandler.ObjectInfoTagHandler;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.tools.ToolUtils;

/**
 * 
 * @author volker_belli
 * @created 01.12.2010
 */
public class ObjectInfoPageToolProvider implements ToolProvider {

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return hasObjectInfoPage(section);
	}

	@SuppressWarnings("unchecked")
	private boolean hasObjectInfoPage(Section<?> section) {
		return section.get() instanceof Term
				&& ((Term) section.get()).getTermIdentifier((Section<? extends Term>) section) != null;
	}

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		if (hasObjectInfoPage(section)) {
			@SuppressWarnings("unchecked")
			Section<? extends Term> s = (Section<? extends Term>) section;
			return new Tool[] {
					getObjectInfoPageTool(s, userContext), getUltimateEditTool(s, userContext) };
		}
		return ToolUtils.emptyToolArray();
	}

	protected Tool getObjectInfoPageTool(Section<? extends Term> section, UserContext userContext) {
		return new DefaultTool(
				"KnowWEExtension/d3web/icon/infoPage16.png",
				"Show Info Page",
				"Opens the information page for the specific object to show its usage inside this wiki.",
				createObjectInfoJSAction(section));
	}

	protected Tool getUltimateEditTool(Section<? extends Term> section, UserContext userContext) {
		return new DefaultTool(
				"http://localhost:8080/KnowWE/KnowWEExtension/images/pencil.png",
				"Open Ultimate Edit",
				"Opens the ultra edit mode.",
				createUltimateEditModeAction(section));
	}

	public static String createObjectInfoJSAction(Section<? extends Term> section) {
		Identifier termIdentifier = section.get().getTermIdentifier(section);
		return createObjectInfoPageJSAction(termIdentifier);
	}

	public static String createObjectInfoPageJSAction(Identifier termIdentifier) {
		String lastPathElementExternalForm = new Identifier(termIdentifier.getLastPathElement()).toExternalForm();
		String externalTermIdentifierForm = termIdentifier.toExternalForm();
		String jsAction = "window.location.href = "
				+ "'Wiki.jsp?page=ObjectInfoPage&amp;" + ObjectInfoTagHandler.TERM_IDENTIFIER
				+ "=' + encodeURIComponent('"
				+ maskTermForHTML(externalTermIdentifierForm)
				+ "') + '&amp;" + ObjectInfoTagHandler.OBJECT_NAME + "=' + encodeURIComponent('"
				+ maskTermForHTML(lastPathElementExternalForm) + "')";
		return jsAction;
	}

	public static String createUltimateEditModeAction(Section<? extends Term> section) {
		Identifier termIdentifier = section.get().getTermIdentifier(section);
		return createUltimateEditModeAction(termIdentifier);
	}

	public static String createUltimateEditModeAction(Identifier termIdentifier) {
		String externalTermIdentifierForm = termIdentifier.toExternalForm();
		String jsAction = "KNOWWE.plugin.ultimateEditTool.createDialogDiv('"
				+ externalTermIdentifierForm + "')";
		return jsAction;
	}

	private static String maskTermForHTML(String string) {
		string = string.replace("\\", "\\\\").replace("'", "\\'");
		string = Strings.encodeHtml(string);
		// in some strange wiki pages we got terms with linebreaks,
		// so handle them well
		string = string.replace("\n", "\\n").replace("\r", "\\r");
		return string;
	}

}
