package de.knowwe.core.kdom.rendering.elements;

import de.knowwe.core.kdom.rendering.RenderResult;

/**
 * Interface for interaction with RenderResult
 *
 * @created 18.06.2024
 * @author Albrecht Striffler (denkbares GmbH)
 */
public interface HtmlProvider {

	/**
	 * Write the HTML of this element to the given {@link RenderResult}
	 */
	void write(RenderResult result);
}
