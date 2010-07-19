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

package de.d3web.knowledgeExporter.txtWriters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.kernel.psmethods2.verbalizer.XclVerbalizer;
import de.d3web.kernel.verbalizer.Verbalizer;
import de.d3web.kernel.verbalizer.VerbalizationManager.RenderingFormat;
import de.d3web.knowledgeExporter.KnowledgeManager;
import de.d3web.xcl.XCLModel;
import de.d3web.xcl.inference.PSMethodXCL;

public class XCLWriter extends TxtKnowledgeWriter {
	
	public XCLWriter(KnowledgeManager manager) {
		super(manager);
	}
	
	@Override
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
        Collections.sort(xclModels);
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
