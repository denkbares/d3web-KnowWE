package de.d3web.we.hermes.logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.rendering.PageAppendHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class PageLoggerHandler implements PageAppendHandler {

	public boolean log = true;

	@Override
	public String getDataToAppend(String topic, String web,
			KnowWEUserContext user) {
		if (log) {
			try {
				BufferedWriter buffy = new BufferedWriter(new FileWriter(
						KnowWEEnvironment.getInstance().getKnowWEExtensionPath()
								+ "/tmp/Pagelogger.log", true));
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String uhrzeit = sdf.format(new Date(System.currentTimeMillis()));
				buffy.append(uhrzeit + ";" + user.getUsername() + ";" + user.getPage() + "\n");
				buffy.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	@Override
	public boolean isPre() {
		return false;
	}
}
