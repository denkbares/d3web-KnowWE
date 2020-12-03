package org.apache.wiki.providers;

import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.util.TextUtil;

import com.denkbares.utils.Log;

/**
 * @author Josua Nürnberger (Feanor GmbH)
 * @created 21.08.20
 */
public class GitAutoUpdateScheduler {

	private static final Logger log = Logger.getLogger(GitAutoUpdateScheduler.class);
	private static final String JSPWIKI_GIT_AUTOUPDATE_INIT_DELAY = "jspwiki.git.autoupdate.initDelay";
	private static final String JSPWIKI_GIT_AUTOUPDATE_DELAY = "jspwiki.git.autoupdate.delay";
	private ScheduledExecutorService scheduler;
	private WikiEngine engine;
	private TimerTask t;

	GitAutoUpdateScheduler(){

	}

	public void initialize(WikiEngine engine, GitVersioningFileProvider fileProvider){
		GitAutoUpdater updater = new GitAutoUpdater(engine, fileProvider);
		this.engine = engine;
		t = new TimerTask() {
			boolean running = false;
			@Override
			public void run() {
				if (running) {
					Log.info("Skipping update wiki");
				}
				else {
					running = true;
					Log.info("Start updating wiki");
					try {
						updater.update();
					} catch (Throwable t){
						log.error("Unexpected error while updating", t);
					}
					Log.info("End updating wiki");
					running = false;
				}
			}
		};
		startScheduler();
	}

	public void shutdown(){
		this.scheduler.shutdown();
	}

	public void pauseAutoUpdate() {
		log.info("Auto update scheduler will be paused");
		if(this.scheduler != null && !this.scheduler.isShutdown()) {
			shutdown();
			try {
				this.scheduler.awaitTermination(60, TimeUnit.SECONDS);
			}
			catch (InterruptedException e) {
				log.warn("Await termination of auto update was interrupted");
			}
		}
		log.info("Auto update is paused");
	}

	public void resumeAutoUpdate() {
		log.info("Auto update will be resumed");
		startScheduler();
	}

	private void startScheduler() {
		scheduler = Executors.newSingleThreadScheduledExecutor();
		int startDelay = TextUtil.getIntegerProperty(engine.getWikiProperties(), JSPWIKI_GIT_AUTOUPDATE_INIT_DELAY, 900);
		int delay = TextUtil.getIntegerProperty(engine.getWikiProperties(), JSPWIKI_GIT_AUTOUPDATE_DELAY, 5);
		scheduler.scheduleAtFixedRate(t, startDelay, delay, TimeUnit.SECONDS);
	}
}
