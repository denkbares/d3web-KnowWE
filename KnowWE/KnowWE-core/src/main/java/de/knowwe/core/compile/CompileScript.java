package de.knowwe.core.compile;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;

/**
 * This interface defines a compilation unit hat is used to compile some piece
 * of wiki markup to enhance the artifact of the compiler or have any other side
 * effects to the compiler itself.
 * <p>
 * This interface replaces the previously used {@link SubtreeHandler}.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 30.10.2013
 * @param <T> the type of the section to be compiled
 * @param <C> the compiler this compilation unit is for
 */
public interface CompileScript<C extends Compiler, T extends Type> {

	void compile(C compiler, Section<T> section);

	/**
	 * Returns the class instance the script is intended for.
	 * 
	 * @created 30.10.2013
	 * @return the compiler class of this script
	 */
	Class<C> getCompilerClass();

}
