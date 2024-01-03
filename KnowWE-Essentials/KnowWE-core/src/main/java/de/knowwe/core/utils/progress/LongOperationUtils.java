package de.knowwe.core.utils.progress;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.events.Event;
import com.denkbares.events.EventManager;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.event.ArticleRegisteredEvent;

public class LongOperationUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(LongOperationUtils.class);

	private static ExecutorService threadPool;

	public static ExecutorService getThreadPool() {
		// initialize thread pool if not exists
		if (threadPool == null) {
			int threadCount = Runtime.getRuntime().availableProcessors() * 3 / 2;
			threadPool = Executors.newFixedThreadPool(threadCount, runnable -> {
				Thread thread = new Thread(runnable, "long-operation-thread");
				thread.setPriority(Thread.MIN_PRIORITY);
				return thread;
			});
			LOGGER.debug("created multicore thread pool of size " + threadCount);
		}
		// and return new executor based on the thread pool
		return threadPool;
	}

	/**
	 * Checks if the operation of the current thread has been interrupted. If so, an {@link InterruptedException} is
	 * thrown and the threads interrupt state is restored.
	 *
	 * @throws InterruptedException if the thread was interrupted
	 * @created 30.07.2013
	 */
	public static void checkCancel() throws InterruptedException {
		if (Thread.interrupted()) throw new InterruptedException();
	}

	/**
	 * Adds / registers a potential long operation to a specific section.
	 *
	 * @param section   the section to add the operatiopn for
	 * @param operation the operation to be added
	 * @return an identifier to be used to access the registered operation
	 * @created 30.07.2013
	 * @see #getLongOperation(Section, String)
	 */
	public static String registerLongOperation(Section<?> section, LongOperation operation) {
		String key = getRegistrationID(section, operation);
		if (key != null) return key;

		Map<String, LongOperation> map = accessLongOperations(section, true);
		key = operation.getId();
		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (map) {
			map.put(key, operation);
		}
		return key;
	}

	/**
	 * Returns the id of an operation being registered to a specific section. This method returns null if the operation
	 * is not registered to the section.
	 *
	 * @param section   the section to check the operations for
	 * @param operation the operation to get the id for
	 * @return the registered id
	 * @created 30.07.2013
	 */
	public static String getRegistrationID(Section<?> section, LongOperation operation) {
		Map<String, LongOperation> map = accessLongOperations(section, true);
		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (map) {
			for (Entry<String, LongOperation> entry : map.entrySet()) {
				if (entry.getValue().equals(operation)) return entry.getKey();
			}
			return null;
		}
	}

	/**
	 * Returns a list of operations that are registered as potential operations for a specific section. This method
	 * always returns a list, potentially empty. it never returns null.
	 *
	 * @param section the section to get the operations for
	 * @return the list of operations
	 * @created 30.07.2013
	 */
	public static Collection<LongOperation> getLongOperations(Section<?> section) {
		Map<String, LongOperation> loMap = accessLongOperations(section, false);
		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (loMap) {
			return Collections.unmodifiableCollection(new ArrayList<>(loMap.values()));
		}
	}

	/**
	 * Returns an operation that is registered as potential operations for a specific section with the specified name.
	 * if no such operation exists, null is returned.
	 *
	 * @param section     the section to get the operations for
	 * @param operationID the id of the requested operation
	 * @return the operations
	 * @created 30.07.2013
	 */
	public static LongOperation getLongOperation(Section<?> section, String operationID) {
		Map<String, LongOperation> loMap = accessLongOperations(section, false);
		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (loMap) {
			return loMap.get(operationID);
		}
	}

	private volatile static com.denkbares.events.EventListener LONG_OPERATION_CLEANER = null;

	private static final Map<String, Map<String, LongOperation>> LONG_OPERATIONS = new ConcurrentHashMap<>();

	private static Map<String, LongOperation> accessLongOperations(Section<?> section, boolean create) {
		lazyInitLongOperationCleaner();
		String key = LongOperation.class.getName();
		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (section) {
			Map<String, LongOperation> longOperationsOfSection = LONG_OPERATIONS.get(section.getID());
			if (longOperationsOfSection == null) {
				if (!create) return Collections.emptyMap();
				longOperationsOfSection = new LinkedHashMap<>();
				LONG_OPERATIONS.put(section.getID(), longOperationsOfSection);
			}
			return longOperationsOfSection;
		}
	}

	private static void lazyInitLongOperationCleaner() {
		if (LONG_OPERATION_CLEANER == null) {
			synchronized (LongOperationUtils.class) {
				if (LONG_OPERATION_CLEANER == null) {
					LONG_OPERATION_CLEANER = new com.denkbares.events.EventListener() {
						@Override
						public Collection<Class<? extends Event>> getEvents() {
							return List.of(ArticleRegisteredEvent.class);
						}

						@Override
						public void notify(Event event) {
							LongOperationUtils.cleanupOutdatedLongOperations();
						}
					};
					EventManager.getInstance().registerListener(LONG_OPERATION_CLEANER);
				}
			}
		}
	}

	private static void cleanupOutdatedLongOperations() {
		LONG_OPERATIONS.keySet().removeIf(id -> !Sections.isLive(Sections.get(id)));
	}

	/**
	 * Starts the given {@link LongOperation} in its own thread.
	 *
	 * @param context   the context of the user requesting the start of the operation
	 * @param operation the operation to be started
	 * @created 13.09.2013
	 */
	public static void startLongOperation(final UserActionContext context, final LongOperation operation) {

		try {
			operation.reset();
			operation.getProgressListener().updateProgress(0, "Initializing...");
			operation.getProgressListener().setRunning(true);
			operation.execute(context);
		}
		catch (IOException | LongOperationException e) {
			LOGGER.warn("Cannot complete operation.", e);
			operation.getProgressListener().setError("Error occurred: " + e.getMessage());
		}
		catch (InterruptedException e) {
			LOGGER.info("Operation canceled by user.");
			operation.getProgressListener().setError("Canceled by user.");
		}
		catch (Throwable e) {
			// use Throwable here, so that the user can see,
			// even if there is an internal server error
			// (like wrong linkage)
			LOGGER.error("Cannot complete operation, unexpected internal error.", e);
			operation.getProgressListener().setError("Unexpected internal error: " + e.getMessage() + ".");
		}
		finally {
			operation.getProgressListener().setRunning(false);
			operation.doFinally();
		}
	}
}
