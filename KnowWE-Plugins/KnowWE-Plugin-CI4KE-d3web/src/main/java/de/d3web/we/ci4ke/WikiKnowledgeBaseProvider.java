package de.d3web.we.ci4ke;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.testing.TestObjectContainer;
import de.d3web.testing.TestObjectProvider;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.utils.KnowWEUtils;

/*
 * Copyright (C) 2012 denkbares GmbH
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

/**
 * Provides KnowledgeBases for the TestingApp or the CIDashboard.
 * 
 * @author Jochen Reutelshöfer (denkbares GmbH)
 * @created 22.05.2012
 */
public class WikiKnowledgeBaseProvider implements TestObjectProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(WikiKnowledgeBaseProvider.class);

	@Override
	public <T> List<TestObjectContainer<T>> getTestObjects(Class<T> c, String id) {
		if (c == null) {
			LOGGER.warn("Class given to TestObjectProvider was 'null'");
			return Collections.emptyList();
		}
		if (!c.equals(KnowledgeBase.class)) {
			return Collections.emptyList();
		}
		List<TestObjectContainer<T>> result = new ArrayList<>();
		try {
			Pattern.compile(id);
		}
		catch (java.util.regex.PatternSyntaxException e) {
			return result;
		}

		Collection<D3webCompiler> compilers = Compilers.getCompilers(
				KnowWEUtils.getArticleManager(Environment.DEFAULT_WEB), D3webCompiler.class);

		for (D3webCompiler compiler : compilers) {
			if (compiler.getCompileSection().getTitle().matches(id)) {
				KnowledgeBase kb = compiler.getKnowledgeBase();
				TestObjectContainer<T> container = new TestObjectContainer<>(kb.getId(),
						c.cast(kb));
				result.add(container);
			}
		}
		return result;
	}
}
