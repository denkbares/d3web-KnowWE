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

/**
 * 
 */
package de.d3web.we.kdom.contexts;

import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.kdom.Section;

/**
 * @author kazamatzuri
 *
 */
@Deprecated
public class KBMContext extends StringContext{
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

	@Override
	public boolean isValidForSection(Section s) {
		return true;
	}
}
