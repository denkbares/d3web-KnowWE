package com.denkbares.knowwe.textdiff;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

/**
 * Computes a diff between two raw texts on the fly and returns the inner shadow-root HTML
 * (stylesheet link + diff table) suitable for injection into a {@code <knowwe-text-diff>}
 * web component on the client. Used by the JS component's lazy-load path.
 *
 * <p>Expects a JSON request body in the {@code data} parameter; see {@link Request}.
 */
public class TextDiffAction extends AbstractAction {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final int DEFAULT_CONTEXT_LINES = 3;

	/**
	 * JSON request payload. {@code oldText} and {@code newText} may be {@code null} to indicate a
	 * fully-added or fully-removed file. {@code contextLines} is optional; absent or negative
	 * means "show all lines, no elision".
	 */
	public record Request(@Nullable String oldText, @Nullable String newText, @Nullable Integer contextLines) {
	}

	@Override
	public void execute(UserActionContext context) throws IOException {
		Request request = MAPPER.readValue(context.getParameter("data"), Request.class);
		int contextLines = request.contextLines != null ? request.contextLines : DEFAULT_CONTEXT_LINES;

		TextDiff diff = new TextDiff(request.oldText, request.newText);
		DiffRenderOptions options = DiffRenderOptions.defaults().withContextLines(contextLines);
		String html = DiffHtmlRenderer.renderTextDiffShadowContent(diff, options);

		context.setContentType("text/html; charset=UTF-8");
		context.getWriter().write(html);
	}
}
