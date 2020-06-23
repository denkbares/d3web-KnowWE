package de.knowwe.instantedit.actions;

import com.denkbares.strings.Strings;

/**
 * DTO Section object of Json being sent from KnowWe-EditCommons.js and KnowWE-Plugin-EditMode.js
 *
 * @author Nikolai Reed (Olyro GmbH)
 * @created 07.04.2020
 */

public class SaveActionSectionDTO {

	private final Type type;
	private final String sectionID;
	private final String data;

	public enum Type {
		WIKI,
		JSON
	}

	public SaveActionSectionDTO(Type type, String sectionID, String data) {
		this.type = type;
		this.sectionID = sectionID;
		this.data = data;
	}

	public Type getType() {
		return type;
	}

	public String getSectionID() {
		return sectionID;
	}

	public String getWikiData() {
		return Strings.trim(data);
	}

	public boolean isEmpty() {
		return Strings.isBlank(this.data);
	}
}
