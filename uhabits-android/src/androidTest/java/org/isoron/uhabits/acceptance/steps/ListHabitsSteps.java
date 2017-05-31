/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.acceptance.steps;

import android.support.test.espresso.*;
import android.support.test.uiautomator.*;
import android.view.*;

import org.hamcrest.*;
import org.isoron.uhabits.R;
import org.isoron.uhabits.activities.habits.list.views.*;

import java.util.*;

import static android.support.test.InstrumentationRegistry.*;
import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.isoron.uhabits.BaseUIAutomatorTest.device;
import static org.isoron.uhabits.acceptance.steps.CommonSteps.clickText;

public abstract class ListHabitsSteps
{
    public static void clickMenu(MenuItem item)
    {
        switch (item)
        {
            case ABOUT:
                clickTextInsideOverflowMenu(R.string.about);
                break;

            case HELP:
                clickTextInsideOverflowMenu(R.string.help);
                break;

            case SETTINGS:
                clickTextInsideOverflowMenu(R.string.settings);
                break;

            case CREATE_HABIT:
                clickViewWithId(R.id.actionAdd);
                break;

            case EDIT_HABIT:
                clickViewWithId(R.id.action_edit_habit);
                break;

            case DELETE:
                clickTextInsideOverflowMenu(R.string.delete);
                break;

            case ARCHIVE:
                clickTextInsideOverflowMenu(R.string.archive);
                break;

            case UNARCHIVE:
                clickTextInsideOverflowMenu(R.string.unarchive);
                break;

            case HIDE_ARCHIVED:
                clickViewWithId(R.id.action_filter);
                clickText(R.string.hide_archived);
                break;
        }
    }

    private static void clickTextInsideOverflowMenu(int id)
    {
        UiObject toolbar = device.findObject(
            new UiSelector().resourceId("org.isoron.uhabits:id/toolbar"));
        if(toolbar.exists())
        {
            onView(allOf(withContentDescription("More options"), withParent
                (withParent(withId(R.id.toolbar))))).perform(click());
        }
        else
        {
            openActionBarOverflowOrOptionsMenu(getTargetContext());
        }

        onView(withText(id)).perform(click());
    }

    private static void clickViewWithId(int id)
    {
        onView(withId(id)).perform(click());
    }

    private static ViewAction longClickEveryDescendantWithClass(Class cls)
    {
        return new ViewAction()
        {

            @Override
            public Matcher<View> getConstraints()
            {
                return isEnabled();
            }

            @Override
            public String getDescription()
            {
                return "perform on children";
            }

            @Override
            public void perform(UiController uiController, View view)
            {
                LinkedList<ViewGroup> stack = new LinkedList<>();
                if (view instanceof ViewGroup) stack.push((ViewGroup) view);

                while (!stack.isEmpty())
                {
                    ViewGroup vg = stack.pop();
                    for (int i = 0; i < vg.getChildCount(); i++)
                    {
                        View v = vg.getChildAt(i);
                        if (v instanceof ViewGroup) stack.push((ViewGroup) v);
                        if (cls.isInstance(v))
                        {
                            v.performLongClick();
                            uiController.loopMainThreadUntilIdle();
                        }
                    }
                }
            }
        };
    }

    public static void longPressCheckmarks(String habit)
    {
        CommonSteps.scrollToText(habit);
        onView(allOf(hasDescendant(withText(habit)),
            withClassName(endsWith("HabitCardView")))).perform(
            longClickEveryDescendantWithClass(CheckmarkButtonView.class));
    }

    public enum MenuItem
    {
        ABOUT, HELP, SETTINGS, EDIT_HABIT, DELETE, ARCHIVE, HIDE_ARCHIVED,
        UNARCHIVE, CREATE_HABIT
    }
}
