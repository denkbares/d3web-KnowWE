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

package de.d3web.knowledgeExporter.xlsWriters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.inference.condition.TerminalCondition;
import de.d3web.kernel.verbalizer.TerminalCondVerbalization;
import de.d3web.knowledgeExporter.KnowledgeManager;
import de.d3web.xcl.XCLModel;
import de.d3web.xcl.XCLRelation;
import de.d3web.xcl.XCLRelationType;
import de.d3web.xcl.inference.PSMethodXCL;

public class SetCoveringTableWriter extends QDTableWriter {

	private ArrayList<XCLModel> xclModels;

	public SetCoveringTableWriter(KnowledgeManager manager) {
		super(manager);
	}

	@Override
	protected void getKnowledge() {
		Collection<KnowledgeSlice> xclRels = manager.getKB()
				.getAllKnowledgeSlicesFor(PSMethodXCL.class);

		xclModels = new ArrayList<XCLModel>();
		for (KnowledgeSlice slice : xclRels) {
			if (slice instanceof XCLModel) {
				xclModels.add((XCLModel) slice);
				diagnosisList.add(((XCLModel) slice).getSolution().getName());
			}
		}

		ArrayList<XCLRelationType> types = new ArrayList<XCLRelationType>();
		types.add(XCLRelationType.requires);
		types.add(XCLRelationType.sufficiently);
		types.add(XCLRelationType.explains);
		types.add(XCLRelationType.contradicted);

		for (XCLModel model : xclModels) {
			Map<XCLRelationType, Collection<XCLRelation>> relationMap = model.getTypedRelations();
			for (XCLRelationType type : types) {
				Collection<XCLRelation> relationsCol = relationMap.get(type);
				for (XCLRelation rel : relationsCol) {
					String weight = "";
					if (type == XCLRelationType.explains) {
						weight = trimNum(new Double(rel.getWeight()).toString());
					}
					else if (type == XCLRelationType.contradicted) {
						weight = "--";
					}
					else if (type == XCLRelationType.requires) {
						weight = "!";
					}
					else if (type == XCLRelationType.sufficiently) {
						weight = "++";
					}

					Condition cond = rel.getConditionedFinding();

					if (cond instanceof TerminalCondition) {
						TerminalCondVerbalization tCondVerb = (TerminalCondVerbalization)
								verbalizer.createConditionVerbalization(cond);
						String a = tCondVerb.getAnswer();
						String q = tCondVerb.getQuestion();
						addEntry(model.getSolution().getName(), q, a, weight);
					}
				}
			}
		}

		splitSolutionList();

	}
}