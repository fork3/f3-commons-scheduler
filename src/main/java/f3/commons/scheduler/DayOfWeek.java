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

import javax.annotation.Nullable;

/**
 * @author n3k0nation
 *
 */
public enum DayOfWeek {
   /**
    * The singleton instance for the day-of-week of Monday.
    * This has the numeric value of {@code 1}.
    */
   MONDAY,
   /**
    * The singleton instance for the day-of-week of Tuesday.
    * This has the numeric value of {@code 2}.
    */
   TUESDAY,
   /**
    * The singleton instance for the day-of-week of Wednesday.
    * This has the numeric value of {@code 3}.
    */
   WEDNESDAY,
   /**
    * The singleton instance for the day-of-week of Thursday.
    * This has the numeric value of {@code 4}.
    */
   THURSDAY,
   /**
    * The singleton instance for the day-of-week of Friday.
    * This has the numeric value of {@code 5}.
    */
   FRIDAY,
   /**
    * The singleton instance for the day-of-week of Saturday.
    * This has the numeric value of {@code 6}.
    */
   SATURDAY,
   /**
    * The singleton instance for the day-of-week of Sunday.
    * This has the numeric value of {@code 7}.
    */
   SUNDAY,
   
   NONE;
	
	public @Nullable java.time.DayOfWeek toJavaTime() {
		if(this == NONE) {
			return null;
		}
		
		return java.time.DayOfWeek.values()[ordinal()];
	}
}
