/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.help;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.tms.client.widget.SmartDesktop;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.utils.I18N;

/** 
 * Menu for help information.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class HelpMenu extends JMenu {

	/** Smart desktop */
	protected final SmartDesktop desktop;

	/** Open trouble ticket menu item */
	private final JMenuItem ticket_item = new JMenuItem(
		I18N.get("help.trouble.ticket"));

	/** Create a new HelpMenu */
	public HelpMenu(SmartDesktop sd) { 
		super(I18N.get("help"));
		desktop = sd;
		addSupportItem();
		addAboutItem();
	}

	/** Add support menu item */
	protected void addSupportItem() {
		JMenuItem item = new JMenuItem(I18N.get("help.support"));
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new SupportForm());
			}
		};
		add(item);
	}

	/** Add about menu item */
	protected void addAboutItem() {
		JMenuItem item = new JMenuItem(I18N.get("iris.about"));
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new AboutForm());
			}
		};
		add(item);
	}

	/** Set the logged-in status */
	public void setLoggedIn(boolean in) {
		if(in && SystemAttrEnum.HELP_TROUBLE_TICKET_ENABLE.getBoolean())
			addOpenTroubleTicketItem();
	}

	/** Add the 'open trouble ticket' menu item. This menu item
	 * is inserted at the begining of the help menu and not removed
	 * when the user logs out. */
	protected void addOpenTroubleTicketItem() { 
		// it's already on the menu
		if(isMenuComponent(ticket_item))
			return;
		final String url =
			SystemAttrEnum.HELP_TROUBLE_TICKET_URL.getString();
		new ActionJob(ticket_item) {
			public void perform() throws Exception {
				Help.invokeHelp(url);
			}
		};
		insert(ticket_item, 0);
	}
}
