package com.matchi.schedule

import org.joda.time.DateTime
import org.joda.time.Interval

class TimeSpan {
	DateTime start
	DateTime end

	public TimeSpan() {
	}
	
	public TimeSpan(DateTime start, DateTime end) {
		super();
		this.start = start;
		this.end = end;
	}	

	@Override
	public String toString() {
		return "TimeSpan [" + start.toString() + ", " + end.toString() + "]";
	}
	
	public boolean isWithin(TimeSpan other) {
		if((this.start.isAfter(other.start) || this.start.equals(other.start)) && (this.end.isBefore(other.end) || this.end.equals(other.end))) {
			return true
		}
		
		return false
	}

    public boolean intersects(TimeSpan other) {
        Interval gap = this.toInterval().gap(other.toInterval())
        if(gap == null) {
            return true
        } else {
            return false
        }
    }

    public Interval toInterval() {
        return new Interval(start, end.minusMillis(1))
    }

    public String getHourlyFormatted() {
        return getFormatted("HH","-")
    }

    public String getHourAndMinuteFormatted() {
        return getFormatted("HH:mm","-")
    }

    public String getFormatted(def format, def delimiter) {
        def result = this.start.toString(format) + delimiter + this.end.toString(format)

        return result
    }

    public String getStartTimeFormatted(def format) {
        return this.start.toString(format)
    }
}
