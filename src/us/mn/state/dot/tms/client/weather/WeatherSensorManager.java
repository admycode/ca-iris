/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.weather;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import javax.swing.JPopupMenu;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A weather sensor manager is a container for SONAR weather sensor objects.
 *
 * @author Douglas Lau
 */
public class WeatherSensorManager extends ProxyManager<WeatherSensor> {

	/** Lane marking map object marker */
	static protected final WeatherSensorMarker MARKER =
		new WeatherSensorMarker();

	/** Get the weather sensor cache */
	static private TypeCache<WeatherSensor> getCache(Session s) {
		return s.getSonarState().getWeatherSensors();
	}

	/** User session */
	protected final Session session;

	/** Create a new weather sensor manager */
	public WeatherSensorManager(Session s, GeoLocManager lm) {
		super(getCache(s), lm);
		session = s;
		cache.addProxyListener(this);
	}

	/** Get the proxy type name */
	public String getProxyType() {
		return I18N.get("weather.sensor");
	}

	/** Get the shape for a given proxy */
	protected Shape getShape(AffineTransform at) {
		return MARKER.createTransformedShape(at);
	}

	/** Create a theme for weather sensors */
	protected ProxyTheme<WeatherSensor> createTheme() {
		ProxyTheme<WeatherSensor> theme = new ProxyTheme<WeatherSensor>(
			this, MARKER);
		theme.addStyle(ItemStyle.NO_CONTROLLER,
			ProxyTheme.COLOR_NO_CONTROLLER);
		theme.addStyle(ItemStyle.ALL);
		return theme;
	}

	/** Check the style of the specified proxy */
	public boolean checkStyle(ItemStyle is, WeatherSensor proxy) {
		switch(is) {
		case NO_CONTROLLER:
			return proxy.getController() == null;
		case ALL:
			return true;
		default:
			return false;
		}
	}

	/** Show the properties form for the selected proxy */
	public void showPropertiesForm() {
		// FIXME
	}

	/** Create a popup menu for the selected proxy object(s) */
	protected JPopupMenu createPopup() {
		// No popup
		return null;
	}

	/** Find the map geo location for a proxy */
	protected GeoLoc getGeoLoc(WeatherSensor proxy) {
		return proxy.getGeoLoc();
	}

	/** Get the layer zoom visibility threshold */
	protected int getZoomThreshold() {
		return 12;
	}
}
