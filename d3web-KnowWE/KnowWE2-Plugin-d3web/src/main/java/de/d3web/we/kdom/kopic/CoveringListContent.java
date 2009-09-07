package de.d3web.we.kdom.kopic;

import java.util.ArrayList;
import java.util.List;

import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.psMethods.xclPattern.XCLModel;
import de.d3web.kernel.psMethods.xclPattern.XCLRelationType;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Annotation.Finding;
import de.d3web.we.kdom.condition.ComplexFinding;
import de.d3web.we.kdom.condition.FindingToConditionBuilder;
import de.d3web.we.kdom.condition.NegatedFinding;
import de.d3web.we.kdom.xcl.XCLRelation;
import de.d3web.we.kdom.xcl.XCLRelationWeight;
import de.d3web.we.kdom.xcl.XCList;
import de.d3web.we.kdom.xml.XMLContent;
import de.d3web.we.knowRep.KnowledgeRepresentationHandler;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.terminology.D3webTerminologyHandler;

public class CoveringListContent extends XMLContent {
	
	@Override
	protected void init() {
		childrenTypes.add(new XCList());
	}
	
	KnowledgeBaseManagement kbm = null;
	
	@Override
	public void reviseSubtree(Section s, KnowledgeRepresentationManager tm, String web,
			KnowWEDomParseReport rep) {
		// TODO: Collect all XCLRelations
		// write them with XCLModel.insertRelation()into the knowledgeBase?!
		//  TEST: in Overview ArticleKnowledge there must
		// occur duplicates: Solved but needs further Testing!!!
		
		KnowledgeRepresentationHandler handler = tm.getHandler("d3web");
		if (handler instanceof D3webTerminologyHandler) {
			kbm = ((D3webTerminologyHandler) handler)
					.getKBM(s.getTopic());
		}
		
		// Analyse s (Has XCList-Children)
		ArrayList <Section> elements = new ArrayList<Section>(s.getChildren());
		for (Section sec : elements) {
			this.analyseXCList(sec);
		}

	}
	
	private Diagnosis currentdiag;
	
	/**
	 * Analyses a given XCList and writes
	 * it to the KnowledgeBase.
	 * @param kbm 
	 * 
	 * @param XCLList
	 */
	private void analyseXCList(Section xclList) {
		
		// Check if xclList is XCList
		if (xclList.getObjectType().getName().equals("XCList") && (kbm != null)) {
			
			// Get all children of xclList containing XCLHead/XCLBody/XCLTail and some other text
			ArrayList <Section> elements = new ArrayList <Section>(this.getXCLHeadBodyTail(xclList.getChildren()));
			
			if(elements.size() <= 1) {
				//invalid XCL-KDOM-tree
				return;
			}
			
			// Insert Solution into KnowledgeBase when Solution doesnt exist
			Section section = elements.get(0);
			currentdiag = kbm.findDiagnosis(section.getOriginalText());
			if (currentdiag == null) {
				currentdiag = kbm.createDiagnosis(section.getOriginalText(), kbm.getKnowledgeBase().getRootDiagnosis());
			}
			
			// Insert XCLRelations belonging to current Diagnosis
			ArrayList <Section> currentRels = new ArrayList <Section>(this.getXCLRelations(elements.get(1)));
			
			// insert every Relation into currentModel
			this.insertRelations(currentRels);
			
		}				
	}

	/**
	 * Inserts Relation into currentModel
	 * 
	 * @param currentRels
	 * @return
	 */
	private void insertRelations(ArrayList<Section> currentRels) {

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
			((XCLRelation)rel.getObjectType()).storeId(rel.getId(), kbRelId);
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
			if (rel.findChildOfType(Class.forName("de.d3web.we.kdom.xcl.XCLRelationWeight")) != null) {
				Section relWeight = rel.findChildOfType(Class.forName("de.d3web.we.kdom.xcl.XCLRelationWeight"));
				String weight = relWeight.getOriginalText();
				weight = weight.replaceAll("\\[", "");
				weight = weight.replaceAll("\\]", "");
				return Double.valueOf(weight);
			}
		} catch (ClassNotFoundException e7) {
			// Do Nothing
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
		ArrayList <Section> rels = new ArrayList <Section>();
		
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
			String name = children.get(i).getObjectType().getName();
			if ((name.equals("XCLHead")) || (name.equals("XCLBody")) || (name.equals("XCLTail"))) {
				continue;
			}
			children.remove(i--);
		}
		return children;
	}

}
