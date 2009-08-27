package de.d3web.we.core.blackboard;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.d3web.utilities.ISetMap;
import de.d3web.we.basic.Information;
import de.d3web.we.basic.SolutionState;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.terminology.term.Term;

/**
 * 
 * @author pkluegl
 *
 */
public interface Blackboard {

	Information inspect(Information info);
	
	void update(Information info);

	void clear(Broker broker);
	
	List<Information> getAllInformation();
	
	Collection<Information> getInferenceInformation(Term term);
	
	List<Information> getOriginalUserInformation();

	void setAllInformation(List<Information> infos);
	
	Map<Term, SolutionState> getGlobalSolutions();

	ISetMap<Term, Information> getAssumptions();
	
	void initializeClusterManagers(Broker broker);
	
	void removeInformation(String namespace);
	
}
