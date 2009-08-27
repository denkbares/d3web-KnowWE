package de.d3web.kernel.dialogControl;


public abstract class ExternalClient {
	
	private int priority = 0;

	public ExternalClient() {
		super();
	}
	
	public ExternalClient(int priority) {
		super();
		this.priority = priority;
	}

	public abstract void init();
	
	public abstract void delegate(String targetNamespace, String id, boolean temporary, String comment);
	
	public abstract void delegateInstanly(String targetNamespace, String id, boolean temporary, String comment);

	public abstract void executeDelegation();

	public int getPriority() {
		return priority;
	}

	public void setPriority(int newPriority) {
		priority = newPriority;
	}

}
