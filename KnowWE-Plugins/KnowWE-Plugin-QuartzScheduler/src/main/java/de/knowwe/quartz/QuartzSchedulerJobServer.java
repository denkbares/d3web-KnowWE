/*
 * Copyright (C) 2024 denkbares GmbH. All rights reserved.
 */

package de.knowwe.quartz;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Josua Nürnberger (Feanor GmbH)
 * @created 21.07.21
 */
@javax.servlet.annotation.WebListener
public class QuartzSchedulerJobServer implements ServletContextListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(QuartzSchedulerJobServer.class);

	private static QuartzSchedulerJobServer instance = null;
	private Map<JobKey, JobDetail> registeredJobs;
	private Scheduler scheduler;

	public static void triggerNow(JobDetail jobDetail) {
		try {
			instance.scheduler.triggerJob(jobDetail.getKey());
		}
		catch (SchedulerException e) {
			LOGGER.error("Can't trigger job " + jobDetail.getKey() + " now", e);
		}
	}

	public static boolean registerJob(JobDetail job, Trigger trigger) {
		LOGGER.info("Register quartz job " + job.getKey());
		Date date = null;
		boolean sucseed = false;

		// in case of testing, there might be no instance here
		if (instance == null) {
			LOGGER.warn(QuartzSchedulerJobServer.class.getSimpleName() + " has not been initialized. No job registered.");
			return false;
		}

		if (instance.registeredJobs.containsKey(job.getKey())) {
			try {
				if (instance.scheduler.checkExists(job.getKey())) {
					LOGGER.info("Delete quartz job " + job.getKey());
					boolean jobDeleted = instance.scheduler.deleteJob(job.getKey());
					if (jobDeleted) {
						instance.registeredJobs.put(job.getKey(), job);
						date = instance.scheduler.scheduleJob(job, trigger);
						sucseed = true;
					}
					else {
						instance.registeredJobs.remove(job.getKey());
						LOGGER.error("Can't delete job " + job.getKey() + " from scheduler");
					}
				}
			}
			catch (SchedulerException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		else {
			instance.registeredJobs.put(job.getKey(), job);
			try {
				date = instance.scheduler.scheduleJob(job, trigger);
				sucseed = true;
			}
			catch (SchedulerException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		if (sucseed) {
			LOGGER.info("Scheduled " + job.getKey() + " " + SimpleDateFormat.getInstance().format(date));
		}
		return sucseed;
	}

	public static void deleteJob(JobKey key) {
		if (instance.registeredJobs.containsKey(key)) {
			try {
				boolean deleted = instance.scheduler.deleteJob(key);
				if (deleted) {
					instance.registeredJobs.remove(key);
					LOGGER.info("Delete quartz job " + key);
				}
			}
			catch (SchedulerException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	public static List<? extends Trigger> getTriggerForJob(JobKey key) {
		if (instance.registeredJobs.containsKey(key)) {
			try {
				return instance.scheduler.getTriggersOfJob(key);
			}
			catch (SchedulerException e) {
				LOGGER.error("Can't read trigger for job " + key, e);
			}
		}
		return null;
	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		LOGGER.info("Initialize quartz scheduler");
		if (instance == null) {
			instance = this;
			registeredJobs = new ConcurrentHashMap<>();
			try {
				LOGGER.info("Start quartz scheduler");
				instance.scheduler = StdSchedulerFactory.getDefaultScheduler();
				instance.scheduler.start();
			}
			catch (SchedulerException e) {
				LOGGER.error("Error initializing scheduler", e);
			}
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		try {
			LOGGER.info("Shutdown quartz scheduler");
			instance.scheduler.shutdown();
		}
		catch (SchedulerException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
}
