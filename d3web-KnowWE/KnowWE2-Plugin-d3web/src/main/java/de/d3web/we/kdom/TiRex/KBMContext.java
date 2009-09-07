/**
 * 
 */
package de.d3web.we.kdom.TiRex;

import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.we.kdom.contexts.Context;

/**
 * @author kazamatzuri
 *
 */
public class KBMContext extends Context{
    public final static String CID="KBMCONTEXT";
    private KnowledgeBaseManagement kbm;
    /* (non-Javadoc)
     * @see de.d3web.we.dom.contexts.Context#getCID()
     */
    @Override
    public String getCID() {	// TODO Auto-generated method stub
	return CID;
    }
    
    public void setKBM(KnowledgeBaseManagement kbm){
	this.kbm=kbm;
    }

    public KnowledgeBaseManagement getKBM(){
	return kbm;
    }
}
