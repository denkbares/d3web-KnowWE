/*
 * Copyright (C) 2015 denkbares GmbH, Germany
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

package de.knowwe.testcases.download;

import java.util.Objects;

import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.testcase.model.CheckTemplate;
import de.d3web.testcase.model.TransformationException;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.condition.KDOMConditionFactory;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.testcases.table.KnowWEConditionCheck;

/**
 * {@link CheckTemplate} for {@link KnowWEConditionCheck}s, providing a {@link Condition} from a given {@link Section}.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 03.11.15
 */
public class KnowWEConditionCheckTemplate implements CheckTemplate {

	private Section<CompositeCondition> section;

	public KnowWEConditionCheckTemplate(Section<CompositeCondition> section) {
		this.section = Objects.requireNonNull(section);
	}

	public Section<CompositeCondition> getSection() {
		return section;
	}

	@Override
	public KnowWEConditionCheck toCheck(KnowledgeBase knowledgeBase) throws TransformationException {
		D3webCompiler d3webCompiler = Compilers.getCompilers(section, D3webCompiler.class)
				.stream()
				.filter(compiler -> compiler.getKnowledgeBase() == knowledgeBase)
				.findAny()
				.orElseThrow(() -> new TransformationException("No compiler found for knowledge base, unable to create Check!"));
		Condition condition = KDOMConditionFactory.createCondition(d3webCompiler, section);
		if (condition == null) throw new TransformationException("No valid condition found in '" + section.getText() + "'");
		return new KnowWEConditionCheck(condition, section);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		KnowWEConditionCheckTemplate that = (KnowWEConditionCheckTemplate) o;

		return section.equals(that.section);

	}

	@Override
	public int hashCode() {
		return section.hashCode();
	}
}
