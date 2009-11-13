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

package de.d3web.we.kdom.xcl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.KnowledgeSlice;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.psMethods.xclPattern.PSMethodXCL;
import de.d3web.kernel.psMethods.xclPattern.XCLModel;
import de.d3web.kernel.psMethods.xclPattern.XCLRelationType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Annotation.Finding;
import de.d3web.we.kdom.condition.ComplexFinding;
import de.d3web.we.kdom.condition.FindingToConditionBuilder;
import de.d3web.we.kdom.condition.NegatedFinding;
import de.d3web.we.kdom.xml.XMLContent;
import de.d3web.we.terminology.D3webReviseSubTreeHandler;
import de.d3web.we.terminology.KnowledgeRecyclingObjectType;
import de.d3web.we.utils.KnowWEUtils;

public class CoveringListContent extends XMLContent implements KnowledgeRecyclingObjectType {
	
	public static final String KBID_KEY = "kbid";
	private Pattern p = Pattern.compile("\"");
	
	@Override
	protected void init() {
		this.childrenTypes.add(new XCList());
		subtreeHandler.add(new CoveringListContentSubTreeHandler());
	}
	
	public class CoveringListContentSubTreeHandler extends D3webReviseSubTreeHandler {

//		KnowledgeBaseManagement kbm = null;
//		String currentWeb = "";
//		private Diagnosis currentdiag;
		
		@Override
		public void reviseSubtree(Section s) {
			
			// Set currentWeb
			String currentWeb = s.getWeb();
			
			KnowledgeBaseManagement kbm = getKBM(s);
			
			if (kbm != null) {
				// Analyse s (Has XCList-Children)
				ArrayList <Section> elements = new ArrayList<Section>(s.getChildren());
				for (Section sec : elements) {
					this.analyseXCList(sec, kbm, currentWeb);
				}
			}

		}
		
		
		/**
		 * Analyses a given XCList and writes
		 * it to the KnowledgeBase.
		 * 
		 * @param kbm 
		 * @param currentweb 
		 * @param XCLList
		 */
		private void analyseXCList(Section xclList, KnowledgeBaseManagement kbm, String currentweb) {
			
			// Check if xclList is XCList
			if ((xclList.getObjectType() instanceof XCList) && (kbm != null)) {
				
				// Get all children of xclList containing XCLHead/XCLBody/XCLTail and some other text
				ArrayList <Section> elements = new ArrayList <Section>(this.getXCLHeadBodyTail(xclList.getChildren()));
				
				if(elements.size() <= 1) {
					//invalid XCL-KDOM-tree
					return;
				}
				
				// Insert Solution into KnowledgeBase when Solution doesnt exist
				Section head = elements.get(0);
				Diagnosis currentdiag = kbm.findDiagnosis(head.getOriginalText().replaceAll(p.toString(), "").trim());
				if (currentdiag == null) {
					currentdiag = kbm.createDiagnosis(head.getOriginalText().replaceAll(p.toString(), "").trim(), kbm.getKnowledgeBase().getRootDiagnosis());
				}
				
				// Insert XCLRelations belonging to current Diagnosis
				ArrayList <Section> currentRels = new ArrayList <Section>(this.getXCLRelations(elements.get(1)));
				
				// insert every Relation into currentModel
				this.insertRelations(currentRels, kbm, currentdiag, currentweb);
				
			}				
		}

		/**
		 * Inserts Relation into currentModel
		 * 
		 * @param currentRels
		 * @param kbm 
		 * @param currentdiag 
		 * @param currentWeb 
		 * @return
		 */
		private void insertRelations(ArrayList<Section> currentRels, KnowledgeBaseManagement kbm, Diagnosis currentdiag, String currentWeb) {

			double weight;
			XCLRelationType relationType;
			
			for (Section rel : currentRels) {
				// set weight and relationType
				weight = this.getWeight(rel);
				relationType = this.getRelationType(rel);
				
				// Get the Conditions
				AbstractCondition cond = FindingToConditionBuilder.analyseAnyRelation(rel, kbm);
				
				if (cond == null)
					continue;
					
				// Insert the Relation into the currentModel
				String kbRelId = XCLModel.insertXCLRelation(kbm.getKnowledgeBase(), cond, currentdiag, relationType, weight, rel.getId());
				KnowWEUtils.storeSectionInfo(currentWeb, rel.getTitle(), rel.getId(), KBID_KEY, kbRelId);
			}
			
		}

		/**
		 * Gets the RelationType from Relation
		 * 
		 * @param rel
		 * @return
		 */
		private XCLRelationType getRelationType(Section rel) {

			if (rel.findChildOfType(XCLRelationWeight.class) != null) {
				Section relWeight = rel.findChildOfType(XCLRelationWeight.class);
				String weightString = relWeight.getOriginalText();
				if (weightString.trim().equals("[--]")) {
					return XCLRelationType.contradicted;
				}
				else if (weightString.trim().equals("[!]")) {
					return XCLRelationType.requires;
				}
				else if (weightString.trim().equals("[++]")) {
					return XCLRelationType.sufficiently;
				}
				else {
					return XCLRelationType.explains;
				}			
			}

			return XCLRelationType.explains;
		}

		/**
		 * Gets the weight from a Relation.
		 * If it has none it returns 1.0.
		 * 
		 * @param rel
		 * @return
		 */
		private double getWeight(Section rel) {
			try {
				if (rel.findChildOfType(XCLRelationWeight.class) != null) {
					Section relWeight = rel.findChildOfType(XCLRelationWeight.class);
					String weight = relWeight.getOriginalText();
					weight = weight.replaceAll("\\[", "");
					weight = weight.replaceAll("\\]", "");
					return Double.valueOf(weight);
				}
			} catch (NumberFormatException e27) {
				// Do Nothing
			}
			return 1.0;
		}

		/**
		 * Gets all XCLRelations from a given XCLBody
		 * 
		 * @param section
		 * @return
		 */
		private List<Section> getXCLRelations(Section body) {
			ArrayList <Section> rels = new ArrayList <Section>(body.getChildren().size());
			
			// get the XCLRelation Sections
			// Sort out Relations only containing PlainText
			for (Section sec : body.getChildren()) {
				if (sec.getObjectType() instanceof XCLRelation) {
					if ((sec.findChildOfType(ComplexFinding.class) != null)
							|| (sec.findChildOfType(Finding.class) != null)
							|| (sec.findChildOfType(NegatedFinding.class) != null)) {
						rels.add(sec);
					}
				}
			}
			return rels;
		}

		/**
		 * Gets XCLHead, XCLBody and XCLTail from a given XCList type.
		 * 
		 * @param children
		 * @return
		 */
		private List<Section> getXCLHeadBodyTail(List<Section> children) {
			for(int i = 0; i < children.size(); i++) {
				KnowWEObjectType name = children.get(i).getObjectType();
				if ((name instanceof XCLHead) || (name instanceof XCLBody) || (name instanceof XCLTail)) {
					continue;
				}
				children.remove(i--);
			}
			return children;
		}

	}

	@Override
	public void cleanKnowledge(Section s, KnowledgeBaseManagement kbm) {
		
		if (kbm != null) {
			KnowWEArticle oldArt = s.getArticle().getOldArticle();
				
			// get all XCLContent of the old article
			List<Section> oldXCLCs = new ArrayList<Section>();
			oldArt.getSection().findSuccessorsOfType(this.getClass(), oldXCLCs);
			
			// store all Solutions of those old XCLs, that havn't got reused in the current article
			Set<String> xclsToDelete = new HashSet<String>();
			for (Section os:oldXCLCs) {
				if (!os.isReused()) {
					List<Section> heads = new ArrayList<Section>();
					os.findSuccessorsOfType(XCLHead.class, heads);
					for (Section head:heads) {
						xclsToDelete.add(head.getOriginalText().replaceAll(p.toString(), "").trim());
					}
				}
			}
			
			// delete the xcls from the KnowledgeBase
			Collection<KnowledgeSlice> slices = kbm.getKnowledgeBase().getAllKnowledgeSlicesFor(PSMethodXCL.class);
			for (KnowledgeSlice slice:slices) {
				if (xclsToDelete.contains(((XCLModel) slice).getSolution().getText())) {
					kbm.getKnowledgeBase().remove(slice);
					//System.out.println("Deletet XCL " + slice.getId());
				}
			}
		}
	}
}
