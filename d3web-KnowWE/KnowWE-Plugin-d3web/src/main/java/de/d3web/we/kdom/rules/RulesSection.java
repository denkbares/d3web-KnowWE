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

import java.util.HashSet;

import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.kopic.AbstractKopicSection;
import de.d3web.we.kdom.kopic.renderer.RuleSectionRenderer;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.terminology.D3webReviseSubTreeHandler;
import de.d3web.we.terminology.D3webTerminologyHandler;
import de.d3web.we.terminology.KnowledgeRecyclingObjectType;

public class RulesSection extends AbstractKopicSection implements KnowledgeRecyclingObjectType {

	public static final String TAG = "Rules-section";

	public RulesSection() {
		super(TAG);
	}

	@Override
	protected void init() {
		childrenTypes.add(new RulesSectionContent());
		setCustomRenderer(new RuleSectionRenderer());
		addReviseSubtreeHandler(new RulesSectionSubTreeHandler());
	}
	
	private class RulesSectionSubTreeHandler extends D3webReviseSubTreeHandler {

		@Override
		public void reviseSubtree(KnowWEArticle article, Section s) {
			useOldKnowledge(article, s);
		}

	}

	@Override
	public void cleanKnowledge(KnowWEArticle article, Section s, KnowledgeBaseManagement kbm) {
		HashSet<Class<? extends KnowWEObjectType>> types 
				= ((D3webTerminologyHandler) KnowledgeRepresentationManager.getInstance().getHandler("d3web"))
					.getCleanedTypes().get(s.getTitle());
		if (types != null && types.contains(Rule.class)) {
			return;
		}
		OldRulesEraser.deleteRules(article, s, kbm);
	}
}
