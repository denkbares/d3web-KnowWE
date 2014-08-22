package de.knowwe.kdom.renderer;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Renderer that wraps an other renderer to enable asynchron rendering of the
 * wrapped renderer (delegate). Instead of the delegate a placeholder will be
 * rendered and the real content will be fetched by ajax and embedded to the web
 * page as soon as the rendering result is available.
 * <p/>
 * Use this renderer to wrap your original renderer if you expect long rendering
 * times to improve user experience.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 21.11.2013
 */
public class AsynchronRenderer implements Renderer {

	private final Renderer decoratedRenderer;
	private final boolean inline;

	public static final String ASYNCHRONOUS = "asynchronous";

	public AsynchronRenderer(Renderer decoratedRenderer) {
		this(decoratedRenderer, false);
	}

	public AsynchronRenderer(Renderer decoratedRenderer, boolean inline) {
		this.decoratedRenderer = decoratedRenderer;
		this.inline = inline;
	}

	@Override
	public void render(Section<?> section, UserContext user, RenderResult result) {
		boolean asynchronous = isAsynchronous(section);
		boolean ajaxAction = user.getParameters().containsKey("action");
		if (ajaxAction || !asynchronous) {
			decoratedRenderer.render(section, user, result);
		}
		else {
			String id = section.getID();
			String size = inline ? "asynchronSmall" : "asynchronNormal";
			result.appendHtml("<span class='asynchronRenderer " + size + "'")
					.appendHtml(" id='").append(id).appendHtml("'")
					.appendHtml(" rel=\"{id:'").append(id).appendHtml("'}\"></span>");
		}
	}

	private boolean isAsynchronous(Section<?> section) {
		Section<DefaultMarkupType> defaultMarkupSection = getDefaultMarkupSection(section);
		if (defaultMarkupSection == null) return true;
		String asynchronousString = DefaultMarkupType.getAnnotation(
				defaultMarkupSection, ASYNCHRONOUS);
		if (asynchronousString == null) return true;
		asynchronousString = asynchronousString.trim().toLowerCase();
		return !asynchronousString.equals("false");
	}

	private Section<DefaultMarkupType> getDefaultMarkupSection(Section<?> section) {
		if (section.get() instanceof DefaultMarkupType) {
			return Sections.cast(section, DefaultMarkupType.class);
		}
		else {
			return Sections.ancestor(section, DefaultMarkupType.class);
		}
	}

}
