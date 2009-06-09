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

import us.mn.state.dot.sonar.SonarObject;

/**
 * A lane-use graphic is an association between lane-use indication and a
 * graphic.
 *
 * @author Douglas Lau
 */
public interface LaneUseGraphic extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "lane_use_graphic";

	/** Set the indication (ordinal of LaneUseIndication) */
	void setIndication(int i);

	/** Get the indication (ordinal of LaneUseIndication) */
	int getIndication();

	/** Set the graphic number */
	void setGNumber(int n);

	/** Get the graphic number */
	int getGNumber();

	/** Set the graphic */
	void setGraphic(Graphic g);

	/** Get the graphic */
	Graphic getGraphic();

	/** Set the page number */
	void setPage(int p);

	/** Get the page number */
	int getPage();

	/** Set the page on time (tenths of a second) */
	void setOnTime(int t);

	/** Get the page on time (tenths of a second) */
	int getOnTime();
}
