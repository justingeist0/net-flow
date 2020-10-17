package com.fantasma.netflow;

import android.content.Context;

import com.fantasma.netflow.util.DataAtTimeFrames;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("com.fantasma.netflow", appContext.getPackageName());
    }

    private Integer[] getWeekStart(Integer[] date, Integer[] start) {
        if(!dayWithinWeek(date, start)) {
            start[2] -= 7;
            if(start[2] <= 0) {
                start[1]--;
                if(start[1] < 1) {
                    start[0]--;
                    start[1] = 12;
                }
                start[2] = DataAtTimeFrames.Companion
                        .getAmountOfDaysInMonth(start[0], start[1]) + start[2];
            }
            steps++;
            getWeekStart(date, start);
        }
        return start;
    }
}
