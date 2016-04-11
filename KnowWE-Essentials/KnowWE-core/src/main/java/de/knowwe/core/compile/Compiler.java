package de.knowwe.core.compile;

import java.util.Collection;

import de.knowwe.core.kdom.parsing.Section;

/**
 * Interface that defines a compiler that compiles a special artifact instance.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 30.10.2013
 */
public interface Compiler {

	CompilerManager getCompilerManager();

	/**
	 * Returns whether the given section is generally in the responsibility of
	 * this {@link Compiler}. We don't check if there is a {@link CompileScript}
	 * for the given {@link Section} for the given {@link Compiler}, we only
	 * check if this compiler would compile the Section, if there were
	 * {@link CompileScript}.<br/>
	 * Global Compilers will mostly return true here, {@link PackageCompiler}s
	 * will only return true, if the Section is part of a package they are
	 * compiling.
	 *
	 * @param section the section we want to check.
	 * @return whether this Compiler compiles this section or would compile it,
	 * if it had a CompileScript
	 * @created 13.12.2013
	 */
	boolean isCompiling(Section<?> section);

	/**
	 * This method is called once to initialize the compiler right after it has
	 * been added to the specified compiler manager.
	 *
	 * @param compilerManager the {@link CompilerManager} this compiler has been
	 *                        added to
	 * @created 31.10.2013
	 */
	void init(CompilerManager compilerManager);

	/**
	 * When this method is called, the compiler shall compile its artifact. The
	 * specified changes sections may be used to optimize the compilation as the
	 * compiler is capable to incrementally compile changes or not.
	 *
	 * @param added   the sections added to the wiki
	 * @param removed the sections removed from the wiki
	 * @created 31.10.2013
	 */
	void compile(Collection<Section<?>> added, Collection<Section<?>> removed);

	/**
	 * Destroys this compiler and frees its resources. This method will called if the compiler is unregistered from the
	 * CompilerManager.
	 */
	void destroy();

}
