package de.knowwe.kdom.renderer;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;

/**
 * Renderer that wraps an other renderer to enable asynchron rendering of the
 * wrapped renderer (delegate). Instead of the delegate a placeholder will be
 * rendered and the real content will be fetched by ajax and embedded to the web
 * page as soon as the rendering result is available.
 * <p>
 * Use this renderer to wrap your original renderer if you expect long rendering
 * times to improve user experience.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 21.11.2013
 */
public class AsynchronRenderer implements Renderer {

	private final Renderer decoratedRenderer;

	public AsynchronRenderer(Renderer decoratedRenderer) {
		this.decoratedRenderer = decoratedRenderer;
	}

	@Override
	public void render(Section<?> section, UserContext user, RenderResult result) {
		boolean ajaxAction = user.getParameters().containsKey("action");
		if (ajaxAction) {
			decoratedRenderer.render(section, user, result);
		}
		else {
			String id = section.getID();
			result.appendHtml("<span class='asynchronRenderer'")
					.appendHtml(" id='").append(id).appendHtml("'")
					.appendHtml(" rel=\"{id:'").append(id).appendHtml("'}\"></span>");
		}
	}

}
