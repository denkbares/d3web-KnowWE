package cc.knowwe.dialog.action.sync;

import java.io.IOException;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

public class CancelUpdate extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		SyncClientContext.getInstance().cancelSync();
	}

}
