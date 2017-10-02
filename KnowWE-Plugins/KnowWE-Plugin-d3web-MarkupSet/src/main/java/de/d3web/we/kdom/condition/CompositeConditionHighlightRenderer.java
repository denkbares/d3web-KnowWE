package de.d3web.we.kdom.condition;

import java.util.ArrayList;
import java.util.List;

import com.denkbares.strings.Strings;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.session.Session;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.kdom.condition.helper.BracedConditionContent;
import de.d3web.we.kdom.rules.condition.ConditionContainer;
import de.d3web.we.kdom.rules.condition.IfConditionContainer;
import de.d3web.we.kdom.ruletable.RuleTableMarkup;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;

/**
 * Acts as a {@link Renderer} for {@link CompositeCondition}s.
 *
 * @author Simon Maurer
 * @created 25.09.2017
 */
public class CompositeConditionHighlightRenderer implements Renderer {

	@Override
	public void render(Section<?> section, UserContext user, RenderResult result) {
		D3webCompiler compiler = Compilers.getCompiler(section, D3webCompiler.class);
		Session session = null;

		List<String> classes = new ArrayList<>();
		if (compiler != null) {
			session = SessionProvider.getSession(user, D3webUtils.getKnowledgeBase(compiler));
		}
		if (session != null) {
			Section<CompositeCondition> sec = Sections.cast(section, CompositeCondition.class);
			Section<? extends Type> parent = sec.getParent();
			if (parent.get() instanceof IfConditionContainer || parent.get() instanceof ConditionContainer) {
				// the tag "CompositeCondition" always determines the outermost span of a CompositeCondition
				classes.add("CompositeCondition");
			}
			else if (parent.get() instanceof Disjunct) {
				classes.add("Disjunct");
			}
			else if (parent.get() instanceof Conjunct) {
				classes.add("Conjunct");
			}
			else if (parent.get() instanceof BracedConditionContent) {
				classes.add("BracedConditionContent");
			}

			// TerminalConditions are evaluated at TerminalConditionHighlightRenderer.class, not here, as
			// TerminalConditions can also exist without being content of a CompositeCondition
			if (sec.get().isTerminal(sec)) {
				classes.add("TerminalCondition");
			}
			else {
				if (sec.get().isBraced(sec)) {
					classes.add("BracedCondition");
				}
				else if (sec.get().isDisjunction(sec)) {
					classes.add("Disjunction");
				}
				else if (sec.get().isConjunction(sec)) {
					classes.add("Conjunction");
				}
				Condition condition = KDOMConditionFactory.createCondition(compiler, sec);
				if (!(parent.get() instanceof BracedConditionContent)) {
					// BracedConditionContents must not be evaluated, as their parent BracedCondition already has
					// been
					classes.add(RuleTableMarkup.evaluateSessionCondition(session, condition));
				}
			}
		}
		result.appendHtml("<span id='" + section.getID() + "' class='" + Strings.concat(" ", classes) + "'>");
		DelegateRenderer.getInstance().render(section, user, result);
		result.appendHtml("</span>");
	}
}
