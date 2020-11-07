package de.knowwe.core.compile;

import java.util.Collection;

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;

public class PackageRegistrationCompiler extends DefaultGlobalCompiler {

	private CompilerManager compilerManager;
	private final PackageManager packageManager;
	private final TerminologyManager terminologyManager;

	@Override
	public @org.jetbrains.annotations.NotNull TerminologyManager getTerminologyManager() {
		return terminologyManager;
	}

	public PackageRegistrationCompiler() {
		this.packageManager = new PackageManager();
		this.terminologyManager = new TerminologyManager();
	}

	@Override
	public void init(CompilerManager compilerManager) {
		this.compilerManager = compilerManager;
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

	public interface PackageRegistrationScript<T extends Type>
			extends CompileScript<PackageRegistrationCompiler, T>, DestroyScript<PackageRegistrationCompiler, T> {

		@Override
		default Class<PackageRegistrationCompiler> getCompilerClass() {
			return PackageRegistrationCompiler.class;
		}
	}
}
