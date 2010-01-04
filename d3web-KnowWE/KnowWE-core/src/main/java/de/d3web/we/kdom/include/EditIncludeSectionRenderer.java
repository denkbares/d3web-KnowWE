package de.d3web.we.kdom.include;

import de.d3web.we.kdom.rendering.EditSectionRenderer;

public class EditIncludeSectionRenderer extends EditSectionRenderer {

	public EditIncludeSectionRenderer(IncludeSectionRenderer instance) {
		super(instance);
	}

	@Override
	protected String getQuickEditDivAttributes() {
		return "class=\"right\" style=\"padding-right: 6px; padding-top: 2px;\"";
	}
}
