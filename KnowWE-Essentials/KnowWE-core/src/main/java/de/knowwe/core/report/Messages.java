/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.knowwe.core.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.strings.Strings;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.CompileScript;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;

public final class Messages {
	private static final Logger LOGGER = LoggerFactory.getLogger(Messages.class);

	private static final String MESSAGE_KEY = "message_map_key";

	/**
	 * This set holds all Sections with Messages.
	 */
	private static final Map<Message.Type, Set<Section<?>>> sectionsWithMessages = new ConcurrentHashMap<>();

	/**
	 * If present, renders {@link Message}s for the given section. Messages of higher {@link Message.Type} come first,
	 * otherwise messages are sorted by its text.
	 *
	 * @param section the section which to render messages for
	 * @param result where to render the messages to
	 */
	public static void renderMessages(Section<?> section, RenderResult result) {
		Messages.getMessages(section).stream()
				.sorted((message1, message2) -> {
					int ordinalResult = Integer.compare(message2.getType().ordinal(), message1.getType().ordinal());
					if (ordinalResult != 0) {
						return ordinalResult;
					}
					return message1.getVerbalization().compareTo(message2.getVerbalization());
				})
				.forEach(message -> {
					MessageRenderer renderer = section.get().getMessageRenderer(message.getType());
					if (renderer != null) {
						renderer.preRenderMessage(message, null, null, result);
						renderer.postRenderMessage(message, null, null, result);
					}
				});
	}

	/**
	 * Wraps a single or more {@link Message}s into a Collection to be returned by the {@link CompileScript}
	 * implementations. {@link Message}
	 *
	 * @param messages the {@link Message}(s) to be wrapped
	 * @return the wrapped {@link Message}(s)
	 * @created 16.08.2010
	 */
	public static Collection<Message> asList(Message... messages) {
		return Arrays.asList(messages);
	}

	/**
	 * Removes all {@link Message}s from the given source stored for this Section and article.
	 *
	 * @param compiler the {@link Compiler} the {@link Message}s are stored for
	 * @param section  the {@link Section} the {@link Message}s are stored for
	 * @param source   the source the {@link Message}s are stored for
	 * @created 01.12.2011
	 */
	public static void clearMessages(Compiler compiler, Section<? extends Type> section, Object source) {
		storeMessages(compiler, section, source, Messages.noMessage());
	}

	/**
	 * Removes all {@link Message}s from the given source stored for this Section and compiler independently.
	 *
	 * @param section is the Section the {@link Message}s are stored for
	 * @param source  is the source the {@link Message}s are stored for
	 * @created 01.12.2011
	 */
	public static void clearMessages(Section<? extends Type> section, Object source) {
		storeMessages(null, section, source, Messages.noMessage());
	}

	/**
	 * Removes all {@link Message}s from all sources for stored for this Section and article.
	 *
	 * @param compiler the {@link Compiler} the {@link Message}s are stored for
	 * @param section  is the {@link Section} the {@link Message}s are stored for
	 * @created 01.12.2011
	 */
	public static void clearMessages(Compiler compiler, Section<?> section) {
		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (section) {
			Map<Object, Collection<Message>> map = removeMessagesMap(compiler, section);
			if (map != null) cleanUpSectionsWithMessagesMap(section);
		}
	}

	/**
	 * Removes all messages stored in any Section for the given Compiler. Should be pretty fast, because we know which
	 * sections have messages.
	 *
	 * @param compiler the compiler for which the messages should be removed
	 */
	public static void clearMessages(Compiler compiler) {
		for (Section<?> section : getSectionsWithMessages()) {
			clearMessages(compiler, section);
		}
	}

	/**
	 * Clears all {@link Message}s for the given compiler and subtree.
	 *
	 * @param compiler is the compiler you want to clear the message for
	 * @param sec      is the root of the subtree you want to clear the message for
	 */
	public static void clearMessagesRecursively(Compiler compiler, Section<?> sec) {
		clearMessages(compiler, sec);
		for (Section<?> child : sec.getChildren()) {
			clearMessagesRecursively(compiler, child);
		}
	}

	/**
	 * Clears all {@link Message}s for the given compiler and subtree.
	 *
	 * @param compiler is the compiler you want to clear the message for
	 * @param sec      is the root of the subtree you want to clear the message for
	 * @param source   is the source you want to clear the messages for
	 */
	public static void clearMessagesRecursively(Compiler compiler, Section<?> sec, Object source) {
		clearMessages(compiler, sec, source);
		for (Section<?> child : sec.getChildren()) {
			clearMessagesRecursively(compiler, child, source);
		}
	}

	public static Message creationFailedWarning(String name) {
		return Messages.warning("Failed to create: " + name);
	}

	/**
	 * Creates and returns a {@link Message} of the {@link Type} ERROR with the given
	 * text.
	 *
	 * @param message is the text content of the created {@link Message}
	 * @created 01.12.2011
	 */
	public static Message error(String message) {
		return new Message(Message.Type.ERROR, message);
	}

	/**
	 * Creates and returns a {@link Message} of the {@link Type} ERROR with the given
	 * text.
	 *
	 * @param message is the text content of the created {@link Message}
	 * @param e       an exception going along with the message
	 * @created 01.12.2011
	 */
	public static Message error(String message, Exception e) {
		return new Message(Message.Type.ERROR, message, e);
	}

	/**
	 * Creates and returns a {@link Message} of the {@link Type} ERROR with the given
	 * exception.
	 *
	 * @param e the exception to take the message text from
	 * @created 01.12.2011
	 */
	public static Message error(Exception e) {
		String message = e.getMessage();
		return error(Strings.isBlank(message) ? String.valueOf(e) : message);
	}

	/**
	 * Filters the given Collection of {@link Message}s and returns a new Collection containing only {@link Message}s of
	 * the {@link Type} ERROR.
	 *
	 * @created 01.12.2011
	 */
	public static Collection<Message> getErrors(Collection<Message> messages) {
		return messages.stream()
				.filter(msg -> msg.getType() == Message.Type.ERROR)
				.collect(Collectors.toList());
	}

	/**
	 * Returns an unmodifiable {@link Map} with Collections of all {@link Message}s of the given {@link
	 * Type}s stored for the given {@link Section}. The Collections are mapped by the
	 * {@link Compiler} the {@link Message}s were stored for. If {@link Message}s were stored without an argument {@link
	 * Compiler} , the {@link Map} will contain this {@link Collection} with <tt>null</tt> as the <tt>key</tt>.
	 *
	 * @param section is the {@link Section} the {@link Message}s are stored for
	 * @param types   is the {@link Type} of {@link Message} you want (set to
	 *                <tt>null</tt> if you want all)
	 * @created 16.02.2012
	 */
	public static Map<Compiler, Collection<Message>> getMessagesMap(Section<? extends Type> section, Message.Type... types) {
		if (section.isEmpty()) {
			return Collections.emptyMap();
		}

		Map<Compiler, Object> messagesOfAllTypesBySourceByTitle = section.getObjects(MESSAGE_KEY);
		if (messagesOfAllTypesBySourceByTitle.isEmpty()) {
			return Collections.emptyMap();
		}

		Map<Compiler, Collection<Message>> allMassagesOfCompiler = new HashMap<>();
		for (Entry<Compiler, Object> entry : messagesOfAllTypesBySourceByTitle.entrySet()) {
			@SuppressWarnings("unchecked")
			Map<Compiler, Collection<Message>> messagesOfAllTypesBySource = (Map<Compiler, Collection<Message>>) entry
					.getValue();
			Collection<Message> messagesOfGivenTypesOfAllSourcesOfTitle = new ArrayList<>();
			for (Collection<Message> messagesOfAllTypesOfSource : messagesOfAllTypesBySource.values()) {
				addAllMessagesOfTypes(messagesOfAllTypesOfSource, messagesOfGivenTypesOfAllSourcesOfTitle, types);
			}
			if (!messagesOfGivenTypesOfAllSourcesOfTitle.isEmpty()) {
				allMassagesOfCompiler.put(entry.getKey(), messagesOfGivenTypesOfAllSourcesOfTitle);
			}
		}
		return Collections.unmodifiableMap(allMassagesOfCompiler);
	}

	/**
	 * Checks if there are any messages of the specified type or the specified types sub-tree, for any compiler and/or
	 * independent of any compiler.
	 *
	 * @param section the root section of the sub-tree to be checked
	 * @param types   the error messages considered
	 * @return if there are any such messages
	 * @created 06.02.2014
	 */
	public static boolean hasMessagesInSubtree(Section<? extends Type> section, Message.Type... types) {
		if (hasMessages(section, types)) return true;
		for (Section<?> child : section.getChildren()) {
			if (hasMessagesInSubtree(child, types)) return true;
		}
		return false;
	}

	/**
	 * Checks if there are any messages of the specified type, for any compiler and/or independent of any compiler.
	 *
	 * @param section the root section of the subtree to be checked
	 * @param types   the type of messages to be considered... leave empty to consider any type
	 * @return if there are any such messages
	 * @created 06.02.2014
	 */
	public static boolean hasMessages(Section<? extends Type> section, Message.Type... types) {
		if (types.length == 0) types = Message.Type.values();
		for (Message.Type type : types) {
			if (sectionsWithMessages.getOrDefault(type, Collections.emptySet()).contains(section)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns an unmodifiable Collection containing all {@link Message}s of the given {@link
	 * Type}s stored for this article and Section.
	 *
	 * @param compiler the {@link Compiler} the {@link Message}s are stored for
	 * @param section  is the {@link Section} the {@link Message}s are stored for
	 * @param types    is the {@link Type} of {@link Message} you want (set to
	 *                 <tt>null</tt> if you want all)
	 * @created 01.12.2011
	 */
	public static Collection<Message> getMessages(Compiler compiler, Section<?> section, Message.Type... types) {
		if (section.isEmpty()) return Collections.emptyList();
		Collection<Message> allMessages = new ArrayList<>();
		Map<Object, Collection<Message>> msgMapModifiable = getMessagesMap(compiler, section);
		if (msgMapModifiable != null) {
			for (Collection<Message> messages : msgMapModifiable.values()) {
				addAllMessagesOfTypes(messages, allMessages, types);
			}
		}
		return Collections.unmodifiableCollection(allMessages);
	}

	/**
	 * Returns an unmodifiable Collection containing all {@link Message}s of the given {@link
	 * Type}s stored for this article and Section.
	 *
	 * @param section is the {@link Section} the {@link Message}s are stored for
	 * @param types   is the {@link Type} of {@link Message} you want (set to
	 *                <tt>null</tt> if you want all)
	 * @created 01.12.2011
	 */
	public static Collection<Message> getMessages(Section<?> section, Message.Type... types) {
		return getMessages((Compiler) null, section, types);
	}

	/**
	 * Returns an unmodifiable Collection containing all {@link Message}s of the given {@link
	 * Type}s stored for this article, section, and source.
	 *
	 * @param compiler is the article the {@link Message}s are stored for
	 * @param section  is the Section the {@link Message}s are stored for
	 * @param source   is the source the {@link Message}s are stored for
	 * @param types    is the {@link Type} of {@link Message} you want (set to
	 *                 <tt>null</tt> if you want all)
	 * @created 01.12.2011
	 */
	public static Collection<Message> getMessages(Compiler compiler, Section<?> section, Object source, Message.Type... types) {
		if (section.isEmpty()) return Collections.emptyList();
		Map<Object, Collection<Message>> msgsMap = getMessagesMap(compiler, section);
		List<Message> allMsgs = new ArrayList<>();
		if (msgsMap != null) {
			Collection<Message> msgs = msgsMap.get(source);
			if (msgs != null) addAllMessagesOfTypes(msgs, allMsgs, types);
		}
		return Collections.unmodifiableCollection(allMsgs);
	}

	/**
	 * Returns an unmodifiable Collection containing all {@link Message}s of the given {@link
	 * Type}s stored for this section and source, independently of any {@link Compiler}
	 *
	 * @param section is the Section the {@link Message}s are stored for
	 * @param source  is the source the {@link Message}s are stored for
	 * @param types   is the {@link Type} of {@link Message} you want (set to
	 *                <tt>null</tt> if you want all)
	 * @created 01.12.2011
	 */
	public static Collection<Message> getMessages(Section<?> section, Object source, Message.Type... types) {
		return getMessages(null, section, source, types);
	}

	/**
	 * Returns an unmodifiable {@link Map} with Collections of all {@link Message}s of the given {@link
	 * Type}s stored in the KDOM subtree with the given {@link Section} as root. The
	 * Collections are mapped by the title of the {@link Compiler}s the {@link Message}s were stored for. If {@link
	 * Message}s were stored without an argument {@link Compiler}, the {@link Map} will contain this {@link Collection}
	 * with <tt>null</tt> as the <tt>key</tt>.
	 * <p>
	 * The map is sorted by compiler (see {@link Compilers#COMPARATOR}).
	 *
	 * @param section is the {@link Section} the {@link Message}s are stored for
	 * @param types   is the {@link Type} of {@link Message} you want (set to
	 *                <tt>null</tt> if you want all)
	 * @created 16.02.2012
	 */
	public static Map<Compiler, Collection<Message>> getMessagesMapFromSubtree(Section<?> section, Message.Type... types) {
		Map<Compiler, Collection<Message>> allMessages = new LinkedHashMap<>();
		getMessagesMapFromSubtree(allMessages, section, types);
		return Collections.unmodifiableMap(allMessages);
	}

	private static void getMessagesMapFromSubtree(Map<Compiler, Collection<Message>> allMessagesOfSubtree, Section<?> section, Message.Type... types) {
		getMessagesMap(section, types).entrySet().stream()
				.sorted(Entry.comparingByKey(Compilers.COMPARATOR))
				.forEach(e -> allMessagesOfSubtree.computeIfAbsent(e.getKey(), c -> new ArrayList<>())
						.addAll(e.getValue())
				);
		for (Section<?> child : section.getChildren()) {
			getMessagesMapFromSubtree(allMessagesOfSubtree, child, types);
		}
	}

	/**
	 * Returns an unmodifiable Collection containing all {@link Message}s of the given {@link
	 * Type}s of the KDOM subtree with the given Section as root.
	 *
	 * @param compiler the {@link Compiler} the {@link Message}s are stored for
	 * @param section  is the root of the KDOM subtree you want the messages from
	 * @param types    is the {@link Type} of {@link Message} you want (set to
	 *                 <tt>null</tt> if you want all)
	 */
	public static Collection<Message> getMessagesFromSubtree(Compiler compiler,
															 Section<?> section, Message.Type... types) {

		Collection<Message> messages = new ArrayList<>();
		getMessagesFromSubtree(messages, compiler, section, types);
		return Collections.unmodifiableCollection(messages);
	}

	private static void getMessagesFromSubtree(Collection<Message> messages, Compiler compiler, Section<?> section, Message.Type... types) {
		messages.addAll(getMessages(compiler, section, types));
		for (Section<?> child : section.getChildren()) {
			getMessagesFromSubtree(messages, compiler, child, types);
		}
	}

	/**
	 * Returns an unmodifiable Collection containing all {@link Message}s of the given {@link
	 * Type}s of the KDOM subtree with the given Section as root, independently of any
	 * compiler.
	 *
	 * @param section is the root of the KDOM subtree you want the messages from
	 * @param types   is the {@link Type} of {@link Message} you want (set to
	 *                <tt>null</tt> if you want all)
	 */
	public static Collection<Message> getMessagesFromSubtree(Section<?> section, Message.Type... types) {
		Collection<Message> messages = new ArrayList<>();
		getMessagesFromSubtree(messages, null, section, types);
		return Collections.unmodifiableCollection(messages);
	}

	/**
	 * This method is private to avoid misuse (this map is modifiable). The map contains all {@link Message}s for the
	 * given Section and article. The Collections are mapped by the String
	 * <tt>source.getName()</tt>.
	 */
	private static Map<Object, Collection<Message>> getMessagesMap(Compiler compiler, Section<?> sec) {
		return sec.getObject(compiler, MESSAGE_KEY);
	}

	private static Map<Object, Collection<Message>> removeMessagesMap(Compiler compiler, Section<?> sec) {
		return sec.removeObject(compiler, MESSAGE_KEY);
	}

	private static void addAllMessagesOfTypes(Collection<Message> source, Collection<Message> target, Message.Type... types) {
		if (types.length == 0) {
			target.addAll(source);
		}
		else {
			for (Message msg : source) {
				for (Message.Type type : types) {
					if (msg.getType() == type) {
						target.add(msg);
					}
				}
			}
		}
	}

	/**
	 * Filters the given Collection of {@link Message}s and returns a new Collection containing only {@link Message}s of
	 * the {@link Type} INFO.
	 *
	 * @created 01.12.2011
	 */
	public static Collection<Message> getNotices(Collection<Message> messages) {
		return messages.stream()
				.filter(msg -> msg.getType() == Message.Type.INFO)
				.collect(Collectors.toList());
	}

	/**
	 * Filters the given Collection of {@link Message}s and returns a new Collection containing only {@link Message}s of
	 * the {@link Type} WARNING.
	 *
	 * @created 01.12.2011
	 */
	public static Collection<Message> getWarnings(Collection<Message> messages) {
		return messages.stream()
				.filter(msg -> msg.getType() == Message.Type.WARNING)
				.collect(Collectors.toList());
	}

	public static Message invalidNumberError(String message) {
		return Messages.error("Invalid Number: " + message);
	}

	public static Message invalidNumberWarning(String message) {
		return Messages.warning("Invalid Number: " + message);
	}

	public static Message noSuchObjectError(String name) {
		return Messages.noSuchObjectError("Object", name);
	}

	public static Message noSuchObjectError(String type, String name) {
		return noSuchObjectError(type, name, Message.Type.ERROR);
	}

	public static Message noSuchObjectError(String type, String name, Message.Type messageType) {
		if (name != null && name.isEmpty()) {
			name = "empty String";
		}
		name = Strings.trimQuotes(name);
		String message = type + " '" + name + "' not found";
		return switch (messageType) {
			case WARNING -> Messages.warning(message);
			case INFO -> Messages.info(message);
			default -> Messages.error(message);
		};
	}

	/**
	 * Creates and returns a {@link Message} of the {@link Type} INFO with the given
	 * text.
	 *
	 * @param message is the text content of the created {@link Message}
	 * @created 01.12.2011
	 */
	public static Message info(String message) {
		return new Message(Message.Type.INFO, message);
	}

	public static Message objectAlreadyDefinedError(String text) {
		return Messages.objectAlreadyDefinedError(text, (Section) null);
	}

	public static Message objectAlreadyDefinedError(String text, Section<?> definition) {
		String result = "Object already defined: " + text;
		if (definition != null) {
			result += " in: " + definition.getTitle();
		}
		return Messages.error(result);
	}

	public static Message objectAlreadyDefinedError(String text, Collection<Section<?>> definitions) {
		String result = "Object already defined: " + text;
		if (definitions != null && !definitions.isEmpty()) {
			result += " in: " + definitions.stream().map(Section::getTitle).collect(Collectors.joining(", "));
		}
		return Messages.error(result);
	}

	public static Message objectAlreadyDefinedWarning(String text) {
		return Messages.warning("Object already defined: " + text);
	}

	public static Message objectCreationError(String text) {
		return Messages.error("Could not create Object: " + text);
	}

	public static Message ambiguousTermClassesError(String origTerm, Collection<Class<?>> termClasses) {
		origTerm = Strings.trimQuotes(origTerm);
		return Messages.error("The term '" + origTerm
				+ "' is defined with ambiguous term classes: "
				+ termClasses.stream()
				.map(Class::getSimpleName)
				.sorted().collect(Collectors.joining(", ")));
	}

	public static Message ambiguousTermCaseWarning(Collection<?> termObjects) {
		TreeSet<String> sortedIdentifiers = termObjects.stream()
				.map(Object::toString)
				.collect(Collectors.toCollection(TreeSet::new));
		return Messages.warning("There are different cases for the same term: "
				+ sortedIdentifiers);
	}

	/**
	 * Stores a single Message for the given Section and source.
	 * <p/>
	 * <b>ATTENTION: For this method applies the same as for the method
	 * KnowWEUtils#storeMessages(Section, Class, Class, Collection) . It can only be used once for the given set of
	 * parameters. If you use this method a second time with the same parameters, the first Message gets
	 * overwritten!</b>
	 *
	 * @param compiler the {@link Compiler} the {@link Message}s are stored for
	 * @param section  is the {@link Section} the {@link Message}s are stored for
	 * @param source   is the Class the message originate from
	 * @param msg      is the message you want so store
	 */
	public static void storeMessage(Compiler compiler, Section<?> section, Object source, Message msg) {
		Collection<Message> messages = Collections.emptyList();
		if (msg != null) {
			messages = Messages.asList(msg);
		}
		storeMessages(compiler, section, source, messages);
	}

	/**
	 * Stores a single Message for the given Section and source independent from any compiler.
	 * <p/>
	 * <b>ATTENTION: For this method applies the same as for the method
	 * KnowWEUtils#storeMessages(Section, Class, Class, Collection) . It can only be used once for the given set of
	 * parameters. If you use this method a second time with the same parameters, the first Message gets
	 * overwritten!</b>
	 *
	 * @param section is the section you want to store the message for
	 * @param source  is the Class the message originate from
	 * @param msg     is the message you want so store
	 */
	public static void storeMessage(Section<?> section, Object source, Message msg) {
		storeMessage(null, section, source, msg);
	}

	/**
	 * Stores the given Collection of {@link Message}s <tt>m</tt> from the Class <tt>source</tt> for the Article
	 * <tt>article</tt> and the Section <tt>s</tt>.
	 * <p/>
	 * <b>ATTENTION: This method can only be used once for each article, section, and source. If you
	 * use this Method a second time with the same parameters, the first Collection gets overwritten!</b>
	 *
	 * @param compiler the {@link Compiler} the {@link Message}s are stored for
	 * @param section  is the {@link Section} the {@link Message}s are stored for
	 * @param source   is the Class the messages originate from
	 * @param messages is the Collection of messages you want so store
	 */
	public static void storeMessages(Compiler compiler, Section<?> section, Object source, Collection<Message> messages) {
		Map<Object, Collection<Message>> messagesMap = getMessagesMap(compiler, section);
		// we have messages to store
		if (messages != null && !messages.isEmpty()) {
			messages.forEach(m -> m.setSource(source));
			//noinspection SynchronizationOnLocalVariableOrMethodParameter
			synchronized (section) {
				// create map if not already present
				if (messagesMap == null) {
					// we have to get the map again, in case the map was created and stored before synchronizing in another thread
					messagesMap = getMessagesMap(compiler, section);
					if (messagesMap == null) {
						// still null? ok, let's create and store
						messagesMap = new HashMap<>(4);
						KnowWEUtils.storeObject(compiler, section, MESSAGE_KEY, messagesMap);
					}
				}
				// store messages in map
				messagesMap.put(source, Collections.unmodifiableCollection(messages));
				// store section for type collections
				for (Message message : messages) {
					sectionsWithMessages
							.computeIfAbsent(message.getType(), k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
							.add(section);
				}
			}
		}
		// we have no messages, which means that we want to delete existing messages
		else {
			// if no message map is present, we don't need do anything
			if (messagesMap == null) return;
			//noinspection SynchronizationOnLocalVariableOrMethodParameter
			synchronized (section) {
				Collection<Message> removedMessages = messagesMap.remove(source);
				// we did not remove anything, no cleanup needed
				if (removedMessages == null || removedMessages.isEmpty()) return;
				// we removed messages, cleanup!
				// no more more messages for this compiler and section
				if (messagesMap.isEmpty()) {
					removeMessagesMap(compiler, section);
				}
				// remove section from type collections where no messages of that type remain
				cleanUpSectionsWithMessagesMap(section);
			}
		}
	}

	/**
	 * <p><b>THIS METHOD DOES NOT DELETE MESSAGES!</b></p>
	 * It just checks whether there are still messages for the given section. If not, it cleans up the map holding the
	 * information if there are still messages.
	 *
	 * @param section the section for which we want to do the cleanup
	 */
	private static void cleanUpSectionsWithMessagesMap(Section<?> section) {
		Map<Compiler, Collection<Message>> messagesByCompiler = getMessagesMap(section);
		Set<Message.Type> availableTypes = new HashSet<>();
		for (Collection<Message> messagesOfCompiler : messagesByCompiler.values()) {
			for (Message message : messagesOfCompiler) {
				availableTypes.add(message.getType());
			}
		}
		for (Message.Type type : Message.Type.values()) {
			if (!availableTypes.contains(type)) {
				removeSectionFromMessageTracking(type, section);
			}
		}
	}

	/**
	 * Returns all Sections that currently have stored Messages either of Type ERROR or WARNING.
	 */
	public static Collection<Section<?>> getSectionsWithMessages(Message.Type... types) {
		if (types.length == 0) types = Message.Type.values();
		Set<Section<?>> sections = new HashSet<>();
		for (Message.Type type : types) {
			sections.addAll(sectionsWithMessages.getOrDefault(type, Collections.emptySet()));
		}
		return Collections.unmodifiableCollection(sections);
	}

	/**
	 * Stores the given Collection of {@link Message}s for the given Class in the given Section independently from any
	 * {@link Compiler}s.
	 * <p/>
	 * <b>ATTENTION: This method can only be used once for each article,†† section, and source. If
	 * you use this Method a second time with the same parameters, the first Collection gets overwritten!</b>
	 *
	 * @param section  is the section you want to store the messages for
	 * @param source   is the Class the messages originate from
	 * @param messages is the Collection of messages you want so store
	 */
	public static void storeMessages(Section<?> section, Object source, Collection<Message> messages) {
		storeMessages(null, section, source, messages);
	}

	/**
	 * Returns a {@link Message} to be used when a SubtreeHandler recognizes an syntactical error within its markup.
	 *
	 * @param message the {@link Message} of the syntax error
	 * @created 18.08.2010
	 */
	public static Message syntaxError(String message) {
		return Messages.error("Syntax Error: " + message);
	}

	/**
	 * Returns a {@link Message} to be used when a SubtreeHandler recognizes an unexpected internal error based on an
	 * exception. The error will also be logged as a warning.
	 *
	 * @param message the message of the error
	 * @param e       the exception occurred
	 * @created 18.08.2010
	 */
	public static Message internalError(String message, Throwable e) {
		LOGGER.warn(message, e);
		return Messages.error(message + ": " + e);
	}

	/**
	 * Creates and returns a {@link Message} of the {@link Type} WARNING with the given
	 * text.
	 *
	 * @param message is the text content of the created {@link Message}
	 * @created 01.12.2011
	 */
	public static Message warning(String message) {
		return new Message(Message.Type.WARNING, message);
	}

	private Messages() {
	}

	/**
	 * Creates an empty collection of messages.
	 *
	 * @return collection of no message at all
	 * @created 28.02.2012
	 */
	public static Collection<Message> noMessage() {
		return Collections.emptyList();
	}

	public static ResourceBundle getMessageBundle(Locale locale) {
		return ResourceBundle.getBundle("KnowWE_messages", locale);
	}

	public static ResourceBundle getMessageBundle() {
		return getMessageBundle(Locale.getDefault());
	}

	public static ResourceBundle getMessageBundle(UserContext user) {
		return getMessageBundle(Environment.getInstance().getWikiConnector().getLocale(
				user.getRequest()));
	}

	public static void unregisterMessagesSection(Section<?> section) {
		for (Message.Type type : Message.Type.values()) {
			removeSectionFromMessageTracking(type, section);
		}
	}

	private static void removeSectionFromMessageTracking(Message.Type type, Section<?> section) {
		Set<Section<?>> sections = sectionsWithMessages.get(type);
		if (sections != null) sections.remove(section);
	}
}
