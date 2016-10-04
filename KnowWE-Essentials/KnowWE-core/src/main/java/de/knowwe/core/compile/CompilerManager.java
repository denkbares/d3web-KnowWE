package de.knowwe.core.compile;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.denkbares.collections.PriorityList;
import com.denkbares.collections.PriorityList.Group;
import com.denkbares.events.EventManager;
import com.denkbares.utils.Log;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Messages;

/**
 * This class represents the compile manager for a specific
 * {@link ArticleManager}. It is responsible to manage every compile process for
 * all articles and section of the {@link ArticleManager}. Therefore all compile
 * code has been removed out of sections and articles and placed here.
 * <p/>
 * The compile manager holds a set of compilers. The compilers can be plugged
 * into the manager using the defined extension point. Each compiler may
 * implement its own compilation procedure. If the compiler uses the package
 * mechanism to define certain compiling bundles (such as d3web does for
 * knowledge bases or owl for triple stores) the compiler usually have multiple
 * subsequent compilers for each such individual bundle.
 * <p/>
 * To enhance performance, each compiler top level compiles individually, maybe
 * in parallel. Nevertheless, if the compilers have different priorities
 * (defined through the compiler's extension), they are ordered by these
 * priorities. Only compilers with same priority may compile in parallel.
 * <p/>
 */
public class CompilerManager {

	private static final Map<Class<? extends Compiler>, ScriptManager<? extends Compiler>> scriptManagers = new HashMap<>();
	private int compilationCount = 0;

	private final PriorityList<Double, Compiler> compilers;
	// just a fast cache for the contains() method
	private final HashSet<Compiler> compilerCache;
	private final ArticleManager articleManager;
	private Iterator<Group<Double, Compiler>> running = null;
	private final ExecutorService threadPool;
	private final Object lock = new Object();
	private final Object dummy = new Object();
	private static final Map<Thread, Object> compileThreads = Collections.synchronizedMap(new WeakHashMap<>());
	private static final ConcurrentHashMap<String, Object> currentlyCompiledArticles = new ConcurrentHashMap<>();

	public CompilerManager(ArticleManager articleManager) {
		this.articleManager = articleManager;
		this.compilerCache = new HashSet<>();
		this.compilers = new PriorityList<>(5d);
		this.threadPool = createExecutorService();
	}

	/**
	 * Checks whether the current thread is created by a CompilerManager to compile articles.
	 */
	public static boolean isCompileThread() {
		return compileThreads.containsKey(Thread.currentThread());
	}

	static ExecutorService createExecutorService() {
		// we need at least to threads here, because one thread is used to start compilation
		// and the rest for compiling the individual compilers
		int threadCount = Runtime.getRuntime().availableProcessors() + 1;
		ExecutorService pool = Executors.newFixedThreadPool(threadCount, runnable -> {
			Thread thread = new Thread(runnable, "KnowWE-Compiler");
			compileThreads.put(thread, null);
			return thread;
		});
		Log.fine("Created multi core thread pool of size " + threadCount);
		return pool;
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
	 * Starts the compilation based on a specified set of changing sections. The
	 * method returns true if the compilation can be started. The method returns
	 * false if the request is ignored, e.g. because of an already ongoing
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
			long startTime = System.currentTimeMillis();
			try {
				EventManager.getInstance().fireEvent(new CompilationStartEvent(CompilerManager.this));
				doCompile(added, removed);
			}
			catch (Throwable e) {
				Log.severe("Unexpected internal error while starting compilation.", e);
			}
			finally {
				synchronized (lock) {
					running = null;
					currentlyCompiledArticles.clear();
					Log.info("Compiled " + added.size() + " added and " + removed.size()
							+ " removed section" + (removed.size() == 1 ? "" : "s")
							+ " after " + (System.currentTimeMillis() - startTime)
							+ "ms");
					lock.notifyAll();
				}
				EventManager.getInstance().fireEvent(new CompilationFinishedEvent(CompilerManager.this));
			}
		});
		return true;
	}

	private void setCompiling(Collection<Section<?>> sections) {
		for (Section<?> section : sections) {
			String title = section.getTitle();
			if (title == null) continue;
			currentlyCompiledArticles.put(title, dummy);
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

			for (final Compiler compiler : simultaneousCompilers) {
				// wait until we are allowed to compile
				threadPool.execute(() -> {

					long startTime = System.currentTimeMillis();
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
							activeCompilers.remove(compiler);
							Log.fine(compiler.getClass().getSimpleName()
									+ " finished after "
									+ (System.currentTimeMillis() - startTime) + "ms");
							// 2 - notify the waiting caller of doCompile() in the synchronized block below (1)
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

	/**
	 * Returns the unique id or count of the current compilation.
	 *
	 * @created 07.01.2014
	 */
	public int getCompilationId() {
		return this.compilationCount;
	}

	/**
	 * Returns if this compiler manager is currently compiling any changes. You
	 * may use {@link #awaitTermination()} or {@link #awaitTermination(long)} to
	 * wait for the compilation to complete.
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
	 * Returns if this compiler manager is currently compiling an article with the given title. You
	 * may use {@link #awaitTermination()} or {@link #awaitTermination(long)} to
	 * wait for the compilation to complete.
	 *
	 * @return if a compilation is ongoing
	 * @created 04.10.2016
	 */
	public boolean isCompiling(String title) {
		synchronized (lock) {
			return running != null && currentlyCompiledArticles.containsKey(title);
		}
	}

	/**
	 * Returns the priority-sorted list of compilers that are currently defined
	 * for the web this CompilerManager is created for.
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
	 * Please note that it is allowed that compilers are added and removed while
	 * compiling the wiki. Usually a more prioritized compiler may add or remove
	 * sub-sequential Compilers depending on specific markups, e.g. defining a
	 * knowledge base or triple store for specific package combination to be
	 * compiled.
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
	 * Please not that it is allowed that compilers are added and removed while
	 * compiling the wiki. Usually a more prioritized compiler may add or remove
	 * sub-sequential Compilers depending on specific markups, e.g. defining a
	 * knowledge base or triple store for specific package combination to be
	 * compiled.
	 *
	 * @param compiler the instance to be removed
	 * @created 31.10.2013
	 */
	public void removeCompiler(Compiler compiler) {
		// debug code: check that we only remove items
		// that already have been added
		if (!compilers.contains(compiler)) {
			throw new NoSuchElementException("Removeing non-exisitng compiler instance.");
		}
		// remove the compiler, being thread-save
		synchronized (lock) {
			compilerCache.remove(compiler);
			compilers.remove(compiler);
		}
		EventManager.getInstance().fireEvent(new CompilerRemovedEvent(compiler));
		compiler.destroy();
	}

	public boolean contains(Compiler compiler) {
		synchronized (lock) {
			return compilerCache.contains(compiler);
		}
	}

	/**
	 * Blocks until all compilers have completed after a compile request, or the
	 * current thread is interrupted, whichever happens first. The method
	 * returns immediately if the compilers are currently idle (not compiling).
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
	 * Blocks until all compilers have completed after a compile request, or the
	 * timeout occurs, or the current thread is interrupted, whichever happens
	 * first. The method returns immediately if the compilers are currently idle
	 * (not compiling).
	 *
	 * @param timeoutMilliSeconds the maximum time to wait
	 * @return <tt>true</tt> if the compilation has finished and <tt>false</tt>
	 * if the timeout elapsed before termination
	 * @throws InterruptedException if interrupted while waiting
	 * @see #compile
	 */
	public boolean awaitTermination(long timeoutMilliSeconds) throws InterruptedException {
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
	 * Convenience method which compiles the given sections. Since for now only
	 * one compilation operation can happen at the same time, this methods wait
	 * until the current operation finishes before it starts the next.
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
