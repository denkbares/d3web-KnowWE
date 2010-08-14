package de.d3web.we.kdom.edit;

import de.d3web.we.kdom.rendering.EditSectionRenderer;
import de.d3web.we.kdom.xml.XMLContent;

/**
 * <p>
 * Represents the body of the <code>Edit</code> tag.
 * </p>
 * 
 * @author smark
 * @see XMLContent
 */
public class EditContent extends XMLContent {

	@Override
	protected void init() {
		this.setCustomRenderer(new EditSectionRenderer());

	}
}
