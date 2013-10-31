package de.knowwe.core.compile;

import java.util.Collection;

import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.parsing.Section;

/**
 * Interface that defines a compiler that compiles a special artifact instance.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 30.10.2013
 * @param <Artifact> the artifact that will be compiled as the result of this
 *        compiler
 */
public interface Compiler {

	/**
	 * Returns the {@link TerminologyManager} that will hold all defined
	 * terminology of this compiler. Each compiler has its own terminology
	 * manager that will be distinct by all other compilers.
	 * 
	 * @created 30.10.2013
	 * @return the {@link TerminologyManager} of this compiler
	 */
	TerminologyManager getTerminologyManager();

	/**
	 * This method is called once to initialize the compiler right after it has
	 * been added to the specified compiler manager.
	 * 
	 * @created 31.10.2013
	 * @param compilerManager the {@link CompilerManager} this compiler has been
	 *        added to
	 */
	void init(CompilerManager compilerManager);

	/**
	 * When this method is called, the compiler shall compile its artifact. The
	 * specified changes sections may be used to optimize the compilation as the
	 * compiler is capable to incrementally compile changes or not.
	 * 
	 * @created 31.10.2013
	 * @param added the sections added to the wiki
	 * @param removed the sections removed from the wiki
	 */
	void compile(Collection<Section<?>> added, Collection<Section<?>> removed);

}
