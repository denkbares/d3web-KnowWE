package cc.knowwe.dialog.action.sync;

import java.io.IOException;
import java.io.Writer;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

public class CheckProgress extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		SyncClientContext syncContext = SyncClientContext.getInstance();
		int percent = (int) Math.floor(syncContext.getProgess() * 100);
		String state = syncContext.getSyncState().toString();
		Writer writer = context.getWriter();
		writer.append("<progress");
		writer.append(" percent='").append(String.valueOf(percent)).append("'");
		writer.append(" state='").append(state).append("'");
		writer.append(">");
		writer.append("</progress>");
		context.setContentType("text/xml");
	}

}
