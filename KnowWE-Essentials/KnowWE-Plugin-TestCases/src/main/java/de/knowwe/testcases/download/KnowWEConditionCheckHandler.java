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

import java.io.IOException;

import org.w3c.dom.Element;

import de.d3web.core.inference.condition.Condition;
import de.d3web.core.io.KnowledgeBasePersistence;
import de.d3web.core.io.Persistence;
import de.d3web.core.io.PersistenceManager;
import de.d3web.core.io.fragments.FragmentHandler;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.testcase.model.TestCase;
import de.d3web.testcase.model.TransformationException;
import de.d3web.testcase.persistence.ConditionPersistenceCheckHandler;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.testcases.table.KnowWEConditionCheck;

/**
 * Handles writing of {@link KnowWEConditionCheckTemplate}s. Reading is done with {@link ConditionPersistenceCheckHandler}.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 03.11.15
 */
public class KnowWEConditionCheckHandler implements FragmentHandler<TestCase> {


	@Override
	public Object read(Element element, Persistence<TestCase> persistence) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Element write(Object object, Persistence<TestCase> persistence) throws IOException {
		KnowWEConditionCheckTemplate checkTemplate = (KnowWEConditionCheckTemplate) object;
		Section<CompositeCondition> section = checkTemplate.getSection();
		if (section.hasErrorInSubtree()) {
			throw new IOException("Cannot export Checks of Sections that have compile errors: " + section.getText());
		}
		D3webCompiler compiler = D3webUtils.getCompiler(section);
		KnowledgeBase knowledgeBase = compiler.getKnowledgeBase();
		KnowWEConditionCheck check;
		try {
			check = checkTemplate.toCheck(knowledgeBase);
		}
		catch (TransformationException e) {
			throw new IOException("Exception while generating");
		}
		Condition condition = check.getConditionObject();

		KnowledgeBasePersistence knowledgeBasePersistence = new KnowledgeBasePersistence(PersistenceManager.getInstance(), knowledgeBase);
		Element conditionElement = knowledgeBasePersistence.writeFragment(condition);

		return ConditionPersistenceCheckHandler.createCheckElement(persistence, conditionElement);
	}

	@Override
	public boolean canRead(Element element) {
		// not supposed to read... we create ConditionPersistenceCheckTemplate which has its own handler...
		return false;
	}

	@Override
	public boolean canWrite(Object object) {
		return object instanceof KnowWEConditionCheckTemplate;
	}
}
