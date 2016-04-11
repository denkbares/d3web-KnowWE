package de.knowwe.core.compile;

import java.util.Collection;

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;

public class PackageRegistrationCompiler extends DefaultGlobalCompiler {

	private CompilerManager compilerManager;
	private PackageManager packageManager;
	private TerminologyManager terminologyManager;

	@Override
	public TerminologyManager getTerminologyManager() {
		return terminologyManager;
	}

	@Override
	public void init(CompilerManager compilerManager) {
		this.compilerManager = compilerManager;
		this.packageManager = new PackageManager(this);
		this.terminologyManager = new TerminologyManager();
	}

	@Override
	public CompilerManager getCompilerManager() {
		return this.compilerManager;
	}

	@Override
	public void compile(Collection<Section<?>> added, Collection<Section<?>> removed) {
		packageManager.clearChangedPackages();
		super.compile(added, removed);
	}

	public PackageManager getPackageManager() {
		return this.packageManager;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

	public static abstract class PackageRegistrationScript<T extends Type>
			implements CompileScript<PackageRegistrationCompiler, T>, DestroyScript<PackageRegistrationCompiler, T> {

		@Override
		public Class<PackageRegistrationCompiler> getCompilerClass() {
			return PackageRegistrationCompiler.class;
		}

	}

}
