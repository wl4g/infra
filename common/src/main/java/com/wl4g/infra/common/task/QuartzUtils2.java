/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <jameswong1376@gmail.com>>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.infra.common.task;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.isTrueOf;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.DateUtils2.formatDate;
import static com.wl4g.infra.common.lang.Exceptions.getStackTraceAsString;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.SystemUtils.LINE_SEPARATOR;
import static org.quartz.TriggerKey.triggerKey;
import static org.quartz.TriggerUtils.computeFireTimes;
import static org.quartz.impl.StdSchedulerFactory.AUTO_GENERATE_INSTANCE_ID;
import static org.quartz.impl.StdSchedulerFactory.PROP_SCHED_INSTANCE_ID;
import static org.quartz.impl.StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME;
import static org.quartz.impl.StdSchedulerFactory.PROP_SCHED_JOB_FACTORY_CLASS;
import static org.quartz.impl.StdSchedulerFactory.PROP_THREAD_POOL_CLASS;
import static org.quartz.impl.StdSchedulerFactory.PROP_THREAD_POOL_PREFIX;

import java.text.ParseException;
import java.util.List;
import java.util.Properties;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.TriggerListener;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.simpl.PropertySettingJobFactory;
import org.quartz.simpl.SimpleThreadPool;
import org.quartz.spi.TriggerFiredBundle;

import lombok.CustomLog;

/**
 * Quartz task cron expression utility.
 * 
 * @author Wangl.sir
 * @version v1.0 2019年8月2日
 * @since
 */
public abstract class QuartzUtils2 {

    //
    // --- Tools. ---
    //

    /**
     * Check the expression is Valid
     */
    public static boolean isValidCron(String expression) {
        return CronExpression.isValidExpression(expression);
    }

    /**
     * Get the expression next numTimes -- run time
     */
    public static List<String> getNextExecTime(final @NotBlank String expression, final @NotNull int numTimes) {
        hasTextOf(expression, "expression");
        isTrueOf(numTimes > 0, "numTimes>0");
        final CronTriggerImpl cronTriggerImpl = new CronTriggerImpl();
        try {
            cronTriggerImpl.setCronExpression(expression);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
        return safeList(computeFireTimes(cronTriggerImpl, null, numTimes)).stream()
                .map(d -> formatDate(d, "yyyy-MM-dd HH:mm:ss"))
                .collect(toList());
    }

    public static JobDetail newDefaultJobDetail(final @NotNull String jobId, final @NotNull Class<? extends Job> jobClass) {
        hasTextOf(jobId, "jobId");
        notNullOf(jobClass, "jobClass");
        return JobBuilder.newJob()
                .ofType(jobClass)
                .storeDurably()
                .withIdentity(jobId)
                .withDescription(format("Executing for job %s ...", jobClass))
                .build();
    }

    public static Trigger newDefaultJobTrigger(
            final @NotNull String triggerId,
            final @NotBlank String cron,
            final boolean misfire,
            // final @NotNull JobDetail job,
            final @Nullable JobDataMap jobDataMap) {
        hasTextOf(triggerId, "triggerId");
        hasTextOf(cron, "cron");
        // notNullOf(job, "job");
        final CronScheduleBuilder schedule = CronScheduleBuilder.cronSchedule(cron);
        if (misfire) {
            schedule.withMisfireHandlingInstructionFireAndProceed();
        }
        return TriggerBuilder.newTrigger()
                // .forJob(job)
                .withSchedule(schedule)
                .withIdentity(triggerKey(triggerId, null))
                .usingJobData(isNull(jobDataMap) ? new JobDataMap() : jobDataMap)
                .withDescription(format("Trigging for %s", triggerId))
                .build();
    }

    public static Scheduler newDefaultScheduler(final @NotBlank String schedulerName) {
        return newScheduler(schedulerName, 1, null, null);
    }

    /**
     * Actual run at source see:
     * https://github1s.com/quartz-scheduler/quartz/blob/v2.3.2/quartz-core/src/main/java/org/quartz/core/QuartzSchedulerThread.java#L392-L393
     * 
     * @param schedulerName
     * @param threadPools
     * @param quartzProps
     * @param triggerListener
     * @return
     */
    public static Scheduler newScheduler(
            final @NotBlank String schedulerName,
            final @Min(1) int threadPools,
            final @Nullable Properties quartzProps,
            final @Nullable TriggerListener triggerListener) {
        hasTextOf(schedulerName, "schedulerName");
        isTrueOf(threadPools > 0, "threadPools > 0");
        try {
            final Properties props = new Properties(DEFAULT_QUARTZ_PROPS);
            props.put(PROP_SCHED_INSTANCE_NAME, schedulerName);
            if (nonNull(quartzProps)) {
                props.putAll(quartzProps);
            }

            final StdSchedulerFactory factory = new StdSchedulerFactory();
            factory.initialize(props);
            final Scheduler scheduler = factory.getScheduler();
            if (nonNull(triggerListener)) {
                scheduler.getListenerManager().addTriggerListener(triggerListener);
            }

            return scheduler;
        } catch (final SchedulerException e) {
            throw new IllegalStateException("Failed to create quartz scheduler.", e);
        }
    }

    //
    // --- Job CRUD. ---
    //

    public static JobDetail getJobDetail(final @NotNull Scheduler scheduler, final @NotBlank String jobId)
            throws SchedulerException {
        notNullOf(scheduler, "scheduler");
        hasTextOf(jobId, "jobId");
        return scheduler.getJobDetail(new JobKey(jobId));
    }

    public static void updateJob(final @NotNull Scheduler scheduler, final @NotBlank String jobId) throws SchedulerException {
        notNullOf(scheduler, "scheduler");
        hasTextOf(jobId, "jobId");
        final JobDetail jobDetail = scheduler.getJobDetail(new JobKey(jobId));
        scheduler.addJob(jobDetail, true, true);
    }

    public static boolean deleteJob(final @NotNull Scheduler scheduler, final @NotBlank String jobId) throws SchedulerException {
        notNullOf(scheduler, "scheduler");
        hasTextOf(jobId, "jobId");
        return scheduler.deleteJob(new JobKey(jobId));
    }

    public static void activateJob(final @NotNull Scheduler scheduler, final @NotBlank String triggerId)
            throws SchedulerException {
        notNullOf(scheduler, "scheduler");
        hasTextOf(triggerId, "triggerId");
        scheduler.resumeTrigger(new TriggerKey(triggerId));
    }

    public static void deactivateJob(final @NotNull Scheduler scheduler, final @NotBlank String triggerId)
            throws SchedulerException {
        notNullOf(scheduler, "scheduler");
        hasTextOf(triggerId, "triggerId");
        scheduler.pauseTrigger(new TriggerKey(triggerId));
    }

    public static final String KEY_SIMPLE_THREAD_POOL_COUNT = PROP_THREAD_POOL_PREFIX + ".threadCount";

    public static final Properties DEFAULT_QUARTZ_PROPS = new Properties() {
        private static final long serialVersionUID = 1L;
        {
            // see:https://github1s.com/quartz-scheduler/quartz/blob/v2.3.2/quartz-plugins/src/test/java/org/quartz/integrations/tests/AutoInterruptableJobTest.java#L90-L91
            put(PROP_THREAD_POOL_CLASS, SimpleThreadPool.class.getName());
            // see:https://github1s.com/quartz-scheduler/quartz/blob/v2.3.2/quartz-core/src/main/java/org/quartz/impl/StdSchedulerFactory.java#L673-L674
            // see:https://github1s.com/quartz-scheduler/quartz/blob/v2.3.2/quartz-core/src/main/java/org/quartz/core/JobRunShell.java#L127-L128
            put(PROP_SCHED_JOB_FACTORY_CLASS, CustomPropertySettingJobFactory.class.getName());
            put(PROP_SCHED_INSTANCE_ID, AUTO_GENERATE_INSTANCE_ID);
            // see:https://github1s.com/quartz-scheduler/quartz/blob/v2.3.2/quartz-core/src/main/java/org/quartz/impl/StdSchedulerFactory.java#L859-L860
            // see:https://github1s.com/quartz-scheduler/quartz/blob/v2.3.2/quartz-core/src/main/java/org/quartz/simpl/SimpleThreadPool.java#L147-L148
            put(KEY_SIMPLE_THREAD_POOL_COUNT, "1");
            put("org.quartz.jobStore.misfireThreshold", "1");
            // put("org.quartz.plugin.shutdownhook.cleanShutdown",Boolean.TRUE.toString());
            // put("org.quartz.plugin.shutdownhook.class",JobShutdownHookPlugin.class.getName());
        }
    };

    @CustomLog
    public static class CustomPropertySettingJobFactory extends PropertySettingJobFactory {
        @Override
        public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
            try {
                return super.newJob(bundle, scheduler);
            } catch (Exception e) {
                final String errmsg = format("Failed to new instantiate job for : %s", bundle.getJobDetail().getJobClass());
                log.error(errmsg, e);
                System.err.println(errmsg.concat(LINE_SEPARATOR).concat(getStackTraceAsString(e)));
                throw new IllegalStateException(errmsg, e);
            }
        }
    }

}