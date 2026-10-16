package de.knowwe.core;

import java.util.ArrayList;
import de.knowwe.core.utils.KnowWEUtils;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.denkbares.events.EventManager;
import de.knowwe.event.ServletContextDestroyedEvent;
import de.knowwe.event.ServletContextInitializedEvent;

/**
 * Listens to servlet events and delegates them to the denkbares event mechanism. Also allows to register simple tasks
 * to be executed when servlet is destroyed.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 18.10.16
 */
public class ServletContextEventListener implements ServletContextListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServletContextEventListener.class);
	private static final List<Consumer<ServletContextEvent>> contextDestroyedTask = new ArrayList<>();
	private static boolean destroyInProgress = false;

	/**
	 * Register a task to be executed when the servlet is destroyed, e.g. tomcat shutdown or redeploy of the webapp.
	 * Use this for example to shut down long living threads.
	 *
	 * @param task the task to be executed when the servlet is destroyed.
	 */
	public static void registerOnContextDestroyedTask(final Consumer<ServletContextEvent> task) {
		contextDestroyedTask.add(task);
	}

	/**
	 * Returns whether we are currently in the progress of destroying this servlet context.
	 */
	public static boolean isDestroyInProgress() {
		return destroyInProgress;
	}

	/**
	 * The event manager of the environment running in the servlet context; before the environment exists (context
	 * initialization) or after it is gone, the global manager.
	 */
	private static EventManager eventManager(ServletContextEvent event) {
		Environment environment = Environment.findIn(event.getServletContext());
		return environment != null ? environment.getEventManager() : EventManager.getInstance();
	}

	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {
		eventManager(servletContextEvent).fireEvent(new ServletContextInitializedEvent(servletContextEvent));
	}

	@Override
	public void contextDestroyed(final ServletContextEvent servletContextEvent) {
		destroyInProgress = true;
		LOGGER.info("Executing ContextDestroyedTask...");
		eventManager(servletContextEvent).fireEvent(new ServletContextDestroyedEvent(servletContextEvent));
		contextDestroyedTask.forEach(task -> task.accept(servletContextEvent));
		LOGGER.info("Done executing ContextDestroyedTask, clearing...");
		contextDestroyedTask.clear();
		// the environment of this servlet context, published there when it was initialized
		Environment environment = Environment.findIn(servletContextEvent.getServletContext());
		if (environment != null) environment.close();
		destroyInProgress = false;
	}
}
