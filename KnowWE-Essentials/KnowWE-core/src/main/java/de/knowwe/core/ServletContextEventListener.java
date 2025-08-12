package de.knowwe.core;

import java.util.ArrayList;
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

	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {
		EventManager.getInstance().fireEvent(new ServletContextInitializedEvent(servletContextEvent));
	}

	@Override
	public void contextDestroyed(final ServletContextEvent servletContextEvent) {
		destroyInProgress = true;
		LOGGER.info("Executing ContextDestroyedTask...");
		EventManager.getInstance().fireEvent(new ServletContextDestroyedEvent(servletContextEvent));
		contextDestroyedTask.forEach(task -> task.accept(servletContextEvent));
		LOGGER.info("Done executing ContextDestroyedTask, clearing...");
		contextDestroyedTask.clear();
		destroyInProgress = false;
	}
}
