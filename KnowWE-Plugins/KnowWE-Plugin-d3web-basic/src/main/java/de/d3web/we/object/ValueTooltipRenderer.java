/*
 * Copyright (C) 2010 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package de.d3web.we.object;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Identifier;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.ValueObject;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.core.session.ValueUtils;
import de.d3web.core.session.blackboard.Fact;
import de.d3web.core.session.values.NumValue;
import de.d3web.core.utilities.ExplanationUtils;
import de.d3web.core.utilities.TerminologyHierarchyComparator;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.tools.CompositeEditToolProvider;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.renderer.TooltipRenderer;

import static java.util.stream.Collectors.toList;

/**
 * Renders a D3webTerm section by adding the current value(s) as a tooltip.
 *
 * @author volker_belli
 * @created 30.11.2010
 */
public class ValueTooltipRenderer extends TooltipRenderer {

	public static final TerminologyHierarchyComparator COMPARATOR = new TerminologyHierarchyComparator();

	public ValueTooltipRenderer(Renderer decoratedRenderer) {
		super(decoratedRenderer);
	}

	public ValueTooltipRenderer() {
	}

	/**
	 * Renders the string representation of the specified value. For a {@link NumValue} the float is truncated to its
	 * integer value, when possible.
	 *
	 * @param value the specified value
	 * @return A string representation of the specified value.
	 * @created 19.10.2010
	 */
	public static String formatValue(ValueObject object, Value value) {
		return ValueUtils.getVerbalization(object, value, null);
	}

	@Override
	public boolean hasTooltip(Section<?> section, UserContext user) {
		if (!(section.get() instanceof D3webTerm)) return false;
		Section<D3webTerm> sec = Sections.cast(section, D3webTerm.class);
		Collection<D3webCompiler> compilers = Compilers.getCompilers(section, D3webCompiler.class);
		for (D3webCompiler compiler : compilers) {
			@SuppressWarnings("unchecked")
			NamedObject namedObject = sec.get().getTermObject(compiler, sec);
			if (namedObject instanceof Question) return true;
			if (namedObject instanceof Solution) return true;
		}
		return false;
	}

	@Override
	protected int getTooltipDelay(Section<?> section, UserContext user) {
		return 1000;
	}

	@Override
	public String getTooltip(Section<?> section, UserContext user) {
		if (!(section.get() instanceof D3webTerm)) return null;

		Section<D3webTerm> sec = Sections.cast(section, D3webTerm.class);
		StringBuilder builder = new StringBuilder();

		Collection<D3webCompiler> compilers = Compilers.getCompilers(section, D3webCompiler.class);
		boolean first = true;
		for (D3webCompiler compiler : compilers) {

			@SuppressWarnings("unchecked")
			NamedObject namedObject = sec.get().getTermObject(compiler, sec);
			if (namedObject instanceof ValueObject) {
				KnowledgeBase knowledgeBase = D3webUtils.getKnowledgeBase(compiler);
				Session session = SessionProvider.getExistingSession(user, knowledgeBase);
				if (session == null) continue;
				Value value = D3webUtils.getValueNonBlocking(session, (ValueObject) namedObject);
				if (value == null) continue;
				String name = knowledgeBase.getName();
				if (name == null) name = compiler.getCompileSection().getTitle();
				if (first) {
					first = false;
				}
				else {
					builder.append("<br/>");
				}
				appendCurrentValue((ValueObject) namedObject, value, compilers.size() > 1 ? name : null, builder);

				if (compilers.size() == 1) {
					appendSourceFactsExplanation(namedObject, session, builder);
				}
			}
		}
		return builder.toString();
	}

	public static void appendCurrentValue(@NotNull ValueObject namedObject, @NotNull Value value, @NotNull StringBuilder builder) {
		appendCurrentValue(namedObject, value, null, builder);
	}

	public static void appendCurrentValue(@NotNull ValueObject namedObject, @NotNull Value value, @Nullable String source, @NotNull StringBuilder builder) {
		builder.append("Current value");
		if (source != null) {
			builder.append(" in '").append(source).append("'");
		}
		builder.append(": ");
		builder.append(formatValue(namedObject, value));
	}

	public static void appendSourceFactsExplanation(NamedObject namedObject, Session session, StringBuilder builder) {
		Collection<Fact> sourceFacts = ExplanationUtils.getSourceFactsNonBlocking(session, (TerminologyObject) namedObject);
		List<Fact> filteredSourceFacts = sourceFacts.stream()
				.filter(fact -> fact.getTerminologyObject() != namedObject)
				.sorted(Comparator.comparing(Fact::getTerminologyObject, COMPARATOR))
				.collect(toList());
		if (!filteredSourceFacts.isEmpty()) {
			builder.append("<p>The following input values were used to derive this value:");
			builder.append("<ul>");
			for (Fact sourceFact : sourceFacts) {
				String valueString = formatValue((ValueObject) sourceFact.getTerminologyObject(), sourceFact.getValue());
				Identifier identifier = new Identifier(sourceFact.getTerminologyObject().getName());
				String urlLinkToTermDefinition = CompositeEditToolProvider.createCompositeEditModeAction(identifier);
				builder.append("<li>")
						.append("<a onclick=\"").append(urlLinkToTermDefinition).append("\">")
						.append(sourceFact.getTerminologyObject().getName())
						.append("</a>")
						.append(" = ")
						.append(valueString)
						.append("</li>");
			}
			builder.append("</ul>");
		}
	}
}
