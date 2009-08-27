package de.d3web.we.core.dialog;

import java.util.List;

import de.d3web.we.core.knowledgeService.KnowledgeServiceSession;

public interface DialogControl {

	void delegate(KnowledgeServiceSession kss, KnowledgeServiceSession reason, boolean userIndicated, boolean instantly, String comment);
	
	void finished(KnowledgeServiceSession kss);
	
	List<Dialog> getInstantIndicatedDialogs();
	
	List<Dialog> getIndicatedDialogs();
	
	List<Dialog> getHistory();
	
	void cancelDelegate(KnowledgeServiceSession kss);
	
	void clear();
	
	public boolean isUserInterventionNeeded();
	
	boolean isDialogSwitchNeeded();
	
	Dialog getNextActiveDialog();
	
	Dialog showNextActiveDialog();
	
}
