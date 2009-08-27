package de.d3web.knowledgeExporter.xlsWriters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import de.d3web.kernel.domainModel.KnowledgeSlice;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.domainModel.ruleCondition.TerminalCondition;
import de.d3web.kernel.psMethods.xclPattern.PSMethodXCL;
import de.d3web.kernel.psMethods.xclPattern.XCLModel;
import de.d3web.kernel.psMethods.xclPattern.XCLRelation;
import de.d3web.kernel.psMethods.xclPattern.XCLRelationType;
import de.d3web.kernel.verbalizer.TerminalCondVerbalization;
import de.d3web.knowledgeExporter.KnowledgeManager;

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
        for (KnowledgeSlice slice:xclRels) {
        	if (slice instanceof XCLModel) {
        		xclModels.add((XCLModel)slice);
        		diagnosisList.add(((XCLModel)slice).getSolution().getText());
        	}
        }
        
        ArrayList<XCLRelationType> types = new ArrayList<XCLRelationType>();
        types.add(XCLRelationType.requires);
        types.add(XCLRelationType.sufficiently);
        types.add(XCLRelationType.explains);
        types.add(XCLRelationType.contradicted);
        
        for (XCLModel model:xclModels) {
    		Map<XCLRelationType, Collection<XCLRelation>> relationMap = model.getTypedRelations();
            for (XCLRelationType type : types) {
                Collection<XCLRelation> relationsCol = relationMap.get(type);
                for (XCLRelation rel:relationsCol) {
                    String weight = "";
                    if(type == XCLRelationType.explains) {
                    	weight = trimNum(new Double(rel.getWeight()).toString()); 
                    } else if (type == XCLRelationType.contradicted) {
                    	weight = "--";
                    } else if (type == XCLRelationType.requires) {
                    	weight = "!";
                    } else if (type == XCLRelationType.sufficiently) {
                    	weight = "++";
                    }
                    
                    AbstractCondition cond = rel.getConditionedFinding();
                    
                    if (cond instanceof TerminalCondition) {
                    	TerminalCondVerbalization tCondVerb = (TerminalCondVerbalization) 
        					verbalizer.createConditionVerbalization(cond);
                    	String a = tCondVerb.getAnswer();
        				String q = tCondVerb.getQuestion();
                    	addEntry(model.getSolution().getText(), q, a, weight);
                    }
                }
            }
        }

        splitDiagnosisList();
		
	}
}