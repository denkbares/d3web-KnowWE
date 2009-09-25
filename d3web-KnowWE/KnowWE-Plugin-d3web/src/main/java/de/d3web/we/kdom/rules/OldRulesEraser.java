/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
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

package de.d3web.we.kdom.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.KnowledgeSlice;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEUtils;

public class OldRulesEraser {
	
	public static void deleteRules(Section s, KnowledgeBaseManagement kbm) {

		if (kbm != null) {
			//long start = System.nanoTime();
			KnowWEArticle oldArt = s.getArticle().getOldArticle();
				
			// get all Rules of the old article
			List<Section> oldRules = new ArrayList<Section>();
			oldArt.getSection().findSuccessorsOfType(Rule.class, oldRules);
			
			// store all KnowledgeBase-Ids of those old Rules, that havn't got reused in the current article
			Set<String> idsToDelete = new HashSet<String>();
			for (Section or:oldRules) {
				if (!or.isReused()) {
					idsToDelete.add((String) KnowWEUtils.getOldStoredObject(oldArt.getWeb(), oldArt.getTitle(), or.getId(), Rule.KBID_KEY));
				}
			}
			
			// delete the rules from the KnowledgeBase
			Collection<KnowledgeSlice> ruleComplexes = kbm.getKnowledgeBase().getAllKnowledgeSlices();
			for (KnowledgeSlice rc:ruleComplexes) {
				if (idsToDelete.contains(rc.getId())) {
					rc.remove();
					//System.out.println("Deleted Rule: " + rc.getId());
				}
			}
			
			//System.out.println("Deleted old Rules in " + (System.nanoTime() - start) + "ns");
			
		}
	}

}
