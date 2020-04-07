package de.knowwe.instantedit.actions;

import java.util.List;

/**
 * DTO object of Json being sent from KnowWe-EditCommons.js and KnowWE-Plugin-EditMode.js
 *
 * @author Nikolai Reed (Olyro GmbH)
 * @created 07.04.2020
 */

public class SaveActionDTO {

    private List<SaveActionSectionDTO> sections;

    public SaveActionDTO(List<SaveActionSectionDTO> sections) {
        this.sections = sections;
    }

    public List<SaveActionSectionDTO> getSections() {
        return sections;
    }
}
