package de.knowwe.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.denkbares.events.EventManager;
import de.knowwe.event.ServletContextDestroyedEvent;

/**
 * Listens to servlet events and delegates them to the denkbares event mechanism. Also allows to register simple tasks
 * to be executed when servlet is destroyed.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 18.10.16
 */
public class ServletContextEventListener implements ServletContextListener {

	private static final List<Consumer<ServletContextEvent>> contextDestroyedTask = new ArrayList<>();

	private static boolean destroyInProgress = false;

	/**
	 * Register a task to be executed when the servlet is destroyed, e.g. tomcat shutdown or redeploy of the webapp.
	 * Use this for example to shut down long living threads.
	 *
	 * @param task the task to be executed when the servlet is destroyed.
	 */
	public static void registerOnContextDestroyedTask(Consumer<ServletContextEvent> task) {
		contextDestroyedTask.add(task);
	}

	/**
	 * Returns whether we are currently in the progress of destroying this servlet context.
	 */
	public static boolean isDestroyInProgress() {
		return destroyInProgress;
	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		// no event listeners will be registered at this point, so no point in firing and event
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		destroyInProgress = true;
		EventManager.getInstance().fireEvent(new ServletContextDestroyedEvent(servletContextEvent));
		contextDestroyedTask.forEach(task -> task.accept(servletContextEvent));
	}
}
