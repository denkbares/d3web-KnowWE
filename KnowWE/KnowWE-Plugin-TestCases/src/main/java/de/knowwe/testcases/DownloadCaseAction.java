package de.knowwe.testcases;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import de.d3web.utils.Streams;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

public class DownloadCaseAction extends AbstractAction {

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

	public static Section<?> getPlayerSection(UserActionContext context, String playerid) throws IOException {
		Section<?> section = Sections.getSection(playerid);
		if (section == null || !(section.getFather().get() instanceof TestCasePlayerType)) {
			context.sendError(HttpServletResponse.SC_CONFLICT,
					"Unable to find TestCasePlayer with id '"
							+ playerid + "' , possibly because somebody else"
							+ " has edited the page.");
			return null;
		}
		return section;
	}

}