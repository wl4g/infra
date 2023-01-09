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

import java.util.Date;

import org.junit.Test;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.Trigger;

import com.wl4g.infra.common.lang.ThreadUtils2;

public class QuartzUtils2Tests {

    @Test
    public void testValidCron() {
        boolean validCron1 = QuartzUtils2.isValidCron("0/5 * * * * ?");
        System.out.println(validCron1);
        assert validCron1;

        boolean validCron2 = QuartzUtils2.isValidCron("999/999 * * * * ?");
        System.out.println(validCron2);
        assert !validCron2;
    }

    // https://github.com/sanjay035/Dynamic-Job-Scheduler/blob/main/src/main/java/com/scheduling/app/scheduler/JobScheduler.java
    @Test
    public void testSchedulerJobAndTrigger() throws Exception {
        final Scheduler scheduler = QuartzUtils2.newScheduler("testScheduler", 2, null, null);
        scheduler.start();

        final JobDetail jobDetail = QuartzUtils2.newDefaultJobDetail("myJobId", MyJob.class);
        final String cron = "0/1 * * * * ?";
        final Trigger jobTrigger = QuartzUtils2.newDefaultJobTrigger("myTriggerId", cron, false, null);

        // Actual run at source see:
        // https://github1s.com/quartz-scheduler/quartz/blob/v2.3.2/quartz-core/src/main/java/org/quartz/core/QuartzSchedulerThread.java#L392-L393
        final Date firstFireTime = scheduler.scheduleJob(jobDetail, jobTrigger);
        System.out.println("firstFireTime: " + firstFireTime);

        ThreadUtils2.sleep(5_000L);
        scheduler.shutdown();
    }

    public static class MyJob implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Executing my job ... " + context);
        }
    }

}