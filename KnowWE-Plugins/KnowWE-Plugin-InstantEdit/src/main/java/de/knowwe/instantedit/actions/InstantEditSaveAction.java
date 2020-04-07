/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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
package de.knowwe.instantedit.actions;

import com.google.gson.Gson;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Sections;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Saves the changed Section.
 *
 * @author Stefan Mark
 * @author Albrecht Striffler (denkbares GmbH)
 * @author Nikolai Reed (Olyro GmbH)
 * @created 15.06.2011
 */
public class InstantEditSaveAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String id = context.getParameter("KdomNodeId");
		String jsonData = context.getParameter("data");

		Gson g = new Gson();
		SaveActionDTO data = g.fromJson(jsonData, SaveActionDTO.class);
		String newArticle = buildArticle(data);

		Map<String, String> nodesMap = new HashMap<>();
		nodesMap.put(id, newArticle);
		Sections.replace(context, nodesMap).sendErrors(context);
		Compilers.awaitTermination(Compilers.getCompilerManager(context.getWeb()));
	}

	/**
	 * Parses the SaveActionDTO to String as new Article to replace old one
	 * Currently will only parse Type WIKI (wiki text as string) only
	 * @param actionData SaveActionDTO
	 * @return String
	 * @exception IllegalArgumentException for DTO object if it contains JSON type that cannot be parsed
	 */
	private String buildArticle(SaveActionDTO actionData){
		//filter out empty Sections and join the rest
		return actionData.getSections().stream().filter(s->!s.isEmpty()).map(section->{
			if (section.getType().equals(SaveActionSectionDTO.Type.WIKI)){
				return section.getWikiData();
			} else {
				throw new IllegalArgumentException("Received data type JSON but expected WIKI for Section " + section.getSectionID());
			}
		}).collect(Collectors.joining("\r\n\r\n"));
	}

}
