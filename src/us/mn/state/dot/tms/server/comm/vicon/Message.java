/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.vicon;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * Vicon message
 *
 * @author Douglas Lau
 */
public class Message implements CommMessage {

	/** Start Of Header byte */
	static protected final byte SOH = (byte)1;

	/** End Of Message byte */
	static protected final byte EOM = (byte)'\r';

	/** End of Response byte */
	static protected final int EOR = '\n';

	/** Maximum size (in bytes) of a response from switcher */
	static protected final int MAX_RESPONSE = 80;

	/** Serial output stream */
	protected final OutputStream os;

	/** Serial input stream */
	protected final InputStream is;

	/** Chained property buffer */
	protected final LinkedList<ViconProperty> props =
		new LinkedList<ViconProperty>();

	/** Create a new Vicon message */
	public Message(OutputStream o, InputStream i) {
		os = o;
		is = i;
	}

	/** Add a controller property */
	public void add(ControllerProperty cp) {
		if(cp instanceof ViconProperty)
			props.add((ViconProperty)cp);
	}

	/** Query the controller properties.
	 * @throws IOException On any errors sending a request or receiving
	 *         response */
	public void queryProps() throws IOException {
		throw new ProtocolException("QUERY not supported");
	}

	/** Store the controller properties.
	 * @throws IOException On any errors sending a request or receiving
	 *         response */
	public void storeProps() throws IOException {
		is.skip(is.available());
		os.write(SOH);
		for(ViconProperty prop: props)
			prop.encodeStore(os, 0);
		os.write(EOM);
		os.flush();
		getResponse();
	}

	/** Get a response from the switcher */
	protected String getResponse() throws IOException {
		StringBuilder resp = new StringBuilder();
		while(resp.length() <= MAX_RESPONSE) {
			int value = is.read();
			if(value < 0)
				throw new EOFException("END OF STREAM");
			resp.append((char)value);
			if(value == EOR)
				break;
		}
		if(resp.indexOf("$") < 0) {
			throw new ParsingException("VICON ERROR: " +
				resp.toString());
		}
		return resp.toString();
	}
}
