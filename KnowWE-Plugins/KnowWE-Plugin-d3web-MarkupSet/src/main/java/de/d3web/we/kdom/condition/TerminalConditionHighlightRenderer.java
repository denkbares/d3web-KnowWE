package de.d3web.we.kdom.condition;

import java.util.ArrayList;
import java.util.List;

import com.denkbares.strings.Strings;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.session.Session;
import de.d3web.we.kdom.ruletable.RuleTableMarkup;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;

/**
 * Acts as a {@link Renderer} for single {@link TerminalCondition}s.
 *
 * @author Simon Maurer
 * @created 25.09.2017
 */
public class TerminalConditionHighlightRenderer implements Renderer {

	@Override
	public void render(Section<?> section, UserContext user, RenderResult result) {
		D3webCompiler compiler = Compilers.getCompiler(section, D3webCompiler.class);
		Session session = D3webUtils.getExistingSession(compiler, user);
		List<String> classes = new ArrayList<>();
		classes.add("TerminalCondition");
		if (session != null) {
			//noinspection unchecked
			Condition condition = Sections.cast(section, D3webCondition.class).get().createCondition(compiler,
					section);
			classes.add(RuleTableMarkup.evaluateSessionCondition(session, condition));
		}

		result.appendHtml("<span class='" + Strings.concat(" ", classes) + "'>");
		DelegateRenderer.getInstance().render(section, user, result);
		result.appendHtml("</span>");
	}
}
