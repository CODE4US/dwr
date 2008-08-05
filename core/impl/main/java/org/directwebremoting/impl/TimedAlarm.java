/*
 * Copyright 2005 Joe Walker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.directwebremoting.impl;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.directwebremoting.extend.Alarm;
import org.directwebremoting.extend.Sleeper;
import org.directwebremoting.util.SharedObjects;

/**
 * An Alarm that goes off after a certain length of time.
 * @author Joe Walker [joe at getahead dot ltd dot uk]
 */
public class TimedAlarm implements Alarm
{
    /**
     * @param waitTime How long we wait before the Alarm goes off
     */
    public TimedAlarm(final Sleeper sleeper, long waitTime)
    {
        if (waitTime == 0)
        {
            sleeper.wakeUp();
        }
        else
        {
            Runnable runnable = new Runnable()
            {
                public void run()
                {
                    sleeper.wakeUp();
                }
            };

            ScheduledThreadPoolExecutor executor = SharedObjects.getScheduledThreadPoolExecutor();
            future = executor.schedule(runnable, waitTime, TimeUnit.MILLISECONDS);
        }
    }

    /* (non-Javadoc)
     * @see org.directwebremoting.extend.Alarm#cancel()
     */
    public void cancel()
    {
        if (future != null)
        {
            future.cancel(false);
        }
    }

    /**
     * The future result that allows us to cancel the timer
     */
    private ScheduledFuture<?> future;
}
