package de.knowwe.kdom.renderer;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.util.Icon;

import static de.knowwe.core.Attributes.REASON;

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
public class AsynchronousRenderer implements Renderer {

	public static final String ASYNCHRON_RENDERER = "asynchronRenderer";
	protected final Renderer decoratedRenderer;
	protected final boolean inline;

	public static final String ASYNCHRONOUS = "asynchronous";

	public AsynchronousRenderer(Renderer decoratedRenderer) {
		this(decoratedRenderer, false);
	}

	public AsynchronousRenderer(Renderer decoratedRenderer, boolean inline) {
		this.decoratedRenderer = decoratedRenderer;
		this.inline = inline;
	}

	protected AsynchronousRenderer() {
		decoratedRenderer = this;
		inline = false;
	}

	@Override
	public void render(Section<?> section, UserContext user, RenderResult result) {
		if (shouldRenderPreview(section, user)) {
			renderPreview(section, user, result);
		}
		else {
			decoratedRenderer.render(section, user, result);
		}
	}

	protected void renderPreview(Section<?> section, UserContext user, RenderResult result) {
		String id = section.getID();
		String[] attributes = new String[] { "class", ASYNCHRON_RENDERER, "id", id, "rel", "rel=\"{id:'" + id + "'}\"" };

		result.appendHtmlTag("div", attributes);
		if (decoratedRenderer instanceof AsyncPreviewRenderer) {
			((AsyncPreviewRenderer) decoratedRenderer).renderAsyncPreview(section, user, result);
		}
		else {
			result.appendHtml(Icon.LOADING.addClasses(inline ? "asynchronSmall" : "asynchronNormal").toHtml());
		}
		result.appendHtmlTag("/div");
	}

	protected boolean shouldRenderPreview(Section<?> section, UserContext user) {
		if (isAsynchronousRerenderingRequest(user)) return false;
		if (decoratedRenderer instanceof AsyncPreviewRenderer) {
			AsyncPreviewRenderer asyncRenderer = (AsyncPreviewRenderer) this.decoratedRenderer;
			if (!asyncRenderer.shouldRenderAsynchronous(section, user)) return false;
		}
		Section<DefaultMarkupType> defaultMarkupSection = getDefaultMarkupSection(section);
		if (defaultMarkupSection == null) return true;
		String asynchronousString = DefaultMarkupType.getAnnotation(defaultMarkupSection, ASYNCHRONOUS);
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

	/**
	 * Check whether the request was generated to get the actual result for a previously rendered preview, return true
	 * in this case. False otherwise.
	 */
	public static boolean isAsynchronousRerenderingRequest(UserContext user) {
		return ASYNCHRON_RENDERER.equals(user.getParameter(REASON));
	}
}
