package de.knowwe.core.compile;

import java.util.Collection;

import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;

/**
 * Compiles all default stuff that happens globally and not in packages.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 13.11.2013
 */
public class DefaultGlobalCompiler implements TermCompiler, IncrementalCompiler {

	private TerminologyManager terminologyManager;
	private CompilerManager compilerManager;
	private SequentialScriptCompiler<DefaultGlobalCompiler> scriptCompiler;
	private SequentialScriptCompiler<DefaultGlobalCompiler> destroyScriptCompiler;

	public DefaultGlobalCompiler() {
		this.scriptCompiler = new SequentialScriptCompiler<>(this);
		this.destroyScriptCompiler = new SequentialScriptCompiler<>(this);
	}

	@Override
	public @org.jetbrains.annotations.NotNull TerminologyManager getTerminologyManager() {
		return this.terminologyManager;
	}

	@Override
	public void init(CompilerManager compilerManager) {
		this.compilerManager = compilerManager;
		this.terminologyManager = new TerminologyManager();
	}

	@Override
	public void destroy() {
		// nothing to do
	}

	@Override
	public void compile(Collection<Section<?>> added, Collection<Section<?>> removed) {

		// call destroy for the incremental scripts
		for (Section<?> section : removed) {
			destroyScriptCompiler.addSubtree(section);
		}
		destroyScriptCompiler.destroy();

		// assemble the compile scripts, and compile them
		getCompilerManager().setCurrentCompilePriority(this, Priority.PREPARE);
		for (Section<?> section : added) {
			scriptCompiler.addSubtree(section);
		}
		scriptCompiler.compile();

		// set new script compilers to prepare the next compile run
		this.scriptCompiler = new SequentialScriptCompiler<>(this);
		this.destroyScriptCompiler = new SequentialScriptCompiler<>(this);
	}

	@Override
	public CompilerManager getCompilerManager() {
		return this.compilerManager;
	}

	@Override
	public boolean addSectionToDestroy(Section<?> section, Class<?>... scriptFilter) {
		return destroyScriptCompiler.addSection(section, scriptFilter);
	}

	@Override
	public boolean addSectionToCompile(Section<?> section, Class<?>... scriptFilter) {
		return scriptCompiler.addSection(section, scriptFilter);
	}

	@Override
	public Sections<?> addSubtreeToDestroy(Section<?> section, Class<?>... scriptFilter) {
		return destroyScriptCompiler.addSubtree(section, scriptFilter);
	}

	@Override
	public Sections<?> addSubtreeToCompile(Section<?> section, Class<?>... scriptFilter) {
		return scriptCompiler.addSubtree(section, scriptFilter);
	}

	public static abstract class DefaultGlobalScript<T extends Type>
			implements CompileScript<DefaultGlobalCompiler, T>, DestroyScript<DefaultGlobalCompiler, T> {

		@Override
		public Class<DefaultGlobalCompiler> getCompilerClass() {
			return DefaultGlobalCompiler.class;
		}

		@Override
		public void destroy(DefaultGlobalCompiler compiler, Section<T> section) {
			// nothing to do for now...
		}
	}

	public static abstract class DefaultGlobalHandler<T extends Type> extends DefaultGlobalScript<T> {

		@Override
		public Class<DefaultGlobalCompiler> getCompilerClass() {
			return DefaultGlobalCompiler.class;
		}

		public abstract Collection<Message> create(DefaultGlobalCompiler compiler, Section<T> section) throws CompilerMessage;

		@Override
		public void compile(DefaultGlobalCompiler compiler, Section<T> section) throws CompilerMessage {
			Messages.storeMessages(section, getClass(), create(compiler, section));
		}

		@Override
		public void destroy(DefaultGlobalCompiler compiler, Section<T> section) {
			// nothing to do for now...
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

	@Override
	public boolean isCompiling(Section<?> section) {
		return true;
	}
}
