package de.d3web.we.terminology.local;

import java.util.Collection;

import de.d3web.kernel.domainModel.IDObject;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.QASet;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.we.alignment.AlignmentUtilRepository;

public class D3webLocalSymptomTerminology implements LocalTerminologyAccess<IDObject> {

	private final KnowledgeBaseManagement kbm;
	
	public D3webLocalSymptomTerminology(KnowledgeBaseManagement kbm) {
		super();
		this.kbm = kbm;
	}

	public IDObject getObject(String objectId, String valueId) {
		IDObject result = null;
		result = kbm.findQContainer(objectId);
		if(result == null) {
			result = kbm.findQuestion(objectId);
		}
		if(valueId == null) {
			return result;
		} else {
			if(result instanceof Question) {
				return kbm.findAnswer((Question) result, valueId);
			}
		}
		return result;
	}
	public LocalTerminologyHandler<IDObject, IDObject> getHandler(Collection<Class> include, Collection<Class> exclude) {
		LocalTerminologyHandler result = getHandler();
		result.setFilters(include);
		result.setExclusiveFilters(exclude);
		return result;
	}

	public LocalTerminologyHandler<IDObject, IDObject> getHandler() {
		Object root = kbm.getKnowledgeBase().getRootQASet();
		return AlignmentUtilRepository.getInstance().getLocalTerminogyHandler(root);
	}

	public Class getContext() {
		return QASet.class;
	}
	
}
