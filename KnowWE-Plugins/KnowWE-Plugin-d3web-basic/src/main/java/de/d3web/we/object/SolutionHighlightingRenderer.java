/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */

package de.d3web.we.object;

import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.Rating;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.session.Session;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * Highlights the Solutions according to their state in the current case. The Renderer should onyl be applied to section
 * types that are implementations of {@link D3webTerm}, and they only apply the highlight if the D3webTerm returns a
 * solution instance.
 *
 * @author Johannes Dienst, Volker Belli (denkbares GmbH)
 */
public class SolutionHighlightingRenderer implements Renderer {

	private final Renderer innerRenderer;

	public SolutionHighlightingRenderer(Renderer innerRenderer) {
		this.innerRenderer = innerRenderer;
	}

	@Override
	public void render(Section<?> sec, UserContext user, RenderResult string) {

		// determine color to highlight
		Rating.State state = getState(sec, user);
		StyleRenderer styleRenderer = (state == Rating.State.ESTABLISHED) ? StyleRenderer.CONDITION_FULFILLED :
				(state == Rating.State.EXCLUDED) ? StyleRenderer.CONDITION_FALSE : null;

		if (styleRenderer != null) {
			string.appendHtml("<span class='").append(styleRenderer.getCssClass()).appendHtml("'>");
		}
		innerRenderer.render(sec, user, string);
		if (styleRenderer != null) {
			string.appendHtml("</span>");
		}
	}

	private Rating.State getState(Section<?> sec, UserContext user) {
		D3webCompiler compiler = Compilers.getCompiler(user, sec, D3webCompiler.class);
		if (compiler != null) {
			Session session = D3webUtils.getExistingSession(compiler, user);

			if (session != null) {
				//noinspection rawtypes
				Section<D3webTerm> term = Sections.cast(sec, D3webTerm.class);
				//noinspection unchecked
				NamedObject object = term.get().getTermObject(compiler, term);
				if (object instanceof Solution) {
					return session.getBlackboard().getRating((Solution) object).getState();
				}
			}
		}
		return Rating.State.UNCLEAR;
	}
}
