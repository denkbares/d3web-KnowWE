package de.knowwe.core.compile;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.knowwe.core.Environment;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;

public class DefaultCompiler implements Compiler {

	private CompilerManager compilerManager;

	@Override
	public TerminologyManager getTerminologyManager() {
		// TODO: use own terminology manager instance later on
		return Environment.getInstance().getTerminologyManager(compilerManager.getWeb(), null);
	}

	@Override
	public void init(CompilerManager compilerManager) {
		this.compilerManager = compilerManager;
	}

	@Override
	public void compile(Collection<Section<?>> added, Collection<Section<?>> removed) {
		Set<Article> articles = new HashSet<Article>();
		for (Section<?> section : added) {
			articles.add(section.getArticle());
		}
		for (Section<?> section : removed) {
			articles.add(section.getArticle());
		}
		// and compile them
		for (Article article : articles) {
			article.recompile();
		}
	}

}
