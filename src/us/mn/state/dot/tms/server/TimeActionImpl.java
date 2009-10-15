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
package us.mn.state.dot.tms.server;

import java.sql.ResultSet;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.DayPlan;
import us.mn.state.dot.tms.DayPlanHelper;
import us.mn.state.dot.tms.TimeAction;
import us.mn.state.dot.tms.TMSException;

/**
 * Action for triggering an action plan to be deployed or undeployed.
 *
 * @author Douglas Lau
 */
public class TimeActionImpl extends BaseObjectImpl implements TimeAction {

	/** Load all the time actions */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading time actions...");
		namespace.registerType(SONAR_TYPE, TimeActionImpl.class);
		store.query("SELECT name, action_plan, day_plan, minute, " +
			"deploy FROM iris." + SONAR_TYPE  +";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new TimeActionImpl(
					namespace,
					row.getString(1),	// name
					row.getString(2),	// action_plan
					row.getString(3),	// day_plan
					row.getShort(4),	// minute
					row.getBoolean(5)	// deploy
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("action_plan", action_plan);
		map.put("day_plan", day_plan);
		map.put("minute", minute);
		map.put("deploy", deploy);
		return map;
	}

	/** Get the database table name */
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new time action */
	public TimeActionImpl(String n) {
		super(n);
	}

	/** Create a new time action */
	protected TimeActionImpl(Namespace ns, String n, String a, String d,
		short m, boolean dp)
	{
		this(n, (ActionPlan)ns.lookupObject(ActionPlan.SONAR_TYPE, a),
		     (DayPlan)ns.lookupObject(DayPlan.SONAR_TYPE, d), m, dp);
	}

	/** Create a new time action */
	protected TimeActionImpl(String n, ActionPlan a, DayPlan d, short m,
		boolean dp)
	{
		this(n);
		action_plan = a;
		day_plan = d;
		minute = m;
		deploy = dp;
	}

	/** Action plan */
	protected ActionPlan action_plan;

	/** Get the action plan */
	public ActionPlan getActionPlan() {
		return action_plan;
	}

	/** Day plan */
	protected DayPlan day_plan;

	/** Get the day plan */
	public DayPlan getDayPlan() {
		return day_plan;
	}

	/** Minute-of-day (0-1440) */
	protected short minute;

	/** Get the minute-of-day (0-1440) */
	public short getMinute() {
		return minute;
	}

	/** Flag to trigger action plan deployed / undeployed */
	protected boolean deploy;

	/** Set the deploy trigger flag */
	public void setDeploy(boolean d) {
		deploy = d;
	}

	/** Set the deploy trigger flag */
	public void doSetDeploy(boolean d) throws TMSException {
		if(d == deploy)
			return;
		store.update(this, "deploy", d);
		setDeploy(d);
	}

	/** Get the deploy trigger flag */
	public boolean getDeploy() {
		return deploy;
	}

	/** Perform action if date and time is right */
	public void perform(Calendar cal, int min) {
		if(getMinute() == min) {
			if(!DayPlanHelper.isHoliday(day_plan, cal))
				perform();
		}
	}

	/** Perform the time action */
	protected void perform() {
		ActionPlan ap = action_plan;	// Avoid race
		if(ap instanceof ActionPlanImpl) {
			ActionPlanImpl api = (ActionPlanImpl)ap;
			if(api.getActive())
				api.setDeployed(getDeploy());
		}
	}
}
