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
import java.util.Iterator;
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
		public void reviseSubtree(KnowWEArticle article, Section s) {
			
			// Set currentWeb
			String currentWeb = s.getWeb();
			
			KnowledgeBaseManagement kbm = getKBM(article, s);
			
			if (kbm != null) {
				// Analyse s (Has XCList-Children)
				ArrayList <Section> elements = new ArrayList<Section>(s.getChildren());
				for (Section sec : elements) {
					this.analyseXCList(article, sec, kbm, currentWeb);
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
		private void analyseXCList(KnowWEArticle article, Section xclList, KnowledgeBaseManagement kbm, String currentweb) {
			
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
				this.insertRelations(article, currentRels, kbm, currentdiag, currentweb);
				
				//tail with thresholds
				if (elements.size() == 3) {
					Section tail = elements.get(2);
					if (tail.getObjectType() instanceof XCLTail)
						setThresholds(kbm, currentdiag, tail);
						
				}
				
				
			}				
		}


		private void setThresholds(KnowledgeBaseManagement kbm,
				Diagnosis currentdiag, Section tail) {
			
			List knowledge = (List) kbm.getKnowledgeBase().getKnowledge(PSMethodXCL.class, XCLModel.XCLMODEL);
			
			if (knowledge == null)
				return;
			
			Iterator iterator = knowledge.iterator();
			
			while (iterator.hasNext()) {
				XCLModel model = (XCLModel) iterator.next();
				if (model.getSolution().equals(currentdiag)) {
					
					double suggestedThreshold = XCLTail.getSuggestedThreshold(tail);
					if (suggestedThreshold != -1)
						model.setSuggestedThreshold(suggestedThreshold);
					
					double establishedThreshold = XCLTail.getEstablisehdThreshold(tail);
					if (establishedThreshold != -1)
						model.setEstablishedThreshold(establishedThreshold);

					double minsupport = XCLTail.getMinSupport(tail);
					if (minsupport != -1)
						model.setMinSupport(minsupport);
					
					
				}
					
					
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
		private void insertRelations(KnowWEArticle article, ArrayList<Section> currentRels, 
				KnowledgeBaseManagement kbm, Diagnosis currentdiag, String currentWeb) {

			
			for (Section rel : currentRels) {
				double weight = this.getWeight(rel);
				XCLRelationType relationType = this.getRelationType(rel);
				// Get the Conditions
				AbstractCondition cond = FindingToConditionBuilder.analyseAnyRelation(article, rel, kbm);
				
				if (cond == null)
					continue;
					
				// Insert the Relation into the currentModel
				String kbRelId = XCLModel.insertXCLRelation(kbm.getKnowledgeBase(), cond, currentdiag, relationType, weight, rel.getId());
				KnowWEUtils.storeSectionInfo(currentWeb, article.getTitle(), rel.getId(), KBID_KEY, kbRelId);
				
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
			 List<Section> children = body.getChildren();
			for (Section sec : children) {
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
	public void cleanKnowledge(KnowWEArticle article, KnowledgeBaseManagement kbm) {
		
		if (kbm != null) {
//			KnowWEArticle lastArt = article.getLastVersionOfArticle();
//				
//			// get all XCLContent of the old article
//			List<Section> oldXCLCs = new ArrayList<Section>();
//			lastArt.getSection().findSuccessorsOfType(this.getClass(), oldXCLCs);
//			
//			// store all Solutions of those old XCLs, that havn't got reused in the current article
//			Set<String> xclsToDelete = new HashSet<String>();
//			for (Section os:oldXCLCs) {
//				if (!os.isReusedBy(article.getTitle())) {
//					List<Section> heads = new ArrayList<Section>();
//					os.findSuccessorsOfType(XCLHead.class, heads);
//					for (Section head:heads) {
//						xclsToDelete.add(head.getOriginalText().replaceAll(p.toString(), "").trim());
//					}
//				}
//			}
//			
//			// delete the xcls from the KnowledgeBase
//			Collection<KnowledgeSlice> slices = kbm.getKnowledgeBase().getAllKnowledgeSlicesFor(PSMethodXCL.class);
//			for (KnowledgeSlice slice:slices) {
//				if (xclsToDelete.contains(((XCLModel) slice).getSolution().getText())) {
//					kbm.getKnowledgeBase().remove(slice);
//					//System.out.println("Deleted XCL " + slice.getId());
//				}
//			}
			
			long startTime = System.currentTimeMillis();
			
			List<Section> newXCLs = new ArrayList<Section>();
			article.getSection().findSuccessorsOfType(XCLRelation.class, newXCLs);
			
			Set<String> kbIDs = new HashSet<String>();
			for (Section xcl:newXCLs) {
				kbIDs.add((String) KnowWEUtils.getStoredObject(article.getWeb(), article
						.getTitle(), xcl.getId(), CoveringListContent.KBID_KEY));
			}
			// delete the xcls from the KnowledgeBase
			Collection<KnowledgeSlice> slices = kbm.getKnowledgeBase().getAllKnowledgeSlicesFor(PSMethodXCL.class);
			for (KnowledgeSlice slice:slices) {
				XCLModel model = (XCLModel) slice;
				for (String id:model.getAllRelations().keySet()) {
					if (!kbIDs.contains(id)) {
						model.removeRelation(id);
					}
				}
				if (model.getAllRelations().isEmpty()) {
					kbm.getKnowledgeBase().remove(slice);
				}
				
			}
			System.out.println("Cleaned XCLs in " + (System.currentTimeMillis() - startTime) + "ms");
		}
	}
}
