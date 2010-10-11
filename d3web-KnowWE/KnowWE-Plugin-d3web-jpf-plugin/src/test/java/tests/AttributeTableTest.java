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

package tests;

import java.io.IOException;

import junit.framework.TestCase;
import utils.KBCreationTestUtil;
import utils.MyTestArticleManager;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.knowledge.terminology.info.DCElement;
import de.d3web.core.knowledge.terminology.info.DCMarkup;
import de.d3web.core.knowledge.terminology.info.MMInfoObject;
import de.d3web.core.knowledge.terminology.info.MMInfoStorage;
import de.d3web.core.knowledge.terminology.info.MMInfoSubject;
import de.d3web.plugin.test.InitPluginManager;
import de.d3web.we.kdom.KnowWEArticle;

/**
 * This class tests whether the Objects got the right MMInfo from the
 * AttributeTable
 * 
 * @author Sebastian Furth
 * 
 */
public class AttributeTableTest extends TestCase {

	@Override
	protected void setUp() throws IOException {
		InitPluginManager.init();
	}

	public void testMMInfo() {

		KnowWEArticle art = MyTestArticleManager.getArticle(KBCreationTestUtil.KBCREATION_ARTICLE_FILE);
		KnowledgeBase loadedKB = MyTestArticleManager.getKnowledgeBase(art);
		KnowledgeBase createdKB = KBCreationTestUtil.getInstance().getCreatedKB();

		// Get Diagnosis with ID "P1": "Mechanical Problem"
		Solution loadedDiag = loadedKB.searchSolution("P1");
		Solution createdDiag = createdKB.searchSolution("P1");

		// Get MMInfoStorage of diagnoses
		MMInfoStorage loadedStorage = (MMInfoStorage) loadedDiag.getInfoStore().getValue(
				BasicProperties.MMINFO);
		MMInfoStorage createdStorage = (MMInfoStorage) createdDiag.getInfoStore().getValue(
				BasicProperties.MMINFO);
		assertNotNull("Diagnosis " + loadedDiag.getName() + " has no MMInfoStorage.", loadedStorage);
		assertNotNull("Diagnosis " + createdDiag.getName() + " has no MMInfoStorage.",
				createdStorage);

		// Create DCMarkup
		DCMarkup markup = new DCMarkup();
		markup.setContent(DCElement.SOURCE, loadedDiag.getId());
		markup.setContent(DCElement.TITLE, "description");
		markup.setContent(DCElement.SUBJECT, MMInfoSubject.INFO.getName());

		// Get MMInfoObject for created DCMarkup
		MMInfoObject loadedMMInfo = (MMInfoObject) loadedStorage.getMMInfo(markup).toArray()[0];
		MMInfoObject createdMMInfo = (MMInfoObject) createdStorage.getMMInfo(markup).toArray()[0];
		assertNotNull("Diagnosis " + loadedDiag.getName() + " has no MMInfo.", loadedMMInfo);
		assertNotNull("Diagnosis " + createdDiag.getName() + " has no MMInfo.", createdMMInfo);

		// Compare content of MMInfoObject
		assertEquals("Content of MMInfoObject of Diagnosis " + createdDiag.getName() + " differs.",
				createdMMInfo.getContent(), loadedMMInfo.getContent());

	}

}
