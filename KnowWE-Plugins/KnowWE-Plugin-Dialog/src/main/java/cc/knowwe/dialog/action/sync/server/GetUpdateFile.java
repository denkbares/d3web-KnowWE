package cc.knowwe.dialog.action.sync.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import cc.knowwe.dialog.repository.VersionSet;

import com.denkbares.utils.Streams;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

public class GetUpdateFile extends AbstractAction {

	public static String PARAM_FILENAME = "file";
	public static String PARAM_CHECKSUM = "checksum";
	public static String PARAM_VERSION_ALIAS = "alias";

	@Override
	public void execute(UserActionContext context) throws IOException {
		String name = context.getParameter(PARAM_FILENAME);
		String checksum = context.getParameter(PARAM_CHECKSUM);
		String alias = context.getParameter(PARAM_VERSION_ALIAS);

		SyncServerContext syncContext = SyncServerContext.getInstance();
		String version = syncContext.getRepository().getVersion(alias);
		VersionSet versionSet = syncContext.getRepository().getVersionSet(version);
		File file = syncContext.getRepository().getPatch(name, checksum, versionSet).getFile();

		try (FileInputStream in = new FileInputStream(file)) {
			Streams.stream(in, context.getOutputStream());
		}
	}

}
