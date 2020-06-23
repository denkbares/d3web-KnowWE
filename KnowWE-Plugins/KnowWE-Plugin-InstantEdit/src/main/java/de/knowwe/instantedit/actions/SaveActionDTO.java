/*
 * Copyright (C) 2020 denkbares GmbH, Germany
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO object of Json being sent from KnowWe-EditCommons.js and KnowWE-Plugin-EditMode.js
 *
 * @author Nikolai Reed (Olyro GmbH)
 * @created 07.04.2020
 */

public class SaveActionDTO {

	private final List<SaveActionSectionDTO> sections;

	public SaveActionDTO(List<SaveActionSectionDTO> sections) {
		this.sections = sections;
	}

	public List<SaveActionSectionDTO> getSections() {
		return sections;
	}

	/**
	 * Parses the SaveActionDTO to String as new Article to replace old one
	 * Currently will only parse Type WIKI (wiki text as string) only
	 *
	 * @return StringF
	 * @throws IllegalArgumentException for DTO object if it contains JSON type that cannot be parsed
	 */
	public String buildArticle() {
		//filter out empty Sections and join the rest
		return getSections().stream().filter(s -> !s.isEmpty()).map(section -> {
			if (section.getType() == SaveActionSectionDTO.Type.WIKI) {
				return section.getWikiData();
			}
			else {
				throw new IllegalArgumentException("Received data type JSON but expected WIKI for Section " + section.getSectionID());
			}
		}).collect(Collectors.joining("\n\n")) + "\n";
	}
}
