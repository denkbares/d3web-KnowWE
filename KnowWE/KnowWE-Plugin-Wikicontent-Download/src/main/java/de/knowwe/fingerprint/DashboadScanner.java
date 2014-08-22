package de.knowwe.fingerprint;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import de.d3web.utils.Streams;
import de.d3web.we.ci4ke.build.CIBuildManager;
import de.d3web.we.ci4ke.dashboard.CIDashboard;
import de.d3web.we.ci4ke.dashboard.CIDashboardManager;
import de.d3web.we.ci4ke.dashboard.type.CIDashboardType;
import de.d3web.we.ci4ke.hook.CIHookManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

public class DashboadScanner implements Scanner {

	@Override
	public void scan(Article article, File target) throws IOException {
		// execute builds and
		// wait for build thread to terminate
		CIHookManager.triggerHooks(article);
		CIBuildManager.awaitTermination();

		List<Section<CIDashboardType>> dashboardTypes = Sections.successors(
				article.getRootSection(), CIDashboardType.class);

		if (dashboardTypes.isEmpty()) return;

		PrintStream out = new PrintStream(target);
		try {
			for (Section<CIDashboardType> section : dashboardTypes) {
				CIDashboard dashboard = CIDashboardManager.getDashboard(section);
				out.printf("<!-- Dashboard %s -->\n", dashboard.getDashboardName());
				InputStream in = dashboard.getBuildAttachment().getInputStream();
				try {
					Streams.stream(in, out);
				}
				finally {
					in.close();
				}
				out.print("\n");
			}
		}
		finally {
			out.close();
		}
	}

	@Override
	public String getExtension() {
		return ".build";
	}

	@Override
	public String getItemName() {
		return "Build Result";
	}

	@Override
	public Diff compare(File file1, File file2) throws IOException {
		LineFilter filter = new SkipRegexLinesFilter(
				"<\\?xml .*\\?>",
				"<build xmlns=\"http://www.denkbares.com\".*>");
		return Fingerprint.compareTextFiles(file1, file2, filter);
	}

}
