package de.knowwe.d3web.compile;

import de.knowwe.core.compile.CompileScript;
import de.knowwe.core.kdom.Type;

public abstract class KnowledgeBaseCompileScript<T extends Type> implements CompileScript<KnowledgeBaseCompiler, T> {

	@Override
	public final Class<KnowledgeBaseCompiler> getCompilerClass() {
		return KnowledgeBaseCompiler.class;
	}
}
