package de.knowwe.d3web.compile;

import java.util.Collection;

import de.d3web.core.knowledge.KnowledgeBase;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.CompilerManager;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;

/**
 * Class for compiling knowledge bases defined by a %%KnowledgeBase markup.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 31.10.2013
 */
public class KnowledgeBaseCompiler implements Compiler {

	private final Article compilingArticle;
	private KnowledgeBase knowledgeBase;

	/**
	 * Creates a new KnowledgeBaseCompiler for the knowledge base defined in a
	 * specific compiling article.
	 * 
	 * TODO: this constructor shall be replaced later on by constructing the
	 * compiler without the article, directly specifying the packages to
	 * compile.
	 * 
	 * @param compilingArticle
	 */
	public KnowledgeBaseCompiler(Article compilingArticle) {
		this.compilingArticle = compilingArticle;
	}

	@Override
	public TerminologyManager getTerminologyManager() {
		// TODO: replace by its own term manager instance
		return Environment.getInstance().getTerminologyManager(compilingArticle);
	}

	public KnowledgeBase getKnowledgeBase() {
		return knowledgeBase;
	}

	@Override
	public void init(CompilerManager compilerManager) {
	}

	@Override
	public void compile(Collection<Section<?>> added, Collection<Section<?>> removed) {

	}
}
