package de.d3web.knowledgeExporter.txtWriters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import de.d3web.kernel.domainModel.KnowledgeSlice;
import de.d3web.kernel.psMethods.verbalizer.XclVerbalizer;
import de.d3web.kernel.psMethods.xclPattern.PSMethodXCL;
import de.d3web.kernel.psMethods.xclPattern.XCLModel;
import de.d3web.kernel.verbalizer.Verbalizer;
import de.d3web.kernel.verbalizer.VerbalizationManager.RenderingFormat;
import de.d3web.knowledgeExporter.KnowledgeManager;

public class XCLWriter extends TxtKnowledgeWriter {
	
	public XCLWriter(KnowledgeManager manager) {
		super(manager);
	}
	
	public String writeText() {
		StringBuffer text = new StringBuffer();
		
		Collection<KnowledgeSlice> xclRels = manager.getKB()
		.getAllKnowledgeSlicesFor(PSMethodXCL.class);	
		
        ArrayList<XCLModel> xclModels = new ArrayList<XCLModel>();
        for (KnowledgeSlice slice:xclRels) {
        	if (slice instanceof XCLModel) {
        		xclModels.add((XCLModel)slice);
        	}
        }
        
        int i = 0;
		for (XCLModel model:xclModels) {
            XclVerbalizer v = new XclVerbalizer();
    		HashMap<String, Object> parameter = new HashMap<String, Object>();
    		parameter.put(Verbalizer.LOCALE, KnowledgeManager.getLocale());
            text.append(v.verbalize(model, RenderingFormat.PLAIN_TEXT, parameter));
            if (i < xclModels.size() - 1) {
            	text.append("\n\n");
            }
            i++;
        }
		return text.toString();
	}
	

}
