package de.knowwe.instantedit.actions;

/**
 * DTO Section object of Json being sent from KnowWe-EditCommons.js and KnowWE-Plugin-EditMode.js
 *
 * @author Nikolai Reed (Olyro GmbH)
 * @created 07.04.2020
 */

public class SaveActionSectionDTO {

    private Type type;
    private String sectionID;
    private String data;

    enum Type {
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
        return data.trim();
    }

    public boolean isEmpty(){
        return this.data.trim().isEmpty();
    }
}
