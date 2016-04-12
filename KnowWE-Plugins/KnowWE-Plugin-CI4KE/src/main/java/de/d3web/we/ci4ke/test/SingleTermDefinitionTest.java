/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.d3web.we.ci4ke.test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import de.d3web.strings.Identifier;
import de.d3web.testing.AbstractTest;
import de.d3web.testing.Message;
import de.d3web.testing.TestParameter.Mode;
import de.d3web.testing.TestParameter.Type;
import de.d3web.testing.TestingUtils;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;


/**
 * This test checks, if each term is defined exactly once.
 * 
 * @author Reinhard Hatko
 * @created 27.03.2013
 */
public class SingleTermDefinitionTest extends AbstractTest<Article> {

	public SingleTermDefinitionTest() {
		this.addIgnoreParameter("terms", Type.Regex, Mode.Optional,
				"Term names to ignore for this test.");
	}

	@Override
	public Message execute(Article article, String[] args, String[]... ignores) throws InterruptedException {
		if (article == null) throw new IllegalArgumentException("No article provided.");

		TerminologyManager manager = Environment.getInstance().getTerminologyManager(article.getWeb(), article.getTitle());
		
		Collection<Identifier> terms = manager.getAllDefinedTerms();
		
		Map<String, Collection<Section<?>>> multipleDefs = new HashMap<String, Collection<Section<?>>>();
		
		Collection<Pattern> ignorePatterns = TestingUtils.compileIgnores(ignores);

		for (Identifier termIdentifier : terms) {

			if (TestingUtils.isIgnored(termIdentifier.toString(), ignorePatterns)) continue;

			Collection<Section<?>> sections = manager.getTermDefiningSections(termIdentifier);
			
			if (sections.size() > 1) {
				multipleDefs.put(termIdentifier.toString(), sections);
			}
			
		}
		
		if (multipleDefs.isEmpty()) {
			return Message.SUCCESS;
		}
		
		return TestingUtils.createFailure("The following objects are defined more than once:", multipleDefs
				.keySet(),
				Identifier.class);
	}

	@Override
	public Class<Article> getTestObjectClass() {
		return Article.class;
	}

	@Override
	public String getDescription() {
		return "This test checks, if each term is defined only once.";
	}


}
