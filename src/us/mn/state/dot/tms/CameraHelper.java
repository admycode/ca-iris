/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
 * Copyright (C) 2014-2015  AHMCT, University of California
 * Copyright (C) 2016       California Department of Transportation
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.geo.Position;
import us.mn.state.dot.tms.units.Distance;
import us.mn.state.dot.tms.utils.GPSutil;
import us.mn.state.dot.tms.utils.twilight.Sun;
import us.mn.state.dot.tms.utils.twilight.Time;

import static us.mn.state.dot.tms.PresetAliasName.HOME;
import static us.mn.state.dot.tms.PresetAliasName.NIGHT_SHIFT;
import static us.mn.state.dot.tms.SystemAttrEnum.CAMERA_SHIFT_CONCUR_MOVE;
import static us.mn.state.dot.tms.SystemAttrEnum.CAMERA_SHIFT_MOVE_PAUSE;
import static us.mn.state.dot.tms.SystemAttrEnum.CAMERA_SHIFT_REINIT;
import static us.mn.state.dot.tms.SystemAttrEnum.CAMERA_SHIFT_SUNRISE_OFFSET;
import static us.mn.state.dot.tms.SystemAttrEnum.CAMERA_SHIFT_SUNSET_OFFSET;

/**
 * Helper class for cameras.
 *
 * @author Douglas Lau
 * @author Travis Swanston
 * @uahtor Jacob Barde
 */
public class CameraHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private CameraHelper() {
		assert false;
	}

	/** Lookup the camera with the specified name */
	static public Camera lookup(String name) {
		return (Camera) namespace.lookupObject(Camera.SONAR_TYPE,
			name);
	}

	/** Get a camera iterator */
	static public Iterator<Camera> iterator() {
		return new IteratorWrapper<Camera>(namespace.iterator(
			Camera.SONAR_TYPE));
	}

	/** Get the encoder type for a camera */
	static public EncoderType getEncoderType(Camera cam) {
		return EncoderType.fromOrdinal(cam.getEncoderType());
	}

	/** Get the host ip for the camera's encoder */
	static public String parseEncoderIp(Camera cam) {
		String enc = cam.getEncoder();
		if(enc != null && enc.indexOf(':') >= 0)
			return enc.substring(0, enc.indexOf(':'));
		else
			return enc.trim();
	}

	/** Find the nearest cameras to a position */
	static public Collection<Camera> findNearest(Position pos, int n_count){
		TreeMap<Double, Camera> cams = new TreeMap<Double, Camera>();
		Iterator<Camera> it = iterator();
		while(it.hasNext()) {
			Camera cam = it.next();
			GeoLoc loc = cam.getGeoLoc();
			Distance d = GeoLocHelper.distanceTo(loc, pos);
			if(d != null) {
				cams.put(d.m(), cam);
				while(cams.size() > n_count)
					cams.pollLastEntry();
			}
		}
		return cams.values();
	}

	/** Find a camera with the specific UID */
	static public Camera findUID(String uid) {
		Integer id = parseUID(uid);
		if (id != null) {
			Iterator<Camera> it = iterator();
			while (it.hasNext()) {
				Camera cam = it.next();
				Integer cid = parseUID(cam.getName());
				if (id.equals(cid))
					return cam;
			}
		}
		return null;
	}

	/** Parse the integer ID of a camera */
	static public Integer parseUID(String uid) {
		String id = stripNonDigitPrefix(uid);
		try {
			return Integer.parseInt(id);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	/** Strip non-digit prefix from a string */
	static private String stripNonDigitPrefix(String v) {
		int i = 0;
		for (i = 0; i < v.length(); i++) {
			if (Character.isDigit(v.charAt(i)))
				break;
		}
		return v.substring(i);
	}

	/**
	 * Retrieve a list of cameras with both day and night-shift preset aliases enabled
	 * @return
	 */
	static public List<Camera> getCamerasForShift() {
		List<Camera> rv = new ArrayList<>();
		Iterator<Camera> it = iterator();
		while (it.hasNext()) {
			Camera cam = it.next();
			if (PresetAliasHelper.hasShiftPreset(cam, PresetAliasName.HOME)
				&& PresetAliasHelper.hasShiftPreset(cam, PresetAliasName.NIGHT_SHIFT))
				rv.add(cam);
		}
		return rv;
	}

	/** return the geographical center of all cameras */
	static public Position getGeographicCenter() {
		List<Position> pl = new ArrayList<>();
		double lat = 0.0;
		double lon = 0.0;

		Iterator<Camera> it = iterator();
		while (it.hasNext()) {
			Camera cam = it.next();
			if (cam != null && cam.getGeoLoc() != null
				&& cam.getGeoLoc().getLat() != null
				&& cam.getGeoLoc().getLon() != null) {
				lat = cam.getGeoLoc().getLat();
				lon = cam.getGeoLoc().getLon();
				pl.add(new Position(lat, lon));
			}
		}

		return GPSutil.getGeographicCenter(pl);
	}

	/** get the shift reinit system attribute */
	static public boolean isShiftReinit() {
		return CAMERA_SHIFT_REINIT.getBoolean();
	}
	/** get the shift pause system attribute */
	static public int getShiftPause() {
		return CAMERA_SHIFT_MOVE_PAUSE.getInt();
	}

	/** get the shift concurrent movements system attribute */
	static public int getConcurrentMovements() {
		return CAMERA_SHIFT_CONCUR_MOVE.getInt();
	}

	/** get sunrise offset system attribute */
	static public int getSunriseOffset() {
		return CAMERA_SHIFT_SUNRISE_OFFSET.getInt();
	}

	/** get sunset offset system attribute */
	static public int getSunsetOffset() {
		return CAMERA_SHIFT_SUNSET_OFFSET.getInt();
	}

	/**
	 * calculate what the last shift was.
	 * @param offset offset in millis from now to calculate this for.
	 *               should always be 0. only server start-up should use any
	 *               other value.
	 */
	static public PresetAliasName calculateLastShift(int offset) {
		PresetAliasName rv = HOME; // default
		GregorianCalendar today = (GregorianCalendar) TimeSteward.getCalendarInstance();
		today.setTimeInMillis((today.getTimeInMillis() + offset));

		GregorianCalendar nightshift = (GregorianCalendar) getShiftTime(NIGHT_SHIFT, 0);
		GregorianCalendar dayshift = (GregorianCalendar) getShiftTime(HOME, 0);

		if ((nightshift != null && dayshift != null) && ((today.getTimeInMillis() > nightshift.getTimeInMillis()
			|| today.getTimeInMillis() < dayshift.getTimeInMillis())))
			rv = NIGHT_SHIFT;

		return rv;
	}

	/**
	 * calculate what the next shift is.
	 * @param offset offset in millis from now to calculate this for.
	 *               should always be 0. only server start-up should use any
	 *               other value.
	 */
	static public PresetAliasName calculateNextShift(int offset) {
		GregorianCalendar now = (GregorianCalendar) TimeSteward.getCalendarInstance();
		now.setTimeInMillis((now.getTimeInMillis() + offset));

		GregorianCalendar dayshift = (GregorianCalendar) getShiftTime(HOME, 0);
		GregorianCalendar nightshift = (GregorianCalendar) getShiftTime(NIGHT_SHIFT, 0);

		if (dayshift == null || nightshift == null)
			return HOME;

		if (now.getTimeInMillis() < dayshift.getTimeInMillis())
			return HOME;
		if (now.getTimeInMillis() < nightshift.getTimeInMillis())
			return NIGHT_SHIFT;

		return HOME;
	}

	/** calculate the time of the next shift
	 * @param offset offset in millis from now to calculate from.
	 *               if current time is a shift time, an offset of 1 or more minutes would be advisable.
	 */
	static public Calendar calculateNextShiftTime(int offset) {
		GregorianCalendar now = (GregorianCalendar) TimeSteward.getCalendarInstance();
		now.setTimeInMillis((now.getTimeInMillis() + offset));
		GregorianCalendar todayshift = (GregorianCalendar) getShiftTime(HOME, 0);
		GregorianCalendar tonightshift = (GregorianCalendar) getShiftTime(NIGHT_SHIFT, 0);
		GregorianCalendar tomorrowdawn = (GregorianCalendar) getShiftTime(HOME, 1);

		if (todayshift == null || tonightshift == null || tomorrowdawn == null)
			return null;

		if (now.getTimeInMillis() < todayshift.getTimeInMillis())
			return todayshift;
		if (now.getTimeInMillis() < tonightshift.getTimeInMillis())
			return tonightshift;
		if (now.getTimeInMillis() < tomorrowdawn.getTimeInMillis())
			return tomorrowdawn;

		return null;
	}

	/**
	 * calculate the shift time
	 * @param pan       preset alias name. HOME for dayshift, NIGHT_SHIFT for nightshift
	 * @param dayOffset day offset. -1 for yesterday, 0 for today, 1 for tomorrow. other values discarded, 0 is used
	 *
	 * @return
	 */
	static public Calendar getShiftTime(PresetAliasName pan, int dayOffset) {

		int off = dayOffset;
		if (dayOffset < -1 || dayOffset > 1)
			off = 0;
		GregorianCalendar di = (GregorianCalendar) TimeSteward.getCalendarInstance();
		di.add(Calendar.DAY_OF_MONTH, off);

		if (off != 0) {
			/* if there is a day offset, set date instance time to 03:01 (3:01 AM) to account for possible
			 * Daylight Savings Time changes. */
			di.set(Calendar.HOUR_OF_DAY, 3);
			di.set(Calendar.MINUTE, 1);
			di.set(Calendar.SECOND, 0);
		}

		GregorianCalendar diTwilight = null;
		Position center = getGeographicCenter();
		if (center != null) {
			Time twilight;

			boolean dst = di.getTimeZone().inDaylightTime(di.getTime());
			if (NIGHT_SHIFT.equals(pan))
				twilight = Sun.sunsetTime(di, center, di.getTimeZone(), dst);
			else
				twilight = Sun.sunriseTime(di, center, di.getTimeZone(), dst);

			diTwilight = (GregorianCalendar) setTimeToCalendar(di, twilight);

			if (NIGHT_SHIFT.equals(pan))
				diTwilight.setTimeInMillis((diTwilight.getTimeInMillis() + getSunsetOffset() * 60000));
			else
				diTwilight.setTimeInMillis((diTwilight.getTimeInMillis() + getSunriseOffset() * 60000));
		}

		return diTwilight;
	}

	/** set the sun-rise/set time to a calendar instance */
	static private Calendar setTimeToCalendar(Calendar c, Time time) {
		c.set(Calendar.HOUR_OF_DAY, time.getHours());
		c.set(Calendar.MINUTE, time.getMinutes());
		c.set(Calendar.SECOND, (int) time.getSeconds());
		return c;
	}

	private final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss Z";
	static private String date2str(Calendar c) {
		return new SimpleDateFormat(DATE_FORMAT).format(c.getTime());
	}
}
