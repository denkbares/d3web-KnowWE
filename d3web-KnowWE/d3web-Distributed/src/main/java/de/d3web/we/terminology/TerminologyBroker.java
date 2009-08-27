package de.d3web.we.terminology;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.qasets.QContainer;
import de.d3web.utilities.ISetMap;
import de.d3web.utilities.SetMap;
import de.d3web.we.alignment.AlignmentFilter;
import de.d3web.we.alignment.AlignmentUtilRepository;
import de.d3web.we.alignment.GlobalAlignment;
import de.d3web.we.alignment.LocalAlignment;
import de.d3web.we.alignment.aligner.LocalAligner;
import de.d3web.we.basic.IdentifiableInstance;
import de.d3web.we.basic.Information;
import de.d3web.we.basic.TerminologyType;
import de.d3web.we.terminology.global.GlobalTerminology;
import de.d3web.we.terminology.local.LocalTerminologyAccess;
import de.d3web.we.terminology.local.LocalTerminologyHandler;
import de.d3web.we.terminology.local.LocalTerminologyStorage;
import de.d3web.we.terminology.term.Term;
import de.d3web.we.terminology.term.TermFactory;

public class TerminologyBroker {

	private Map<TerminologyType, GlobalTerminology> globalTerminologies;

	private ISetMap<IdentifiableInstance, LocalAlignment> localAlignments;
	private ISetMap<IdentifiableInstance, GlobalAlignment> globalAlignments;
	private ISetMap<Term, GlobalAlignment> globalTermAlignments;
	
	
	public TerminologyBroker() {
		super();
		globalTerminologies = new HashMap<TerminologyType, GlobalTerminology>();
		localAlignments = new SetMap<IdentifiableInstance, LocalAlignment>();
		globalAlignments = new SetMap<IdentifiableInstance, GlobalAlignment>();
		globalTermAlignments = new SetMap<Term, GlobalAlignment>();
	}

	public Collection<LocalAlignment> getLocalAlignments() {
		return localAlignments.getAllValues();
	}
	
	public Collection<GlobalAlignment> getGlobalAlignments() {
		return globalAlignments.getAllValues();
	}
	
	public List<Term> getAlignedTerms(IdentifiableInstance ii) {
		List<Term> result = new ArrayList<Term>();
		Set<IdentifiableInstance> iis = new HashSet<IdentifiableInstance>();
		iis.add(ii);
		for (GlobalAlignment ga : globalAlignments.getAllValues(iis)) {
			result.add(ga.getTerm());
		}
		return result;
	}
	
	public List<IdentifiableInstance> getAlignedIdentifiableInstances(Term term) {
		return getAlignedIdentifiableInstances(term, null);
	}
	
	public List<IdentifiableInstance> getAlignedIdentifiableInstances(Term term, AlignmentFilter filter) {
		List<IdentifiableInstance> result = new ArrayList<IdentifiableInstance>();
		Collection<GlobalAlignment> gas = globalTermAlignments.get(term);
		if(gas == null) return result;
		for (GlobalAlignment ga : gas) {
			if(filter == null || filter.accepts(ga)) {
				result.add(ga.getObject());
			}
		}
		return result;
	}
	
	
	public ISetMap<IdentifiableInstance, IdentifiableInstance> getAlignmentMap(
			Information info) {
		IdentifiableInstance iio = info.getIdentifiableObjectInstance();
		Collection<IdentifiableInstance> iivs = info
				.getIdentifiableValueInstances();
		IdentifiableInstance DUMMY = new IdentifiableInstance("dummy", "dummy",
				"dummy");

		ISetMap<IdentifiableInstance, IdentifiableInstance> result = new SetMap<IdentifiableInstance, IdentifiableInstance>(
				false);
		result.addAll(getLocalIIOVMap(iio, iivs, DUMMY));
		result.addAll(getGlobalIIOVMap(iio, iivs, info, DUMMY));
		// [TODO]: find better solution for empty values.. dummy
		result.removeAll(result.keySet(), DUMMY);
		return result;
	}

	private ISetMap<IdentifiableInstance, IdentifiableInstance> getGlobalIIOVMap(
			IdentifiableInstance iio, Collection<IdentifiableInstance> iivs,
			Information info, IdentifiableInstance dummy) {
		ISetMap<IdentifiableInstance, IdentifiableInstance> result = new SetMap<IdentifiableInstance, IdentifiableInstance>(
				false);

		Collection<GlobalAlignment> iioAlignments = globalAlignments.get(iio);
		if (iioAlignments == null)
			return result;
		
		for (GlobalAlignment alignment : iioAlignments) {
			Term alignedTerm = alignment.getTerm();
			Collection<GlobalAlignment> termAlignments = globalTermAlignments.get(alignedTerm);
			for (GlobalAlignment each : termAlignments) {
				IdentifiableInstance alignedIIO = each.getObject();
				if(!alignedIIO.equals(iio) && !alignedIIO.isValued()) {
					result.add(alignedIIO, dummy);
				}
			}
		}

		for (IdentifiableInstance iiv : iivs) {
			Collection<GlobalAlignment> iivAlignments = globalAlignments.get(iiv);
			if (iivAlignments == null)
				continue;
			for (GlobalAlignment alignment : iivAlignments) {
				Term alignedTerm = alignment.getTerm();
				Collection<GlobalAlignment> termAlignments = globalTermAlignments.get(alignedTerm);
				for (GlobalAlignment each : termAlignments) {
					IdentifiableInstance dummyIIV = each.getObject();
					if(!dummyIIV.equals(iiv) && dummyIIV.isValued()) {
						IdentifiableInstance alignedIIV = new IdentifiableInstance(
							dummyIIV.getNamespace(), dummyIIV.getObjectId(),
							alignment.getAlignedValue(iiv, dummyIIV));
						IdentifiableInstance dummyIIO = new IdentifiableInstance(
								dummyIIV.getNamespace(), dummyIIV.getObjectId(), null);
						result.add(dummyIIO, alignedIIV);
					}
				}
			}
		}
		return result;
		
		
		
		
	}

	private ISetMap<IdentifiableInstance, IdentifiableInstance> getLocalIIOVMap(
			IdentifiableInstance iio, Collection<IdentifiableInstance> iivs,
			IdentifiableInstance dummy) {
		ISetMap<IdentifiableInstance, IdentifiableInstance> result = new SetMap<IdentifiableInstance, IdentifiableInstance>(
				false);

		Collection<LocalAlignment> iioAlignments = localAlignments.get(iio);
		if (iioAlignments == null)
			return result;
		for (LocalAlignment alignment : iioAlignments) {
			IdentifiableInstance alignedIIO = alignment.getAligned(iio);
			result.add(alignedIIO, dummy);
		}

		for (IdentifiableInstance iiv : iivs) {
			Collection<LocalAlignment> iivAlignments = localAlignments.get(iiv);
			if (iivAlignments == null)
				continue;
			for (LocalAlignment alignment : iivAlignments) {
				IdentifiableInstance dummyIIV = alignment.getAligned(iiv);
				IdentifiableInstance alignedIIV = new IdentifiableInstance(
						dummyIIV.getNamespace(), dummyIIV.getObjectId(),
						alignment.getAlignedValue(iiv, dummyIIV));
				IdentifiableInstance dummyIIO = new IdentifiableInstance(
						dummyIIV.getNamespace(), dummyIIV.getObjectId(), null);
				result.add(dummyIIO, alignedIIV);
			}
		}
		return result;
	}

	public GlobalTerminology getGlobalTerminology(TerminologyType type) {
		return globalTerminologies.get(type);
	}

	public ISetMap<Object, Term> addTerminology(TerminologyType type,
			LocalTerminologyAccess terminology, String idString,
			LocalTerminologyStorage storage) {
		GlobalTerminology gt = globalTerminologies.get(type);
		if (gt == null) {
			gt = new GlobalTerminology(type);
			globalTerminologies.put(type, gt);
		}
		ISetMap<Object, Term> result = gt.addTerminology(terminology, idString);
		return result;
	}

	public void removeTerminology(TerminologyType type,
			String idString, LocalTerminologyStorage storage) {
		
		//TODO remove terms in GT!!
		
		removeLocalAlignments(idString);
		removeGlobalAlignments(idString);
	}

	

	public Collection<LocalAlignment> alignLocal(LocalTerminologyAccess terminology, String idString,
			LocalTerminologyStorage storage) {
		Collection<LocalAlignment> result = new ArrayList<LocalAlignment>();
		Collection<LocalAligner> aligners = AlignmentUtilRepository
				.getInstance().getLocalAligners(terminology.getContext());
		if(aligners == null) return result;
		Collection<Class> efilters = new ArrayList<Class>();
		efilters.add(QContainer.class);
		efilters.add(Diagnosis.class);
		LocalTerminologyHandler localHandler = terminology.getHandler(
				new ArrayList(), efilters);
		for (Object object : localHandler) {
			for (LocalAligner aligner : aligners) {
				result.addAll(aligner.align(storage, object, idString));
			}
		}
		return result;
	}

	public Collection<GlobalAlignment> alignGlobal(LocalTerminologyAccess terminology, String id, LocalTerminologyStorage storage) {
		List<GlobalAlignment> result = new ArrayList<GlobalAlignment>();
		TermFactory factory = AlignmentUtilRepository.getInstance().getTermFactory(terminology.getContext());
		for (GlobalTerminology eachGT : globalTerminologies.values()) {
			for (Object eachObj : terminology.getHandler()) {
				result.addAll(factory.getAlignableTerms(eachObj, id, eachGT));
			}
		}
		Collections.sort(result);
		return result;
	}
		
	
	public void addGlobalAlignments(Collection<GlobalAlignment> alignments) {
		if(alignments == null) {
			return;
		}
		for (GlobalAlignment each : alignments) {
			if(each.getTerm() == null) {
				continue;
			}
			globalAlignments.add(each.getObject(), each);
			globalTermAlignments.add(each.getTerm(), each);
		}
	}
	
	public void addLocalAlignments(Collection<LocalAlignment> alignments) {
		for (LocalAlignment each : alignments) {
			localAlignments.add(each.getLocal(), each);
			localAlignments.add(each.getObject(), each);
		}
	}
	
	public void removeGlobalAlignment(GlobalAlignment alignment) {
		globalAlignments.removeAll(new ArrayList<IdentifiableInstance>(globalAlignments
				.keySet()), alignment);
		globalTermAlignments.removeAll(new ArrayList<Term>(globalTermAlignments
				.keySet()), alignment);
	}

	public void removeLocalAlignment(LocalAlignment alignment) {
		localAlignments.removeAll(new ArrayList<IdentifiableInstance>(localAlignments
				.keySet()), alignment);
	}

	//TODO inefficient
	private void removeLocalAlignments(String namespace) {
		for (IdentifiableInstance ii : new ArrayList<IdentifiableInstance>(
				localAlignments.keySet())) {
			if (ii.getNamespace().equals(namespace)) {
				for (LocalAlignment alignment : new ArrayList<LocalAlignment>(
						localAlignments.get(ii))) {
					removeLocalAlignment(alignment);
				}
			}
		}
	}

//	TODO inefficient
	private void removeGlobalAlignments(String namespace) {
		for (IdentifiableInstance ii : new ArrayList<IdentifiableInstance>(
				globalAlignments.keySet())) {
			if (ii.getNamespace().equals(namespace)) {
				for (GlobalAlignment alignment : new ArrayList<GlobalAlignment>(
						globalAlignments.get(ii))) {
					removeGlobalAlignment(alignment);
				}
			}
		}
	}
	
	public void setLocalAlignments(Collection<LocalAlignment> local) {
		localAlignments.clear();
		addLocalAlignments(local);
	}

	public void setGlobalAlignments(Collection<GlobalAlignment> global) {
		globalAlignments.clear(); 
		globalTermAlignments.clear();
		addGlobalAlignments(global);
	}

	public Collection<LocalAlignment> createLocalAlignments(LocalTerminologyStorage storage) {
		Collection<LocalAlignment> result = new ArrayList<LocalAlignment>();
		for (LocalTerminologyAccess each : storage.getTerminologies()) {
			result.addAll(alignLocal(each, storage.getID(each), storage));
		}
		return result;
	}

	public Collection<GlobalAlignment> createGlobalAlignments(LocalTerminologyStorage storage) {
		Collection<GlobalAlignment> result = new ArrayList<GlobalAlignment>();
		for (LocalTerminologyAccess each : storage.getTerminologies()) {
			result.addAll(alignGlobal(each, storage.getID(each), storage));
		}
		return result;
	}

	

	public void setGlobalTerminology(TerminologyType type, GlobalTerminology gt) {
		globalTerminologies.put(type, gt);		
	}

	public Collection<GlobalTerminology> getGlobalTerminologies() {
		return globalTerminologies.values();
	}


}
