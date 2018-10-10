package de.knowwe.jspwiki;

import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * @author Tim Abler
 * @created 09.10.2018
 */
public class AdministrationMarkup extends DefaultMarkupType{

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("Administration");
	}

	public AdministrationMarkup() {
		super(MARKUP);
		this.setRenderer(new AdministrationMarkupRenderer());
	}
}
