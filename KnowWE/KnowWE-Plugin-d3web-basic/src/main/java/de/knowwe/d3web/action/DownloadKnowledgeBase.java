package de.knowwe.d3web.action;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import de.d3web.we.action.AbstractAction;
import de.d3web.we.action.ActionContext;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.knowRep.KnowledgeRepresentationHandler;

public class DownloadKnowledgeBase extends AbstractAction {

	public static final String PARAM_FILENAME = "filename";

	@Override
	public void execute(ActionContext context) throws IOException {
		String filename = context.getParameter(PARAM_FILENAME);
		String topic = context.getParameter(KnowWEAttributes.TOPIC);
		String web = context.getParameter(KnowWEAttributes.WEB);

		KnowledgeRepresentationHandler handler = KnowWEEnvironment.getInstance()
				.getKnowledgeRepresentationManager(web).getHandler("d3web");
		URL home = handler.saveKnowledge(topic);
		InputStream in = home.openStream();

		context.setContentType("application/x-bin");
		context.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");
		OutputStream outs = context.getOutputStream();

		int bit;
		try {
			while ((bit = in.read()) >= 0) {
				outs.write(bit);
			}

		}
		catch (IOException ioe) {
			ioe.printStackTrace(System.out);
			context.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, String.valueOf(ioe));
		}
		finally {
			in.close();
		}

		outs.flush();
		outs.close();

	}

}
