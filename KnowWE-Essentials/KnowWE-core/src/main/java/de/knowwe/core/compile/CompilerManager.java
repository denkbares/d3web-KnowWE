package de.knowwe.core.compile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.collections.CountingSet;
import com.denkbares.collections.PriorityList;
import com.denkbares.collections.PriorityList.Group;
import com.denkbares.events.EventManager;
import com.denkbares.utils.Log;
import com.denkbares.utils.Stopwatch;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.ServletContextEventListener;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Messages;

/**
 * This class represents the compile manager for a specific {@link ArticleManager}. It is responsible to manage every
 * compile process for all articles and section of the {@link ArticleManager}. Therefore all compile code has been
 * removed out of sections and articles and placed here.
 * <p/>
 * The compile manager holds a set of compilers. The compilers can be plugged into the manager using the defined
 * extension point. Each compiler may implement its own compilation procedure. If the compiler uses the package
 * mechanism to define certain compiling bundles (such as d3web does for knowledge bases or owl for triple stores) the
 * compiler usually have multiple subsequent compilers for each such individual bundle.
 * <p/>
 * To enhance performance, each compiler top level compiles individually, maybe in parallel. Nevertheless, if the
 * compilers have different priorities (defined through the compiler's extension), they are ordered by these priorities.
 * Only compilers with same priority may compile in parallel.
 * <p/>
 */
public class CompilerManager {

	private static final Map<Class<? extends Compiler>, ScriptManager<? extends Compiler>> scriptManagers = new HashMap<>();
	private static final String KNOWWE_COMPILER_THREADS_COUNT = "knowwe.compiler.threads.count";
	// number of threads that are not used for compilers themselves, but for operational handling of compilation process
	private static final int OPERATIONAL_THREAD_COUNT = 1;
	private volatile int compilationCount = 0;

	private final PriorityList<Double, Compiler> compilers;
	// just a fast cache for the contains() method
	private final HashSet<Compiler> compilerCache;
	private final ArticleManager articleManager;

	private Iterator<Group<Double, Compiler>> running = null;
	private final ThreadPoolExecutor threadPool;
	private final Object lock = new Object();
	private final Set<String> currentlyCompiledArticles = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private final Map<Compiler, Priority> currentlyCompiledPriority = new ConcurrentHashMap<>();
	private final Set<Compiler> waitingCompilers = new CountingSet<>();
	private static final AtomicLong compileThreadNumber = new AtomicLong(1);
	private static final Map<Thread, Object> compileThreads = Collections.synchronizedMap(new WeakHashMap<>());

	public CompilerManager(ArticleManager articleManager) {
		this.articleManager = articleManager;
		this.compilerCache = new HashSet<>();
		this.compilers = new PriorityList<>(5d);
		this.threadPool = createExecutorService();
		ServletContextEventListener.registerOnContextDestroyedTask(servletContextEvent -> onContextDestroyed());
	}

	private void onContextDestroyed() {
		Log.info("Shutting down KnowWE compilers.");
		new ArrayList<>(compilerCache).forEach(this::removeCompiler);
		compileThreads.clear();
		threadPool.shutdown();
	}

	/**
	 * Checks whether the current thread is created by a CompilerManager to compile articles.
	 */
	public static boolean isCompileThread() {
		return compileThreads.containsKey(Thread.currentThread());
	}

	static ThreadPoolExecutor createExecutorService() {
		int threadCount = getCompilerThreadCount();
		ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount, runnable -> {
			Thread thread = new Thread(runnable, "KnowWE-Compiler-" + compileThreadNumber.getAndIncrement());
			thread.setDaemon(true);
			compileThreads.put(thread, null);
			return thread;
		});
		Log.fine("Created multi core thread pool of size " + threadCount);
		return pool;
	}

	private static int getCompilerThreadCount() {
		final int defaultThreadCount = Runtime.getRuntime().availableProcessors() + OPERATIONAL_THREAD_COUNT;
		final String threadCount = System.getProperty(KNOWWE_COMPILER_THREADS_COUNT, String.valueOf(defaultThreadCount));
		try {
			return Integer.parseInt(threadCount);
		}
		catch (NumberFormatException e) {
			return defaultThreadCount;
		}
	}

	/**
	 * Get the current maximum number of compilers that are allowed to compile in parallel
	 */
	private int getMaxCompilationThreadCount() {
		return threadPool.getMaximumPoolSize() - OPERATIONAL_THREAD_COUNT;
	}

	/**
	 * Set the maximum number of compilers that are allowed to compile in parallel. Can be changed at any time, but must
	 * not be < 1. Only change if you know what you are doing.
	 */
	private void setMaxCompilationThreadCount(int threadCount) {
		if (threadCount < 1) throw new IllegalArgumentException("Thread count has to be >= 1");
		threadCount += OPERATIONAL_THREAD_COUNT;
		threadPool.setMaximumPoolSize(threadCount);
		threadPool.setCorePoolSize(threadCount);
	}

	@SuppressWarnings("unchecked")
	public static <C extends Compiler> ScriptManager<C> getScriptManager(C compiler) {
		return (ScriptManager<C>) getScriptManager(compiler.getClass());
	}

	public static Collection<ScriptManager<? extends Compiler>> getScriptManagers() {
		return Collections.unmodifiableCollection(scriptManagers.values());
	}

	public static <C extends Compiler> ScriptManager<C> getScriptManager(Class<C> compilerClass) {
		@SuppressWarnings("unchecked")
		ScriptManager<C> result = (ScriptManager<C>) scriptManagers.get(compilerClass);
		if (result == null) {
			result = new ScriptManager<>(compilerClass);
			scriptManagers.put(compilerClass, result);
		}
		return result;
	}

	public static <C extends Compiler, T extends Type> void addScript(T type, CompileScript<C, T> script) {
		addScript(Priority.DEFAULT, type, script);
	}

	public static <C extends Compiler, T extends Type> void addScript(Priority priority, T type, CompileScript<C, T> script) {
		getScriptManager(script.getCompilerClass()).addScript(priority, type, script);
	}

	/**
	 * Returns the web this compiler belongs to.
	 *
	 * @return the web of this compiler
	 * @created 30.10.2013
	 */
	public String getWeb() {
		return articleManager.getWeb();
	}

	public ArticleManager getArticleManager() {
		return this.articleManager;
	}

	/**
	 * Starts the compilation based on a specified set of changing sections. The method returns true if the compilation
	 * can be started. The method returns false if the request is ignored, e.g. because of an already ongoing
	 * compilation.
	 *
	 * @return if the compilation has been started
	 * @created 30.10.2013
	 */
	private boolean startCompile(final Collection<Section<?>> added, final Collection<Section<?>> removed) {
		synchronized (lock) {
			if (isCompiling()) return false;
			setCompiling(added);
			setCompiling(removed);
			running = compilers.groupIterator();
			compilationCount++;
		}
		threadPool.execute(() -> {
			Stopwatch stopwatch = new Stopwatch();
			try {
				EventManager.getInstance().fireEvent(new CompilationStartEvent(CompilerManager.this));
				doCompile(added, removed);
			}
			catch (Throwable e) {
				Log.severe("Unexpected internal error while starting compilation.", e);
			}
			finally {
				// we fire this before the synchronization, so the method awaitCompilation waits
				// till after this event is handled completely
				EventManager.getInstance().fireEvent(new CompilationFinishedEvent(CompilerManager.this));
				synchronized (lock) {
					running = null;
					currentlyCompiledArticles.clear();
					Log.info("Compiled " + added.size() + " added and " + removed.size()
							+ " removed section" + (removed.size() == 1 ? "" : "s")
							+ " after " + stopwatch.getDisplay());
					lock.notifyAll();
				}
			}
		});
		return true;
	}

	private void setCompiling(Collection<Section<?>> sections) {
		for (Section<?> section : sections) {
			String title = section.getTitle();
			if (title == null) continue;
			currentlyCompiledArticles.add(title);
		}
	}

	private void doCompile(final Collection<Section<?>> added, final Collection<Section<?>> removed) throws InterruptedException {
		while (true) {
			// get the current compilers
			List<Compiler> simultaneousCompilers;
			synchronized (lock) {
				if (!running.hasNext()) {
					break;
				}
				Group<Double, Compiler> group = running.next();
				simultaneousCompilers = group.getElements();
			}

			// start all simultaneous compilers and
			// observe the active ones until they all have terminated
			final Set<Compiler> activeCompilers = new LinkedHashSet<>(simultaneousCompilers);

			// before actually starting it, mark all of them as started, to enable "await-priority" functionality
			// (we only store the simultaneously running ones and do not support waiting for other compiler priorities!)
			simultaneousCompilers.forEach(compiler -> setCurrentCompilePriority(compiler, Priority.INIT));

			for (final Compiler compiler : simultaneousCompilers) {
				// wait until we are allowed to compile
				threadPool.execute(() -> {

					Stopwatch stopwatch = new Stopwatch();
					try {
						// compile the content
						compiler.compile(added, removed);
					}
					catch (Throwable e) {
						String msg = "Unexpected internal exception while compiling with "
								+ compiler + ": " + e.getMessage();
						Log.severe(msg, e);
						for (Section<?> section : added) {
							// it does not matter if we store the messages
							// for the same article multiple times, because
							// for each source there can only be one
							// collection of messages
							Messages.storeMessage(section.getArticle().getRootSection(),
									this.getClass(), Messages.error(msg));
						}
					}
					finally {
						// and notify that the compiler has finished
						synchronized (lock) {
							// 1- update all required compiler flags
							activeCompilers.remove(compiler);
							Log.fine(compiler.getClass().getSimpleName() +
									" finished after " + stopwatch.getDisplay());
							clearCurrentCompilePriority(compiler);
							// 2 - notify the waiting caller of doCompile() in the synchronized block below (1)
							// always notify all, as the clear is usually a noop (if the compiler has cleared before)
							lock.notifyAll();
						}
					}
				});
			}

			// we wait until all have been terminated
			synchronized (lock) {
				while (!activeCompilers.isEmpty()) {
					// 1 - Every time we are awoken by a finished compiler above (2), we check if it was the last
					// one of the current group of compilers. If it was, we do the next group... until we are done
					// completely
					lock.wait();
				}
			}
		}
	}

	public void setCurrentCompilePriority(@NotNull Compiler compiler, @NotNull Priority priority) {
		synchronized (lock) {
			Priority previous = currentlyCompiledPriority.put(compiler, priority);
			if (previous == null || previous.intValue() != priority.intValue()) {
				lock.notifyAll();
			}
		}
	}

	public void clearCurrentCompilePriority(@NotNull Compiler compiler) {
		synchronized (lock) {
			if (currentlyCompiledPriority.remove(compiler) != null) {
				lock.notifyAll();
			}
		}
	}

	/**
	 * Returns the compile priority the given Compiler currently operates in or <tt>null</tt>, if the compiler currently
	 * is not compiling.
	 *
	 * @param compiler the compiler for which to check the compilation priority
	 */
	@Nullable
	public Priority getCurrentCompilePriority(@NotNull Compiler compiler) {
		return currentlyCompiledPriority.get(compiler);
	}

	public void awaitCompilePriorityCompleted(@NotNull Compiler compiler, @NotNull Priority priority) throws InterruptedException {
		// do reduce overhead of this call, we do the first check outside of synchronization
		// (because we can as long as we don't need to wait)
		if (!shouldWait(compiler, priority)) return;

		synchronized (lock) {
			try {
				waitingCompilers.add(compiler);
				while (true) {
					if (!shouldWait(compiler, priority)) return;

					// deadlock detection: if all remaining compilers are waiting, throw an error
					// might be a bit to conservative, if a compiler uses multiple threads and only a subset of them is waiting, but we accept this for now
					if (waitingCompilers.containsAll(currentlyCompiledPriority.keySet())) {
						throw new InterruptedException("Deadlock detected, terminate waiting for compiler: " +
								compiler.getClass().getSimpleName() + " @priority: " + priority);
					}

					// in case we have a small CPU and only a few compile threads
					int threadCount = getMaxCompilationThreadCount();
					if (waitingCompilers.size() >= threadCount) {
						int newThreadCount = threadCount + 1;
						setMaxCompilationThreadCount(newThreadCount);
						Log.warning("All compile threads are occupied with waiting compilers, increasing thread count to " + newThreadCount + ".\n"
								+ "Consider using system property " + KNOWWE_COMPILER_THREADS_COUNT + " to set thread count to this number at startup.");
					}

					// otherwise wait for notification...
					lock.wait(1000);
				}
			}
			finally {
				waitingCompilers.remove(compiler);
			}
		}
	}

	private boolean shouldWait(@NotNull Compiler compiler, @NotNull Priority priority) {
		Priority current = currentlyCompiledPriority.get(compiler);
		// if the compiler is not in the map, it does not compile, so we do not wait
		//noinspection SimplifiableIfStatement
		if (current == null) return false;

		// check if priority is completed
		return current.intValue() <= priority.intValue();
	}

	/**
	 * Returns the unique id or count of the current compilation.
	 *
	 * @created 07.01.2014
	 */
	public int getCompilationId() {
		return this.compilationCount;
	}

	/**
	 * Returns if this compiler manager is currently compiling any changes. You may use {@link #awaitTermination()} or
	 * {@link #awaitTermination(long)} to wait for the compilation to complete.
	 *
	 * @return if a compilation is ongoing
	 * @created 30.10.2013
	 */
	public boolean isCompiling() {
		synchronized (lock) {
			return running != null;
		}
	}

	/**
	 * Returns if this compiler manager is currently compiling an article with the given title. You may use {@link
	 * #awaitTermination()} or {@link #awaitTermination(long)} to wait for the compilation to complete.
	 *
	 * @return if a compilation is ongoing
	 * @created 04.10.2016
	 */
	public boolean isCompiling(String title) {
		synchronized (lock) {
			return running != null && currentlyCompiledArticles.contains(title);
		}
	}

	/**
	 * Returns the priority-sorted list of compilers that are currently defined for the web this CompilerManager is
	 * created for.
	 *
	 * @return the currently defined compilers
	 * @created 31.10.2013
	 */
	public List<Compiler> getCompilers() {
		return Collections.unmodifiableList(compilers);
	}

	/**
	 * Adds a new compiler with the specific priority.
	 * <p/>
	 * Please note that it is allowed that compilers are added and removed while compiling the wiki. Usually a more
	 * prioritized compiler may add or remove sub-sequential Compilers depending on specific markups, e.g. defining a
	 * knowledge base or triple store for specific package combination to be compiled.
	 *
	 * @param priority the priority of the compiler
	 * @param compiler the instance to be added
	 * @created 31.10.2013
	 */
	public void addCompiler(double priority, Compiler compiler) {
		// debug code: check that we only add items
		// that not already have been added
		if (compilers.contains(compiler)) {
			throw new IllegalStateException("Do not add equal compilers instances multiple times.");
		}
		// add the compiler, being thread-save
		synchronized (lock) {
			compilers.add(priority, compiler);
			compilerCache.add(compiler);
			compiler.init(this);
		}
	}

	/**
	 * Removes an existing compiler with the specific priority.
	 * <p/>
	 * Please not that it is allowed that compilers are added and removed while compiling the wiki. Usually a more
	 * prioritized compiler may add or remove sub-sequential Compilers depending on specific markups, e.g. defining a
	 * knowledge base or triple store for specific package combination to be compiled.
	 *
	 * @param compiler the instance to be removed
	 * @created 31.10.2013
	 */
	public void removeCompiler(Compiler compiler) {
		// debug code: check that we only remove items
		// that already have been added
		if (!compilers.contains(compiler)) {
			throw new NoSuchElementException("Removing non-existing compiler instance.");
		}
		// remove the compiler, being thread-save
		synchronized (lock) {
			compilerCache.remove(compiler);
			compilers.remove(compiler);
		}
		Messages.clearMessages(compiler);
		EventManager.getInstance().fireEvent(new CompilerRemovedEvent(compiler));
		compiler.destroy();
	}

	public boolean contains(Compiler compiler) {
		synchronized (lock) {
			return compilerCache.contains(compiler);
		}
	}

	/**
	 * Blocks until all compilers have completed after a compile request, or the current thread is interrupted,
	 * whichever happens first. The method returns immediately if the compilers are currently idle (not compiling).
	 *
	 * @throws InterruptedException if interrupted while waiting
	 * @see #compile
	 */
	public void awaitTermination() throws InterruptedException {
		// repeatedly wait 10 seconds until all compiles have been completed
		//noinspection StatementWithEmptyBody
		while (!awaitTermination(10000)) {
		}
	}

	/**
	 * Blocks until all compilers have completed after a compile request, or the timeout occurs, or the current thread
	 * is interrupted, whichever happens first. The method returns immediately if the compilers are currently idle (not
	 * compiling).
	 *
	 * @param timeoutMilliSeconds the maximum time to wait
	 * @return <tt>true</tt> if the compilation has finished and <tt>false</tt>
	 * if the timeout elapsed before termination
	 * @throws InterruptedException if interrupted while waiting
	 * @see #compile
	 */
	public boolean awaitTermination(long timeoutMilliSeconds) throws InterruptedException {
		if (isCompileThread()) {
			Log.severe("Unable to wait for compilation to finish in a compile thread, because it would cause a deadlock.");
			return true;
		}
		long endTime = System.currentTimeMillis() + timeoutMilliSeconds;
		synchronized (lock) {
			while (true) {
				// if every compile has terminated within the specified time,
				// return true
				if (running == null) return true;

				// but reduce the timeout by the time the previous compiler has
				// required to complete, and stop if time has been elapsed.
				long remainingTime = endTime - System.currentTimeMillis();
				if (remainingTime <= 0) return false;
				lock.wait(remainingTime);
			}
		}
	}

	/**
	 * Convenience method which compiles the given sections. Since for now only one compilation operation can happen at
	 * the same time, this methods wait until the current operation finishes before it starts the next.
	 *
	 * @created 16.11.2013
	 */
	public void compile(List<Section<?>> added, List<Section<?>> removed) {
		while (!startCompile(added, removed)) {
			try {
				awaitTermination();
			}
			catch (InterruptedException e) {
				Log.warning("Caught InterruptedException while waiting to compile.", e);
			}
		}
	}
}
