package de.d3web.we.core.packaging;

import java.util.Set;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEUtils;

public class PackageRenderUtils {

	public static KnowWEArticle checkArticlesCompiling(KnowWEArticle article, Section<?> s, StringBuilder string) {
		// check and handle, if the section is compiled in other articles
		KnowWEArticle compilingArticle = article;
		if (article.getTitle().equals(s.getTitle())) {
			Set<String> compilingArticles = KnowWEEnvironment.getInstance().getPackageManager(
					article.getWeb()).getArticlesReferingTo(s);

			if (compilingArticles.size() > 1) {
				// string.append("Articles compiling this Section: " +
				// compilingArticles);
				string.append(KnowWEUtils.maskHTML("<span class=\"info\">Articles compiling this Section: "
						+ compilingArticles + "</span><p/>"));
			}
			else if (compilingArticles.size() == 1) {
				String title = compilingArticles.iterator().next();
				if (!title.equals(compilingArticle.getTitle())) {
					// string.append("Compiled and rendered for Article: " +
					// title + "\n\n");
					compilingArticle = KnowWEEnvironment.getInstance().getArticle(article.getWeb(),
							title);
				}
			}
			else {
				// string.append("This Section is not compiled in any article!\n\n");
				string.append(KnowWEUtils.maskHTML("<span class=\"warning\">This Section is not compiled in any article!</span><p/>"));
			}
		}
		return compilingArticle;
	}

	public static KnowWEArticle checkArticlesCompiling(KnowWEArticle article, Section<?> s) {
		KnowWEArticle compilingArticle = article;
		if (article.getTitle().equals(s.getTitle())) {
			Set<String> compilingArticles = KnowWEEnvironment.getInstance().getPackageManager(
					article.getWeb()).getArticlesReferingTo(s);
			if (compilingArticles.size() == 1) {
				String title = compilingArticles.iterator().next();
				if (!title.equals(compilingArticle.getTitle())) {
					compilingArticle = KnowWEEnvironment.getInstance().getArticle(article.getWeb(),
							title);
				}
			}
		}
		return compilingArticle;
	}

}
