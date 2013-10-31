package de.knowwe.core.compile;

import java.util.Collection;

import de.knowwe.core.Environment;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.parsing.Section;

public class PackageCompiler implements Compiler {

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
		// TODO implement compiling of section
	}

}
