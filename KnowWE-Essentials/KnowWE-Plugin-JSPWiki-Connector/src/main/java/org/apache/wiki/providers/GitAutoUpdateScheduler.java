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
	private final ScheduledExecutorService scheduler;

	GitAutoUpdateScheduler(){
		scheduler = Executors.newSingleThreadScheduledExecutor();
	}

	public void initialize(WikiEngine engine, GitVersioningFileProvider fileProvider){
		GitAutoUpdater updater = new GitAutoUpdater(engine, fileProvider);
		TimerTask t = new TimerTask() {
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
		int startDelay = TextUtil.getIntegerProperty(engine.getWikiProperties(), JSPWIKI_GIT_AUTOUPDATE_INIT_DELAY, 900);
		scheduler.scheduleAtFixedRate(t, startDelay, 5, TimeUnit.SECONDS);
	}

	public void shutdown(){
		this.scheduler.shutdown();
	}
}
