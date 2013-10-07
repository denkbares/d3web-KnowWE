package de.knowwe.core.utils.progress;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import de.d3web.strings.Strings;
import de.d3web.utils.Streams;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

public class DownloadFileAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String fileParameter = context.getParameter("file");
		String filePath = Strings.decodeURL(fileParameter);
		String nameParameter = context.getParameter("name");
		String name = Strings.decodeURL(nameParameter);

		File file = new File(filePath);
		try {
			context.setContentType("application/x-bin");
			context.setHeader("Content-Disposition", "attachment;filename=\"" + name + "\"");

			FileInputStream in = new FileInputStream(file);
			OutputStream out = context.getOutputStream();
			Streams.streamAndClose(in, out);
		}
		finally {
			file.delete();
			file.deleteOnExit();
		}
	}

}