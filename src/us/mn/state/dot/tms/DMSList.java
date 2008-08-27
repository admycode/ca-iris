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
package us.mn.state.dot.tms;

import java.rmi.RemoteException;

/**
 * DMSList is an interface which contains the methods for
 * remotely maintaining a list of dynamic message signs.
 *
 * @author Douglas Lau
 */
public interface DMSList extends DeviceList {

	/** Send an alert to all signs in the specified group */
	public void sendGroup(String group, String owner, String[] text,
		boolean overwrite)
		throws TMSException, RemoteException;

	/** Clear all signs in the specified group */
	public void clearGroup(String group, String owner)
		throws RemoteException;
}
