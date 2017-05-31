/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits;

import android.appwidget.*;
import android.content.*;
import android.content.res.*;
import android.os.*;
import android.support.annotation.*;
import android.support.test.*;
import android.support.test.filters.*;
import android.util.*;

import junit.framework.*;

import org.isoron.androidbase.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.tasks.*;
import org.isoron.uhabits.core.utils.*;
import org.isoron.uhabits.preferences.*;
import org.isoron.uhabits.utils.*;
import org.junit.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

@MediumTest
public class BaseAndroidTest extends TestCase
{
    // 8:00am, January 25th, 2015 (UTC)
    public static final long FIXED_LOCAL_TIME = 1422172800000L;

    protected Context testContext;

    protected Context targetContext;

    protected AndroidPreferences prefs;

    protected HabitList habitList;

    protected TaskRunner taskRunner;

    protected HabitLogger logger;

    protected HabitFixtures fixtures;

    protected CountDownLatch latch;

    protected AndroidTestComponent component;

    protected ModelFactory modelFactory;

    protected BaseSystem baseSystem;

    @Override
    @Before
    public void setUp()
    {
        if (Looper.myLooper() == null) Looper.prepare();

        targetContext = InstrumentationRegistry.getTargetContext();
        testContext = InstrumentationRegistry.getContext();

        DateUtils.setFixedLocalTime(FIXED_LOCAL_TIME);
        setResolution(2.0f);
        setTheme(R.style.AppBaseTheme);
        setLocale("en", "US");

        component = DaggerAndroidTestComponent
            .builder()
            .appModule(new AppModule(targetContext.getApplicationContext()))
            .build();

        baseSystem = new BaseSystem(targetContext);

        HabitsApplication.setComponent(component);
        prefs = component.getPreferences();
        habitList = component.getHabitList();
        taskRunner = component.getTaskRunner();
        logger = component.getHabitsLogger();

        modelFactory = component.getModelFactory();
        fixtures = new HabitFixtures(modelFactory, habitList);

        latch = new CountDownLatch(1);
    }

    protected void assertWidgetProviderIsInstalled(Class componentClass)
    {
        ComponentName provider =
            new ComponentName(targetContext, componentClass);
        AppWidgetManager manager = AppWidgetManager.getInstance(targetContext);

        List<ComponentName> installedProviders = new LinkedList<>();
        for (AppWidgetProviderInfo info : manager.getInstalledProviders())
            installedProviders.add(info.provider);

        assertThat(installedProviders, hasItems(provider));
    }

    protected void awaitLatch() throws InterruptedException
    {
        assertTrue(latch.await(60, TimeUnit.SECONDS));
    }

    protected void setLocale(@NonNull String language, @NonNull String country)
    {
        Locale locale = new Locale(language, country);
        Locale.setDefault(locale);
        Resources res = targetContext.getResources();
        Configuration config = res.getConfiguration();
        config.setLocale(locale);
    }

    protected void setResolution(float r)
    {
        DisplayMetrics dm = targetContext.getResources().getDisplayMetrics();
        dm.density = r;
        dm.scaledDensity = r;
        InterfaceUtils.setFixedResolution(r);
    }

    protected void setTheme(@StyleRes int themeId)
    {
        targetContext.setTheme(themeId);
        StyledResources.setFixedTheme(themeId);
    }

    protected void sleep(int time)
    {
        try
        {
            Thread.sleep(time);
        }
        catch (InterruptedException e)
        {
            fail();
        }
    }

    public long timestamp(int year, int month, int day)
    {
        GregorianCalendar cal = DateUtils.getStartOfTodayCalendar();
        cal.set(year, month, day);
        return cal.getTimeInMillis();
    }

    protected void startTracing()
    {
        File dir = baseSystem.getFilesDir("Profile");
        assertNotNull(dir);
        String tracePath = dir.getAbsolutePath() + "/performance.trace";
        Log.d("PerformanceTest", String.format("Saving trace file to %s", tracePath));
        Debug.startMethodTracingSampling(tracePath, 0, 1000);
    }

    protected void stopTracing()
    {
        Debug.stopMethodTracing();
    }
}
