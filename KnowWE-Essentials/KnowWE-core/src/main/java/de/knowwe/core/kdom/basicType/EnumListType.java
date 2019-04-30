/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */
package de.knowwe.core.kdom.basicType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * Type to match a list of enumerated values.
 *
 * @author volker_belli
 * @created 21.01.2011
 */
public class EnumListType extends AbstractType {

	private static final String DEFAULT_SEPERATOR = "[\\h\\s\\v,;]+";

	private final KeywordType splitter;

	/**
	 * Creates a new enumerated list type for the enum constant names of the specified enum class, with whitespaces or
	 * ',' or ';' as separator characters.
	 *
	 * @param enumClass the enumeration to get the keywords from
	 */
	public EnumListType(Class<? extends Enum<?>> enumClass) {
		this(DEFAULT_SEPERATOR, Stream.of(enumClass.getEnumConstants()).map(Enum::name).collect(Collectors.toList()));
	}

	/**
	 * Creates a new enumerated list type for the specified keywords, as literal strings (not as a regular expressions),
	 * with whitespaces or ',' or ';' as separator characters.
	 *
	 * @param literalKeyWords the literal keyword
	 */
	public EnumListType(String... literalKeyWords) {
		this(DEFAULT_SEPERATOR, Arrays.asList(literalKeyWords));
	}

	/**
	 * Creates a new enumerated list type for the specified keywords, as literal strings (not as a regular expressions),
	 * with whitespaces or ',' or ';' as separator characters.
	 *
	 * @param literalKeyWords the literal keyword
	 */
	public EnumListType(Collection<String> literalKeyWords) {
		this(DEFAULT_SEPERATOR, literalKeyWords);
	}

	/**
	 * Creates a new enumerated list type for the specified keywords, as literal strings (not as a regular
	 * expressions).
	 *
	 * @param literalKeyWords the literal keyword
	 */
	public EnumListType(String separatorRegex, Collection<String> literalKeyWords) {
		setSectionFinder(AllTextFinderTrimmed.getInstance());
		this.splitter = new KeywordType(Pattern.compile(separatorRegex));

		addChildType(splitter);
		literalKeyWords.stream()
				.sorted(Comparator.comparingInt(String::length).reversed())
				.map(KeywordType::new).peek(k -> k.setRenderer(StyleRenderer.CONSTANT))
				.forEach(this::addChildType);
		addChildType(UnrecognizedSyntaxType.getInstance());
	}

	/**
	 * Returns the detected and accepted keywords of the specified section. All non-accepted keywords are ignored.
	 *
	 * @param section the section to get the keywords from
	 * @return the accepted keywords
	 */
	@NotNull
	public List<String> getAcceptedKeywords(Section<? extends EnumListType> section) {
		return section.getChildren().stream()
				.filter(s -> s.get() != splitter && s.get() instanceof KeywordType)
				.map(Section::getText).map(Strings::trim).collect(Collectors.toList());
	}
}