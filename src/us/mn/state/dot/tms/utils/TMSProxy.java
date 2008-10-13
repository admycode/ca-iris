/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.utils;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import us.mn.state.dot.tms.DMSList;
import us.mn.state.dot.tms.Login;
import us.mn.state.dot.tms.DeviceList;
import us.mn.state.dot.tms.TMS;
import us.mn.state.dot.tms.TMSObject;

/**
 * Client-side proxy for the TMS server object.
 *
 * @author Douglas Lau
 */
public class TMSProxy {

	/** Remote TMS */
	protected final TMS tms;

	/** Timing plan list */
	protected final RemoteListModel plans;

	/** Get the timing plan list */
	public RemoteListModel getTimingPlans() { return plans; }

	/** Ramp meter list */
	protected final DeviceList meter_list;

	/** Get the ramp meter list */
	public DeviceList getMeterList() {
		return meter_list;
	}

	/** Ramp meter list model */
	protected RemoteListModel rampMeters;

	/** Get the ramp meter list model
	 * @deprecated Call getMeterList instead */
	public RemoteListModel getRampMeters() {
		return rampMeters;
	}

	/** Set the ramp meter list model
	 * @deprecated */
	public void setRampMeters(RemoteListModel m) {
		rampMeters = m;
	}

	/** Available ramp meter list */
	protected final RemoteListModel availableMeters;

	/** Get the available ramp meter list */
	public RemoteListModel getAvailableMeters() { return availableMeters; }

	/** Dynamic message sign list */
	protected final DMSList dms_list;

	/** Get the dynamic message sign list */
	public DMSList getDMSList() {
		return dms_list;
	}

	/** Available DMS list */
	protected final RemoteListModel availableDMSs;

	/** Get the available DMS list */
	public RemoteListModel getAvailableDMSs() {
		return availableDMSs;
	}

	/** Dynamic message sign list */
	protected RemoteListModel dms_model;

	/** Get the dynamic message sign list model
	 * @deprecated Use getDMSList instead */
	public RemoteListModel getDMSListModel() {
		return dms_model;
	}

	/** Set the dynamic message sign list model
	 * @deprecated */
	public void setDMSListModel(RemoteListModel m) {
		dms_model = m;
	}

	/** Lane Control Signal list */
	protected final RemoteListModel lcss;

	/** Get the lane control signal list */
	public RemoteListModel getLCSList() { return lcss; }

	/** LCS list */
	protected final DeviceList lcs_list;

	/** Available LCS list */
	protected final RemoteListModel availableLCSs;

	/** Get the available LCS list */
	public RemoteListModel getAvailableLCSs() {
		return availableLCSs;
	}

	/** Create a new TMS proxy */
	public TMSProxy(String server, String user) throws RemoteException,
		NotBoundException, MalformedURLException
	{
		Login l = (Login)Naming.lookup("//" + server + "/login");
		tms = l.login(user);
		meter_list = tms.getRampMeterList();
		availableMeters = new RemoteListModel(
			meter_list.getAvailableList());
		dms_list = tms.getDMSList();
		availableDMSs = new RemoteListModel(
			dms_list.getAvailableList());
		plans = new RemoteListModel(tms.getTimingPlanList());
		lcs_list = tms.getLCSList();
		lcss = new RemoteListModel(lcs_list);
		availableLCSs = new RemoteListModel(
			lcs_list.getAvailableList());
	}

	/** Dispose of all proxied lists */
	public void dispose() {
		availableMeters.dispose();
		availableDMSs.dispose();
		availableLCSs.dispose();
	}

	/** Get a TMSObject */
	public TMSObject getTMSObject(int vaultOID){
		try{
			return tms.getObject(vaultOID);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
}
