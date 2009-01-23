/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.schedule;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Device2;
import us.mn.state.dot.tms.TimingPlan;
import us.mn.state.dot.tms.TimingPlanType;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for timing plans 
 *
 * @author Douglas Lau
 */
public class TimingPlanModel extends ProxyTableModel<TimingPlan> {

	/** Count of columns in timing plan table model */
	static protected final int COLUMN_COUNT = 8;

	/** Name column number */
	static protected final int COL_NAME = 0;

	/** Plan type column number */
	static protected final int COL_TYPE = 1;

	/** Device column number */
	static protected final int COL_DEVICE = 2;

	/** Start time column number */
	static protected final int COL_START = 3;

	/** Stop time column number */
	static protected final int COL_STOP = 4;

	/** Active column number */
	static protected final int COL_ACTIVE = 5;

	/** Testing column number */
	static protected final int COL_TESTING = 6;

	/** Target column number */
	static protected final int COL_TARGET = 7;

	/** Time parser formats */
	static protected final DateFormat[] TIME_FORMATS = {
		new SimpleDateFormat("h:mm a"),
		new SimpleDateFormat("H:mm"),
		new SimpleDateFormat("h a"),
		new SimpleDateFormat("H")
	};

	/** Parse a time string */
	static protected Date parseTime(String t) {
		for(int i = 0; i < TIME_FORMATS.length; i++) {
			try {
				return TIME_FORMATS[i].parse(t);
			}
			catch(ParseException e) {
				// Ignore
			}
		}
		return null;
	}

	/** Parse a time string and return the minute-of-day */
	static protected int parseMinute(String t) {
		Calendar c = Calendar.getInstance();
		c.setTime(parseTime(t));
		return c.get(Calendar.HOUR_OF_DAY) * 60 +
			c.get(Calendar.MINUTE);
	}

	/** Convert minute-of-day to time string */
	static protected String timeString(int minute) {
		StringBuilder min = new StringBuilder();
		min.append(minute % 60);
		while(min.length() < 2)
			min.insert(0, '0');
		return (minute / 60) + ":" + min;
	}

	/** Cell renderer for this table */
	static protected final DefaultTableCellRenderer RENDERER =
		new DefaultTableCellRenderer();
	static {
		RENDERER.setHorizontalAlignment(SwingConstants.CENTER);
	}

	/** Create a new table column */
	static protected TableColumn createColumn(int col, String name,
		boolean center)
	{
		TableColumn c = new TableColumn(col);
		c.setHeaderValue(name);
		if(center)
			c.setCellRenderer(RENDERER);
		return c;
	}

	/** Create the table column model */
	static public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, "Name", false));
		m.addColumn(createColumn(COL_TYPE, "Plan Type", false));
		m.addColumn(createColumn(COL_DEVICE, "Device", false));
		m.addColumn(createColumn(COL_START, "Start Time", true));
		m.addColumn(createColumn(COL_STOP, "Stop Time", true));
		m.addColumn(createColumn(COL_ACTIVE, "Active", false));
		m.addColumn(createColumn(COL_TESTING, "Testing", false));
		m.addColumn(createColumn(COL_TARGET, "Target", true));
		return m;
	}

	/** Device in question */
	protected final Device2 device;

	/** Create a new timing plan table model */
	public TimingPlanModel(TypeCache<TimingPlan> c, Device2 d) {
		super(c, d != null);
		device = d;
		initialize();
	}

	/** Add a new proxy to the table model */
	protected int doProxyAdded(TimingPlan proxy) {
		if(device == null || device == proxy.getDevice())
			return super.doProxyAdded(proxy);
		else
			return -1;
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the column class */
	public Class getColumnClass(int column) {
		if(column == COL_ACTIVE || column == COL_TESTING)
			return Boolean.class;
		else if(column == COL_TARGET)
			return Integer.class;
		else
			return String.class;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		TimingPlan p = getProxy(row);
		if(p == null)
			return null;
		switch(column) {
		case COL_NAME:
			return p.getName();
		case COL_TYPE:
			return TimingPlanType.fromOrdinal(p.getPlanType());
		case COL_DEVICE:
			Device2 d = p.getDevice();
			if(d != null)
				return d.getName();
			else
				return null;
		case COL_START:
			return timeString(p.getStartMin());
		case COL_STOP:
			return timeString(p.getStopMin());
		case COL_ACTIVE:
			return p.getActive();
		case COL_TESTING:
			return p.getTesting();
		case COL_TARGET:
			return p.getTarget();
		default:
			return null;
		}
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		return column != COL_NAME && column != COL_TYPE &&
		       column != COL_DEVICE;
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		TimingPlan p = getProxy(row);
		if(p == null)
			return;
		switch(column) {
		case COL_START:
			p.setStartMin(parseMinute(value.toString()));
			break;
		case COL_STOP:
			p.setStopMin(parseMinute(value.toString()));
			break;
		case COL_ACTIVE:
			if(value instanceof Boolean)
				p.setActive((Boolean)value);
			break;
		case COL_TESTING:
			if(value instanceof Boolean)
				p.setTesting((Boolean)value);
			break;
		case COL_TARGET:
			if(value instanceof Integer)
				p.setTarget((Integer)value);
			break;
		}
	}
}
