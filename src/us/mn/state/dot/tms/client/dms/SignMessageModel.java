/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import java.util.HashMap;
import java.util.HashSet;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignText;
import us.mn.state.dot.tms.utils.SDMS;

/**
 * Model for sign text messages. This object is instantiated and contained by
 * MessageSelector. One SignMessageModel is associated with a single DMS.
 * It creates and contains SignTextComboBoxModel objects for each combobox 
 * in MessageSelector. This object listens for changes to sign_text and 
 * dms_sign_groups and is responsible for updating its model accordingly. 
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SignMessageModel implements ProxyListener<DmsSignGroup> {

	/** Create a SONAR name to check for allowed updates */
	static protected String createNamespaceString(String name) {
		return SignText.SONAR_TYPE + "/" + name;
	}

	/** DMS id associated with this object */
	protected final String dms_id;

	/** DMS sign group type cache, relates dms to sign groups */
	protected final TypeCache<DmsSignGroup> dms_sign_groups;

	/** Sign text type cache, list of all sign text lines */
	protected final TypeCache<SignText> sign_text;

	/** Listener for sign text proxies */
	protected final ProxyListener<SignText> listener;

	/** SONAR User for permission checks */
	protected final User user;

	/** Create a new sign message model */
	public SignMessageModel(String dms, TypeCache<DmsSignGroup> d,
		TypeCache<SignText> t, User u)
	{
		dms_id = dms;
		dms_sign_groups = d;
		sign_text = t;
		user = u;
		listener = new ProxyListener<SignText>() {
			public void proxyAdded(SignText proxy) {
				if(isMember(proxy.getSignGroup()))
					addSignText(proxy);
			}
			public void enumerationComplete() { }
			public void proxyRemoved(SignText proxy) {
				if(isMember(proxy.getSignGroup()))
					removeSignText(proxy);
			}
			public void proxyChanged(SignText proxy, String a) {
				if(isMember(proxy.getSignGroup()))
					changeSignText(proxy);
			}
		};
	}

	/** Initialize the sign message model */
	public void initialize() {
		dms_sign_groups.addProxyListener(this);
		sign_text.addProxyListener(listener);
	}

	/** Dispose of the model */
	public void dispose() {
		sign_text.removeProxyListener(listener);
		dms_sign_groups.removeProxyListener(this);
	}

	/** Add a new proxy to the model */
	public void proxyAdded(DmsSignGroup proxy) {
		if(dms_id.equals(proxy.getDms()))
			addGroup(proxy.getSignGroup());
	}

	/** Enumeration of the proxy type is complete */
	public void enumerationComplete() {
		// We're not interested
	}

	/** Remove a proxy from the model */
	public void proxyRemoved(DmsSignGroup proxy) {
		if(dms_id.equals(proxy.getDms()))
			removeGroup(proxy.getSignGroup());
	}

	/** Change a proxy in the model */
	public void proxyChanged(DmsSignGroup proxy, String attrib) {
		// NOTE: this should never happen
	}

	/** Set of DMS member groups */
	protected final HashSet<String> groups = new HashSet<String>();

	/** Is the DMS a member of the specified group? */
	protected boolean isMember(SignGroup g) {
		return g != null && groups.contains(g.getName());
	}

	/** 
	 * Get the local SignGroup for the DMS.
	 * @return local SignGroup if it exists, otherwise null
	 */
	protected SignGroup getLocalSignGroup() {
		DmsSignGroup dsg = dms_sign_groups.find(
			new Checker<DmsSignGroup>()
		{
			public boolean check(DmsSignGroup g) {
				return isLocalSignGroup(g);
			}
		});
		if(dsg != null)
			return dsg.getSignGroup();
		else
			return null;
	}

	/** Check if the given sign group is a local group for the DMS */
	protected boolean isLocalSignGroup(DmsSignGroup g) {
		return g.getDms().equals(dms_id) && g.getSignGroup().getLocal();
	}

	/** 
	  * Create a new sign text and add to the persistent sign text library.
	  * @param sg SignGroup the new message will be associated with.
	  * @param line Combobox line number.
	  * @param message Line text.
	  * @param priority line priority
	  */
	protected void createSignText(SignGroup sg, short line, String messarg,
		short priority)
	{
		if (sg==null || line<1 || messarg==null || messarg.length()<=0)
			return;
		// FIXME: this is a hack, see comments in method
		if(SDMS.ignoreLineHack(messarg))
			return;
		String mess = SDMS.getValidText(messarg);
		String name = createUniqueSignTextName(sg);
		if(canAddSignText(name)) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("sign_group", sg);
			attrs.put("line", new Short(line));
			attrs.put("message", mess);
			attrs.put("priority", new Short(priority));
			sign_text.createObject(name, attrs);
		}
	}

	/** Check if the user can add the named sign text */
	public boolean canAddSignText(String name) {
		return name != null && user.canAdd(createNamespaceString(name));
	}

	/** 
	 * Create a HashSet which contains all SignText names for this sign.
	 * @return A HashSet with entries as SignText names, e.g. V1_23
	 */
	private HashSet<String> createSignTextNameSet(final SignGroup sg) {
		final HashSet<String> names = new HashSet<String>();
		sign_text.find(new Checker<SignText>() {
			public boolean check(SignText st) {
				if(st.getSignGroup() == sg)
					names.add(st.getName());
				return false;
			}
		});
		return names;
	}

	/** 
	 * Create a SignText name, which is in this form: 
	 *    sign_group.name + "_" + uniqueid
	 *    where uniqueid is a sequential integer.
	 * @return A unique string for a new SignText entry, e.g. V1_23
	 *
	 * FIXME: this code is duplicated in SignTextTableModel as createName
	 */
	private String createUniqueSignTextName(SignGroup sg) {
		HashSet<String> names = createSignTextNameSet(sg);
		for(int i = 0; i < 10000; i++) {
			String n = sg.getName() + "_" + i;
			if(!names.contains(n))
				return n;
		}
		String msg =
			"Warning: SignMessageModel.createUniqueSignTextName()";
		System.err.println(msg);
		assert false : msg;
		return null;
	}

	/** 
	 * Lookup a SignText in the database.
	 * @parm line Message line number (1 based)
	 * @return the matching SignText else null if it doesn't exist.
	 */
	protected SignText lookupSignText(final short line, final String msg,
		final SignGroup sg)
	{
		if(sign_text == null || msg == null || sg == null || line < 1)
			return null;
		return sign_text.find(new Checker<SignText>() {
			public boolean check(SignText st) {
				if(st.getLine() != line)
					return false;
				if(!st.getSignGroup().getName().equals(sg.getName()))
					return false;
				if(!st.getMessage().equals(msg))
					return false;
				return true;
			}
		});
	}

	/** 
	 * Called when the DMS associated with this object is added to a
	 * new sign group. New SignText lines from the new sign group are
	 * added to each SignTextComboBoxModel.
	 */
	protected void addGroup(final SignGroup g) {
		groups.add(g.getName());
		// add new sign text lines in new group to combobox models
		sign_text.find(new Checker<SignText>() {
			public boolean check(SignText st) {
				if(st.getSignGroup() == g)
					addSignText(st);
				return false;
			}
		});
	}

	/** 
	 * Called when the DMS associated with this object is removed
	 * from a sign group. 
	 */
	protected void removeGroup(final SignGroup g) {
		groups.remove(g.getName());
		// delete lines from combobox models associated with sign group
		sign_text.find(new Checker<SignText>() {
			public boolean check(SignText st) {
				if(st.getSignGroup() == g)
					removeSignText(st);
				return false;
			}
		});
	}

	/** Mapping of line numbers to combo box models */
	protected final HashMap<Short, SignTextComboBoxModel> lines =
		new HashMap<Short, SignTextComboBoxModel>();

	/** Get the combobox line model for the specified line */
	public SignTextComboBoxModel getLineModel(short line) {
		if(lines.containsKey(line))
			return lines.get(line);
		else {
			SignTextComboBoxModel m = new SignTextComboBoxModel(
				line, this);
			lines.put(line, m);
			return m;
		}
	}

	/** Get the maximum line number */
	public short getMaxLine() {
		short m = 0;
		for(short i: lines.keySet())
			m = (short)Math.max(i, m);
		return m;
	}

	/** Add a sign message to the model, called by listener when sign_text
	 * changes */
	protected void addSignText(SignText t) {
		short line = t.getLine();
		SignTextComboBoxModel m = getLineModel(line);
		m.add(t);
	}

	/** Remove a sign message from the model, called by listener when
	 * sign_text changes */
	protected void removeSignText(SignText t) {
		short line = t.getLine();
		SignTextComboBoxModel m = getLineModel(line);
		m.remove(t);
	}

	/** Change a sign message in the model, called by listener when
	 * sign_text changes */
	protected void changeSignText(SignText t) {
		// iterate through all combobox models because the line
		// may have changed, moving it between comboboxes
		for(SignTextComboBoxModel m: lines.values())
			m.remove(t);
		// add to associated model
		this.addSignText(t);
	}
}
