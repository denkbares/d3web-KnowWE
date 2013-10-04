package de.knowwe.testcases.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import de.d3web.utils.Streams;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

public class DownloadCasesAction extends AbstractAction {

	public static final String PARAM_FILENAME = "filename";

	@Override
	public void execute(UserActionContext context) throws IOException {

		String path = context.getParameter("path");
		String file = context.getParameter("file");

		File caseFile = new File(path);

		context.setContentType("application/x-bin");
		context.setHeader("Content-Disposition", "attachment;filename=\"" + file + "\"");

		FileInputStream in = new FileInputStream(caseFile);
		OutputStream out = context.getOutputStream();
		Streams.streamAndClose(in, out);
	}

}