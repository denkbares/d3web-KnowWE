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

package de.d3web.we.kdom.contexts;

import org.openrdf.model.URI;

import de.d3web.we.kdom.Section;
import de.d3web.we.module.semantic.owl.UpperOntology;

public class SolutionContext extends StringContext {
    	private URI soluri;
	public final static String CID="SOLUTIONCONTEXT";
	
	public void setSolution(String sol){
		attributes.put("solution", sol);
	}

	public void setSolutionURI(URI solutionuri){
	    soluri=solutionuri;
	}
	public String getSolution() {
		return attributes.get("solution");
	}
	
	public URI getSolutionURI(){
	    if (soluri==null){
		UpperOntology uo=UpperOntology.getInstance();
		soluri=uo.getHelper().createlocalURI(getSolution());
	    }
	    return soluri;
	}

	@Override
	public String getCID() {		
		return CID;
	}

	@Override
	public boolean isValidForSection(Section s) {
		return true;
	}
}
