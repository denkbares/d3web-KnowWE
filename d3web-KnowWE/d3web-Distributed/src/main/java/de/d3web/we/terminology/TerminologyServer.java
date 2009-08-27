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
		//storage.register(idString, type, terminology);
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
		if(InformationType.OriginalUserInformation.equals(infoType)) {
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
