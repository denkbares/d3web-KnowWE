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

package de.d3web.we.terminology;

import java.util.ArrayList;
import java.util.Collection;

import de.d3web.utilities.ISetMap;
import de.d3web.we.alignment.Alignment;
import de.d3web.we.alignment.GlobalAlignment;
import de.d3web.we.alignment.LocalAlignment;
import de.d3web.we.basic.IdentifiableInstance;
import de.d3web.we.basic.Information;
import de.d3web.we.basic.InformationType;
import de.d3web.we.basic.TerminologyType;
import de.d3web.we.terminology.global.GlobalTerminology;
import de.d3web.we.terminology.local.LocalTerminologyAccess;
import de.d3web.we.terminology.local.LocalTerminologyStorage;
import de.d3web.we.terminology.term.Term;

public class TerminologyServer {

	public TerminologyServer() {
		super();
		storage = new LocalTerminologyStorage();
		broker = new TerminologyBroker();
	}

	private LocalTerminologyStorage storage;
	private TerminologyBroker broker;

	public ISetMap<Object, Term> addTerminology(String idString, TerminologyType type, LocalTerminologyAccess terminology) {
		// TODO will be registered somewhere else!!! see DPSE!
		// storage.register(idString, type, terminology);
		ISetMap<Object, Term> result = broker.addTerminology(type, terminology, idString, storage);
		return result;
	}

	public void removeTerminology(String idString, TerminologyType type) {
		broker.removeTerminology(type, idString, storage);
		storage.signoff(idString, type);
	}

	public GlobalTerminology getGlobalTerminology(TerminologyType type) {
		return broker.getGlobalTerminology(type);
	}

	public Collection<GlobalTerminology> getGlobalTerminologies() {
		return broker.getGlobalTerminologies();
	}

	public Collection<Information> getAlignedInformation(Information info) {
		ISetMap<IdentifiableInstance, IdentifiableInstance> map = broker.getAlignmentMap(info);
		InformationType infoType = info.getInformationType();
		if (InformationType.OriginalUserInformation.equals(infoType)) {
			infoType = InformationType.AlignedUserInformation;
		}
		return Information.toInformation(map, info, infoType);
	}

	public TerminologyBroker getBroker() {
		return broker;
	}

	public LocalTerminologyStorage getStorage() {
		return storage;
	}

	public void removeGlobalAlignment(GlobalAlignment alignment) {
		broker.removeGlobalAlignment(alignment);
	}

	public void removeLocalAlignment(LocalAlignment alignment) {
		broker.removeLocalAlignment(alignment);
	}

	public void setGlobalTerminology(TerminologyType type, GlobalTerminology gt) {
		broker.setGlobalTerminology(type, gt);
	}

	public Collection<Alignment> createAlignments() {
		Collection<Alignment> result = new ArrayList<Alignment>();
		result.addAll(createLocalAlignments());
		result.addAll(createGlobalAlignments());
		return result;
	}

	public void setGlobalAlignments(Collection<GlobalAlignment> global) {
		broker.setGlobalAlignments(global);
	}

	public void setLocalAlignments(Collection<LocalAlignment> local) {
		broker.setLocalAlignments(local);
	}

	public Collection<LocalAlignment> createLocalAlignments() {
		return broker.createLocalAlignments(storage);
	}

	public Collection<GlobalAlignment> createGlobalAlignments() {
		return broker.createGlobalAlignments(storage);
	}

	public Collection<LocalAlignment> getLocalAlignments() {
		return broker.getLocalAlignments();
	}

	public Collection<GlobalAlignment> getGlobalAlignments() {
		return broker.getGlobalAlignments();
	}

}
