/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package us.mn.state.dot.tms;

import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.utils.SString;

/**
 *  A DMS page time.
 *  @see MultiString, DmsPgTimeSpinner, SignMessageComposer
 *  @author Michael Darter
 */
public class DmsPgTime {

	/** minimum, maximum, and increment for on-time */
	public static final int MIN_ONTIME_TENTHS = 4;		// .4 secs
	public static final int MAX_ONTIME_TENTHS = 101;	// 10.1 secs
	public static final DmsPgTime MIN_ONTIME = 
		new DmsPgTime(MIN_ONTIME_TENTHS);
	public static final DmsPgTime MAX_ONTIME = 
		new DmsPgTime(MAX_ONTIME_TENTHS);

	/** page time */
	private final int m_tenths;

	/** constructor */
	public DmsPgTime(int tenths) {
		m_tenths = validateTenths(tenths);
	}

	/** constructor */
	public DmsPgTime(float secs) {
		m_tenths = validateTenths(secsToTenths(secs));
	}

	/** constructor */
	public DmsPgTime(double secs) {
		m_tenths = validateTenths(secsToTenths((float)secs));
	}

	/** Return the page time in tenths */
	public int toTenths() {
		return m_tenths;
	}

	/** Return the page time in seconds */
	public float toSecs() {
		return tenthsToSecs(m_tenths);
	}

	/** Return the page time in ms */
	public int toMs() {
		return m_tenths * 100;
	}

	/** Return the page time in tenths */
	public String toString() {
		return SString.intToString(m_tenths);
	}

	/** Validate page on-time, in tenths.
	 *  @param t Page time, in tenths. If this value is zero,
	 *	   the system default page on time is returned,
	 *         otherwise the minimum and maximum values are
	 *	   enforced. */
	public static int validateTenths(int t) {
		if(t == 0)
			return getDefaultOn().toTenths();
		t = (t < MIN_ONTIME_TENTHS ? MIN_ONTIME_TENTHS : t);
		t = (t > MAX_ONTIME_TENTHS ? MAX_ONTIME_TENTHS : t);
		return t;
	}

	/** Get default page on-time */
	public static DmsPgTime getDefaultOn() {
		return new DmsPgTime(secsToTenths(
			SystemAttrEnum.DMS_PAGE_ON_SECS.getFloat()));
	}

	/** Get default page off-time */
	public static DmsPgTime getDefaultOff() {
		return new DmsPgTime(secsToTenths(
			SystemAttrEnum.DMS_PAGE_OFF_SECS.getFloat()));
	}

	/** Convert from 10ths of a second to seconds */
	public static float tenthsToSecs(int tenths) {
		return (float)tenths / 10f;
	}

	/** Convert from seconds to 10ths. */
	public static int secsToTenths(float secs) {
		return (int)(secs * 10f);
	}

	/** Convert from seconds to 10ths. */
	public static int MsToTenths(int ms) {
		return (int)(ms / 100);
	}
}
