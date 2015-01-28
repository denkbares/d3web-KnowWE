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
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.ValueObject;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.core.session.blackboard.Fact;
import de.d3web.core.utilities.ExplanationUtils;
import de.d3web.core.utilities.TerminologyHierarchyComparator;
import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.solutionpanel.SolutionPanelUtils;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
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

	protected int getTooltipDelay(Section<?> section, UserContext user) {
		return 200;
	}

	@Override
	protected String getTooltip(Section<?> section, UserContext user) {
		if (!(section.get() instanceof D3webTerm<?>)) return null;

		@SuppressWarnings("unchecked")
		Section<D3webTerm<NamedObject>> sec = (Section<D3webTerm<NamedObject>>) section;
		StringBuilder builder = new StringBuilder();
		Collection<D3webCompiler> compilers = Compilers.getCompilers(section, D3webCompiler.class);
		boolean first = true;
		for (D3webCompiler compiler : compilers) {

			NamedObject namedObject = sec.get().getTermObject(compiler, sec);
			KnowledgeBase knowledgeBase = D3webUtils.getKnowledgeBase(compiler);
			Session session = SessionProvider.getSession(user, knowledgeBase);
			if (namedObject instanceof ValueObject) {
				Value value = D3webUtils.getValueNonBlocking(session, (ValueObject) namedObject);
				if (value == null) continue;
				String name = knowledgeBase.getName();
				if (name == null) name = compiler.getCompileSection().getTitle();
				if (!first) {
					builder.append("<br/>");
				}
				else {
					first = false;
				}
				builder.append("Current value");
				if (compilers.size() > 1) {
					builder.append(" in '").append(name).append("'");
				}
				builder.append(": ");
				builder.append(SolutionPanelUtils.formatValue(value, -1));

				if (compilers.size() == 1) {
					Collection<Fact> sourceFacts = ExplanationUtils.getSourceFactsNonBlocking(session, (TerminologyObject) namedObject);
					List<Fact> filteredSourceFacts = sourceFacts.stream()
							.filter(fact -> fact.getTerminologyObject() != namedObject)
							.collect(toList());
					filteredSourceFacts.sort(Comparator.comparing(Fact::getTerminologyObject, COMPARATOR));
					if (!filteredSourceFacts.isEmpty()) {
						builder.append("<p>The following input values were used the derive this value:");
						builder.append("<ul>");
						for (Fact sourceFact : sourceFacts) {
							String valueString = getValueString(value, sourceFact);
							Identifier identifier = new Identifier(sourceFact.getTerminologyObject().getName());
							String urlLinkToTermDefinition = KnowWEUtils.getURLLinkToObjectInfoPage(identifier);
							builder.append("<li>")
									.append("<a href='").append(urlLinkToTermDefinition)
									.append("'>")
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
		}
		return builder.toString();
	}

	private String getValueString(Value value, Fact sourceFact) {
		String valueString = sourceFact.getValue().toString();
		if (value.getValue() instanceof Date) {
			Date dateValue = (Date) value.getValue();
			if (dateValue.getTime() < TimeUnit.DAYS.toMillis(365)) {
				valueString = Strings.getDurationVerbalization(dateValue.getTime());
			}
		}
		return valueString;
	}
}
