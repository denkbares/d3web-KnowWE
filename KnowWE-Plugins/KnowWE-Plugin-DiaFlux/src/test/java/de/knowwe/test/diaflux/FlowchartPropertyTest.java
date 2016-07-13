/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.knowwe.test.diaflux;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import utils.TestArticleManager;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.diaFlux.inference.DiaFluxUtils;
import com.denkbares.plugin.test.InitPluginManager;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.Article;

/**
 * 
 * @author Reinhard Hatko
 * @created 27.05.2013
 */
public class FlowchartPropertyTest {

	static String flowName = "Flow";
	String FILE = "src/test/resources/FlowchartPropertyTest.txt";
	KnowledgeBase kb;
	private Flow flow;

	@Before
	public void setUp() throws IOException {
		InitPluginManager.init();
		Article art = TestArticleManager.getArticle(FILE);
		kb = D3webUtils.getKnowledgeBase(art);
	}

	@Test
	public void testProperty() {
		flow = DiaFluxUtils.getFlowSet(kb).get(flowName);
		String value = flow.getInfoStore().getValue(MMInfo.DESCRIPTION);
		assertThat(value, is("This is a test property"));
	}

}
