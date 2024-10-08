package org.apache.wiki.providers.autoUpdate;

import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.wiki.api.core.Engine;
import org.apache.wiki.providers.GitVersioningFileProvider;
import org.apache.wiki.util.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Josua Nürnberger (Feanor GmbH)
 * @created 21.08.20
 */
public class GitAutoUpdateScheduler {
	private static final Logger LOGGER = LoggerFactory.getLogger(GitAutoUpdateScheduler.class);

	private static final String JSPWIKI_GIT_AUTOUPDATE_INIT_DELAY = "jspwiki.git.autoupdate.initDelay";
	private static final String JSPWIKI_GIT_AUTOUPDATE_DELAY = "jspwiki.git.autoupdate.delay";
	private ScheduledExecutorService scheduler;
	private Engine engine;
	private TimerTask t;

	public GitAutoUpdateScheduler(){

	}

	public void initialize(Engine engine, GitVersioningFileProvider fileProvider){
		GitAutoUpdater updater = new GitAutoUpdater(engine, fileProvider);
		this.engine = engine;
		t = new TimerTask() {
			boolean running = false;
			@Override
			public void run() {
				if (running) {
					LOGGER.info("Skipping update wiki");
				}
				else {
					running = true;
					LOGGER.info("Start updating wiki");
					try {
						updater.update();
					} catch (Throwable t){
						LOGGER.error("Unexpected error while updating", t);
					}
					LOGGER.info("End updating wiki");
					running = false;
				}
			}
		};
		startScheduler(false);
	}

	public void shutdown(){
		this.scheduler.shutdown();
	}

	public void pauseAutoUpdate() {
		LOGGER.info("Auto update scheduler will be paused");
		if(this.scheduler != null && !this.scheduler.isShutdown()) {
			shutdown();
			try {
				this.scheduler.awaitTermination(60, TimeUnit.SECONDS);
			}
			catch (InterruptedException e) {
				LOGGER.warn("Await termination of auto update was interrupted");
			}
		}
		LOGGER.info("Auto update is paused");
	}

	public void resumeAutoUpdate() {
		LOGGER.info("Auto update will be resumed");
		startScheduler(true);
	}

	private void startScheduler(boolean resume) {
		scheduler = Executors.newSingleThreadScheduledExecutor();
		int startDelay = resume?0: TextUtil.getIntegerProperty(engine.getWikiProperties(), JSPWIKI_GIT_AUTOUPDATE_INIT_DELAY, 900);
		int delay = TextUtil.getIntegerProperty(engine.getWikiProperties(), JSPWIKI_GIT_AUTOUPDATE_DELAY, 5);
		scheduler.scheduleWithFixedDelay(t, startDelay, delay, TimeUnit.SECONDS);
	}
}
