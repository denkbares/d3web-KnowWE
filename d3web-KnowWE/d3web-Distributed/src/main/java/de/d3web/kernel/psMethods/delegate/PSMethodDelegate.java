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

package de.d3web.kernel.psMethods.delegate;

import java.util.Collection;
import java.util.List;

import de.d3web.kernel.XPSCase;
import de.d3web.kernel.domainModel.KnowledgeSlice;
import de.d3web.kernel.domainModel.RuleComplex;
import de.d3web.kernel.psMethods.PSMethodAdapter;
import de.d3web.kernel.psMethods.PropagationEntry;

public class PSMethodDelegate extends PSMethodAdapter {
	private static PSMethodDelegate instance = new PSMethodDelegate();

	public static PSMethodDelegate getInstance() {
		return instance;
	}


	@Override
	public void propagate(XPSCase theCase, Collection<PropagationEntry> changes) {
		
		for (PropagationEntry propagationEntry : changes) {
			
			List<? extends KnowledgeSlice> slices = propagationEntry.getObject().getKnowledge(this.getClass());
			
			if(slices == null) 
				return;
			
			for (KnowledgeSlice slice : slices) {
				((RuleComplex) slice).check(theCase);
			}
		}
	}
}
