package de.knowwe.core.compile;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.CompilerMessage;

/**
 * This interface defines a compilation unit that is used to compile some piece
 * of wiki markup to enhance the artifact of the compiler or have any other side
 * effects to the compiler itself.
 * <p>
 * This interface replaces the previously used SubtreeHandler.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 30.10.2013
 * @param <T> the type of the section to be compiled
 * @param <C> the compiler this compilation unit is for
 */
public interface CompileScript<C extends Compiler, T extends Type> {

	/**
	 * Compiles a specified section for the specified compiler instance. The
	 * method shall check the validity of the specified section and/or modify
	 * some data of the compilers build artifacts (e.g. modify the knowledge
	 * base for a d3web compiler).
	 * <p>
	 * If there are any errors or warnings during compilation, the compiler may
	 * throw a {@link CompilerMessage} to indicate the compilation issue.
	 * 
	 * @created 18.01.2014
	 * @param compiler the compiler the section shall be compiled for
	 * @param section the section to be compiled
	 * @throws CompilerMessage the message(s) indicating a problem or a
	 *         user-relevant information
	 */
	void compile(C compiler, Section<T> section) throws CompilerMessage;

	/**
	 * Returns the class instance of the {@link Compiler} the script is intended
	 * for.
	 * 
	 * @created 30.10.2013
	 * @return the compiler class of this script
	 */
	Class<C> getCompilerClass();
}
