/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.d3web.kernel.psMethods.delegate;

import java.util.Collection;

import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.inference.MethodKind;
import de.d3web.core.inference.PSMethodAdapter;
import de.d3web.core.inference.PropagationEntry;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.RuleSet;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.session.Session;
import de.d3web.core.session.blackboard.Fact;
import de.d3web.core.session.blackboard.Facts;

public class PSMethodDelegate extends PSMethodAdapter {

	private static PSMethodDelegate instance = new PSMethodDelegate();

	public static PSMethodDelegate getInstance() {
		return instance;
	}

	@Override
	public void propagate(Session session, Collection<PropagationEntry> changes) {

		for (PropagationEntry propagationEntry : changes) {

			KnowledgeSlice slices = ((NamedObject) propagationEntry.getObject()).getKnowledge(
					this.getClass(), MethodKind.FORWARD);

			if (slices == null) return;
			RuleSet rs = (RuleSet) slices;
			for (Rule slice : rs.getRules()) {
				slice.check(session);
			}
		}
	}

	@Override
	public Fact mergeFacts(Fact[] facts) {
		// PSMethodDelegate does not provide own facts
		return Facts.mergeError(facts);
	}

}
