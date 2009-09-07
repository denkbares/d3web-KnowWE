package de.d3web.we.action;

import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.psMethods.xclPattern.XCLRelation;
import de.d3web.kernel.verbalizer.ConditionVerbalizer;
import de.d3web.kernel.verbalizer.VerbalizationManager.RenderingFormat;

public class ExplainsRelationVerbalizer {
	
	public void verbalizeExplainsRelation(XCLRelation rel, StringBuffer text) {
		 ConditionVerbalizer v = new ConditionVerbalizer();
		 AbstractCondition cond = rel.getConditionedFinding();
         String weight = "1";
         Double d = rel.getWeight();
         
         if(d != null && d != 0) {
        	 weight = Double.toString(d);
         }
         text.append("  " + v.verbalize(cond, RenderingFormat.PLAIN_TEXT, null)
                 + " [" + weight + "]");
         if (text.toString().endsWith(" ")) {
         	text.replace(text.length() -1 , text.length(), "");
         }
         text.append(",");
	}

}
