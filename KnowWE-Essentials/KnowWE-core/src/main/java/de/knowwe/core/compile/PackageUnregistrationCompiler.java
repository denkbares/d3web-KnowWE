package de.knowwe.core.compile;

import java.util.Collection;

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.CompilerMessage;

public class PackageUnregistrationCompiler extends DefaultGlobalCompiler {

	private CompilerManager compilerManager;
	private final PackageRegistrationCompiler registrationCompiler;

	@Override
	public @org.jetbrains.annotations.NotNull TerminologyManager getTerminologyManager() {
		return registrationCompiler.getTerminologyManager();
	}

	public PackageUnregistrationCompiler(PackageRegistrationCompiler registrationCompiler) {
		this.registrationCompiler = registrationCompiler;
	}

	public PackageRegistrationCompiler getRegistrationCompiler() {
		return registrationCompiler;
	}

	@Override
	public void init(CompilerManager compilerManager) {
		this.compilerManager = compilerManager;
	}

	@Override
	public CompilerManager getCompilerManager() {
		return this.compilerManager;
	}

	public PackageManager getPackageManager() {
		return registrationCompiler.getPackageManager();
	}

	@Override
	public void compile(Collection<Section<?>> added, Collection<Section<?>> removed) {
		super.compile(added, removed);
		getPackageManager().clearChangedPackages();
	}

	@FunctionalInterface
	public interface PackageUnregistrationScript<T extends Type>
			extends CompileScript<PackageUnregistrationCompiler, T>, DestroyScript<PackageUnregistrationCompiler, T> {

		@Override
		default void compile(PackageUnregistrationCompiler compiler, Section<T> section) throws CompilerMessage {
			// nothing to do here by default
		}

		@Override
		default Class<PackageUnregistrationCompiler> getCompilerClass() {
			return PackageUnregistrationCompiler.class;
		}
	}
}
