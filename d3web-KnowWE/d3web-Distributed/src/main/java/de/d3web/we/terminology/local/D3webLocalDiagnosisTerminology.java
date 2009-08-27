package de.d3web.we.terminology.local;

import java.util.Collection;

import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.NamedObject;
import de.d3web.we.alignment.AlignmentUtilRepository;

public class D3webLocalDiagnosisTerminology implements LocalTerminologyAccess<NamedObject>{

private final KnowledgeBaseManagement kbm;
	
	public D3webLocalDiagnosisTerminology(KnowledgeBaseManagement kbm) {
		super();
		this.kbm = kbm;
	}

	public NamedObject getObject(String objectId, String valueId) {
		return kbm.findDiagnosis(objectId);
	}

	public LocalTerminologyHandler<NamedObject, NamedObject> getHandler(Collection<Class> include, Collection<Class> exclude) {
		LocalTerminologyHandler result = getHandler();
		result.setFilters(include);
		result.setExclusiveFilters(exclude);
		return result;
	}

	public LocalTerminologyHandler<NamedObject, NamedObject> getHandler() {
		Object root = kbm.getKnowledgeBase().getRootDiagnosis();
		return AlignmentUtilRepository.getInstance().getLocalTerminogyHandler(root);
	}

	public Class getContext() {
		return Diagnosis.class;
	}

}
