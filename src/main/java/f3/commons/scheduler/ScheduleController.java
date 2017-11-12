/*
 * Copyright (c) 2010-2017 fork2
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

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import f3.commons.reflection.MethodUtils;
import lombok.Getter;

/**
 * @author n3k0nation
 *
 */
public class ScheduleController<T> {
	
	@Getter private final T object;
	private final ScheduledExecutorService executor;
	private final Map<String, Future<?>> scheduled = new ConcurrentHashMap<>();
	public ScheduleController(T object, ScheduledExecutorService executor) {
		this.object = object;
		this.executor = executor;
	}
	
	public void schedule() {
		final List<Method> methods = MethodUtils.getAnnotatedMethods(object.getClass(), Schedule.class);
		for(int i = 0; i < methods.size(); i++) {
			final Method method = methods.get(i);
			method.setAccessible(true);
			if(method.getParameterCount() != 0) {
				continue;
			}
			
			final Schedule schedule = method.getAnnotation(Schedule.class);
			String key = schedule.name();
			if(key.isEmpty()) {
				key = method.getName();
			}
			
			final Future<?> future;
			if(schedule.dayOfWeek() != DayOfWeek.NONE) {
				future = scheduleAsWeekly(method, schedule, key);
			} else if(schedule.hourOfDay() >= 0 && schedule.hourOfDay() <= 23) {
				future = scheduleAsDaily(method, schedule, key);
			} else if(schedule.minuteOfHour() >= 0 && schedule.minuteOfHour() <= 59) {
				future = scheduleAsHourly(method, schedule, key);
			} else if(schedule.rate() > 0) {
				future = scheduleWithRate(method, schedule, key);
			} else if(schedule.delay() > 0) {
				future = scheduleWithDelay(method, schedule, key);
			} else {
				future = executor.schedule(new TaskWrap(method, schedule, key), schedule.delay(), schedule.delayUnit());
			}
			
			scheduled.put(key, future);
		}
	}
	
	public boolean cancelSpecified(String key) {
		final Future<?> future = scheduled.remove(key);
		if(future == null) {
			return false;
		}
		
		return future.cancel(false);
	}
	
	public void cancelAll() {
		for(Map.Entry<String, Future<?>> entry : scheduled.entrySet()) {
			scheduled.remove(entry.getKey());
			entry.getValue().cancel(false);
		}
	}
	
	private Future<?> scheduleWithDelay(Method method, Schedule schedule, String key) {
		return executor.scheduleWithFixedDelay(
				new TaskWrap(method, schedule, key), 
				schedule.initialDelay(), 
				schedule.delay(), 
				schedule.delayUnit()
		);
	}
	
	private Future<?> scheduleWithRate(Method method, Schedule schedule, String key) {
		return executor.scheduleAtFixedRate(
				new TaskWrap(method, schedule, key), 
				schedule.initialDelay(), 
				schedule.rate(), 
				schedule.delayUnit()
		);
	}
	
	private Future<?> scheduleAsHourly(Method method, Schedule schedule, String key) {
		final LocalDateTime now = LocalDateTime.now();
		LocalDateTime time = now.withMinute(schedule.minuteOfHour());
		
		if(time.isBefore(now)) {
			time = time.plusHours(1);
		}
		
		Duration duration = Duration.between(time, now);
		if(duration.isNegative()) {
			duration = duration.abs();
		}
		
		return executor.scheduleWithFixedDelay(
				new TaskWrap(method, schedule, key), 
				duration.toMillis(), 
				TimeUnit.HOURS.toMillis(1), 
				TimeUnit.MILLISECONDS
		);
	}
	
	private Future<?> scheduleAsDaily(Method method, Schedule schedule, String key) {
		final LocalDateTime now = LocalDateTime.now();
		LocalDateTime time = now.withHour(schedule.hourOfDay());
		
		if(schedule.minuteOfHour() >= 0 && schedule.minuteOfHour() <= 59) {
			time = time.withMinute(schedule.minuteOfHour());
		}
		
		if(time.isBefore(now)) {
			time = time.plusDays(1);
		}
		
		Duration duration = Duration.between(time, now);
		if(duration.isNegative()) {
			duration = duration.abs();
		}
		
		return executor.scheduleWithFixedDelay(
				new TaskWrap(method, schedule, key), 
				duration.toMillis(), 
				TimeUnit.DAYS.toMillis(1), 
				TimeUnit.MILLISECONDS
		);
	}
	
	private Future<?> scheduleAsWeekly(Method method, Schedule schedule, String key) {
		final LocalDateTime now = LocalDateTime.now();
		LocalDateTime time = now;
		
		if(schedule.hourOfDay() >= 0 && schedule.hourOfDay() <= 23) {
			time = time.withHour(schedule.hourOfDay());
		}
		
		if(schedule.minuteOfHour() >= 0 && schedule.minuteOfHour() <= 59) {
			time = time.withMinute(schedule.minuteOfHour());
		}
		
		time = now.with(ChronoField.DAY_OF_WEEK, schedule.dayOfWeek().ordinal());
		if(time.isBefore(now)) {
			time = time.plusWeeks(1);
		}
		
		Duration duration = Duration.between(time, now);
		if(duration.isNegative()) {
			duration = duration.abs();
		}
		
		return executor.scheduleWithFixedDelay(
				new TaskWrap(method, schedule, key), 
				duration.toMillis(), 
				TimeUnit.DAYS.toMillis(7), 
				TimeUnit.MILLISECONDS
		);
	}
	
	private class TaskWrap implements Runnable {
		final Method method;
		final Schedule schedule;
		final String key;
		int count;
		
		TaskWrap(Method method, Schedule schedule, String key) {
			this.method = method;
			this.schedule = schedule;
			this.key = key;
		}
		
		@Override
		public void run() {
			ReflectiveOperationException exception = null;
			try {
				method.invoke(object);
			} catch(ReflectiveOperationException e) {
				exception = e;
			}
			
			if(schedule.count() > 0 && ++count > schedule.count()) {
				final Future<?> task = scheduled.remove(key);
				if(task != null) {
					task.cancel(false);
				}
			}
			
			if(exception != null) {
				throw new RuntimeException("Failed to call scheduled method: " + 
						method.getDeclaringClass().getCanonicalName() + "::" + method.getName(), exception);
			}
		}
	}
	
}
