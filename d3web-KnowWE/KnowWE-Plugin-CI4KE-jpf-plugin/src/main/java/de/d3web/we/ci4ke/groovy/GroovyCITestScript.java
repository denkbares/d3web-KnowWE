/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.d3web.we.ci4ke.groovy;

import groovy.lang.Script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.d3web.empiricaltesting.TestSuite;
import de.d3web.we.ci4ke.handling.CIConfig;
import de.d3web.we.ci4ke.handling.CITest;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.xcl.XCLRelation;
import de.d3web.we.kdom.xcl.XCList;
import de.d3web.we.testsuite.kdom.TestSuiteType;
import de.d3web.we.utils.KnowWEUtils;

public abstract class GroovyCITestScript extends Script implements CITest {

	public GroovyCITestScript() {
		super();
	}

	protected CIConfig config;

	@Override
	public void init(CIConfig config) {
		this.config = config;
	}

	public Collection<KnowWEArticle> getAllArticles() {
		return KnowWEEnvironment.getInstance().getArticleManager(
				KnowWEEnvironment.DEFAULT_WEB).getArticles();
	}

	public KnowWEArticle getArticle() {
		return KnowWEEnvironment.getInstance().getArticle(KnowWEEnvironment.DEFAULT_WEB,
				this.config.getMonitoredArticleTitle());
	}

	public TestSuite getTestSuite() {
		Section<TestSuiteType> section = getArticle().getSection().
				findSuccessor(TestSuiteType.class);
		if (section != null) {
			TestSuite suite = (TestSuite) KnowWEUtils.getStoredObject(section,
					TestSuiteType.TESTSUITEKEY);
			return suite;
		}
		return null;
	}

	public List<String> findXCListsWithLessThenXRelations(int limitRelations) {

		List<String> sectionIDs = new ArrayList<String>();

		List<Section<XCList>> found = new ArrayList<Section<XCList>>();
		getArticle().getSection().findSuccessorsOfType(XCList.class, found);

		for (Section<XCList> xclSection : found) {
			List<Section<XCLRelation>> relations = new ArrayList<Section<XCLRelation>>();
			xclSection.findSuccessorsOfType(XCLRelation.class, relations);
			if (relations.size() < limitRelations) sectionIDs.add(xclSection.getID());
		}
		return sectionIDs;
	}

}
