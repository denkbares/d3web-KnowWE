package de.d3web.kernel.dialogControl;

import de.d3web.kernel.XPSCase;


public class DistributedControllerFactory implements QASetManagerFactory {

	private ExternalProxy proxy;
	
	public DistributedControllerFactory(ExternalProxy proxy) {
		super();
		this.proxy = proxy;
	}
	
	public QASetManager createQASetManager(XPSCase theCase) {
		DialogController delegate = new MQDialogController(theCase);
		DistributedDialogController result = new DistributedDialogController(delegate, proxy);
		return result;
	}

}
