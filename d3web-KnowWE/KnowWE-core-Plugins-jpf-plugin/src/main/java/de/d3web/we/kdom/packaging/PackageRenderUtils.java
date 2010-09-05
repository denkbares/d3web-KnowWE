package de.d3web.we.kdom.packaging;

import java.util.Set;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;

public class PackageRenderUtils {

	public static KnowWEArticle checkArticlesCompiling(KnowWEArticle article, Section<?> s, StringBuilder string) {
		// check and handle, if the section is compiled in other articles
		KnowWEArticle compilingArticle = article;
		Set<String> compilingArticles = KnowWEEnvironment.getInstance().getPackageManager(
				article.getWeb()).getArticlesReferingTo(s);

		if (compilingArticles.size() > 1) {
			string.append("Articles compiling this Section: " + compilingArticles + "\n\n");
		}
		else if (compilingArticles.size() == 1) {
			String title = compilingArticles.iterator().next();
			if (!title.equals(compilingArticle.getTitle())) {
				string.append("Compiled and rendered for Article: " + title + "\n\n");
				compilingArticle = KnowWEEnvironment.getInstance().getArticle(article.getWeb(),
						title);
			}
		}
		else {
			string.append("This Section is not compiled in any article!\n\n");
		}
		return compilingArticle;
	}

	public static KnowWEArticle checkArticlesCompiling(KnowWEArticle article, Section<?> s) {
		KnowWEArticle compilingArticle = article;
		Set<String> compilingArticles = KnowWEEnvironment.getInstance().getPackageManager(
				article.getWeb()).getArticlesReferingTo(s);
		if (compilingArticles.size() == 1) {
			String title = compilingArticles.iterator().next();
			if (!title.equals(compilingArticle.getTitle())) {
				compilingArticle = KnowWEEnvironment.getInstance().getArticle(article.getWeb(),
						title);
			}
		}
		return compilingArticle;
	}

}
