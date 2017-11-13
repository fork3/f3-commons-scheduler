/*
 * Copyright (c) 2010-2017 fork3
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES 
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR 
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package f3.commons.scheduler;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Execute the annotated method with/without delay/period/day of week/hour of day/minute of hour.
 * @author n3k0nation
 *
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Schedule {
	
	/** Task name. 
	 * If task name is empty scheduler get method name as task name. 
	 * If task name collide with other task name scheduler stop previous task with that name. */
	String name() default "";
	
	/** Method execute repeat count. */
	int count() default 0;
	
	
	// classical pool schedule params //
	
	/** Number of time units to delay before first execution of delay() or rate() task. 
	 * If initial delay is less or equals of zero: execute method with zero-delay.
	 * If delay() and rate() is invalid method execute once with initial delay */
	long initialDelay() default 0l;
	
	/** Execute method with fixed period between end of last invocation and start of next.
	 * If delay() is valid and rate() is valid - method execute as fixed rate delay (rate() priority is high than delay()) */
	long delay() default 0l;
	
	/** Execute method with fixed period between invocations.
	 * If delay() is valid and rate() is valid - method execute as fixed rate delay (rate() priority is high than delay()) */
	long rate() default 0l;
	
	TimeUnit delayUnit() default TimeUnit.MILLISECONDS;
	
	
	// day of week scheduling //
	
	/** Execute method in fixed day of week.
	 * If dayOfWeek set in any value exclude NONE and:
	 * <li> hourOfDay not set or value less than 0 or great than 23 - scheduler auto-setup hourOfDay to current hour.
	 * <li> minuteOfHour not set or value less than 0 or great than 59 - scheduler auto-setup minuteOfHour to current minute */
	DayOfWeek dayOfWeek() default DayOfWeek.NONE;
	
	/** Execute method each day in fixed hour of day. 
	 * If hourOfDay greater than 0 and less than 23:
	 * <li> minuteOfHour not set or value less than 0 or great than 59 - scheduler auto-setup minuteOfHour to current minute */
	int hourOfDay() default -1;
	
	/** Execute method each hour in fixed minute of hour */
	int minuteOfHour() default -1;
	
	
	// cron //
	
	//String cron() default "";
}
