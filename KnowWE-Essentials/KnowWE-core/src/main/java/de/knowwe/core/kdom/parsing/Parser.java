package de.knowwe.core.kdom.parsing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.knowwe.core.kdom.Type;

/**
 * This interface defines a parser to interpret textual markup of a specific content and generates a kdom by parsing the
 * text.
 *
 * @author albercht.striffler
 * @created 12.03.2011
 */
@FunctionalInterface
public interface Parser {

	/**
	 * Parses the specified text and creates a new kdom from the text content. The created kdom is intended to be a
	 * child of the specified parent section. The newly created root section of the newly created kdom (sub-)tree is
	 * returned. The method return null if no section could be created by the parser.
	 *
	 * @param text   the textual markup to be parsed
	 * @param parent the parent section of the kdom subtree to be created
	 * @return the parsed kdom subtree
	 * @created 12.03.2011
	 */
	@Nullable
	Section<? extends Type> parse(@NotNull String text, @Nullable Section<? extends Type> parent);
}
