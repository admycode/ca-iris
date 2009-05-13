/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.pelco;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.ParsingException;
import us.mn.state.dot.tms.comm.ProtocolException;

/**
 * Pelco message
 *
 * @author Douglas Lau
 * @author Timothy Johnson
 */
public class Message implements AddressedMessage {

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

	/** Chained request buffer */
	protected final LinkedList<Request> requests =
		new LinkedList<Request>();

	/** Create a new Pelco message */
	public Message(OutputStream o, InputStream i) {
		os = o;
		is = i;
	}

	/** Get a string of the message */
	public String toString() {
		StringBuilder b = new StringBuilder();
		for(Request req: requests)
			b.append(req.toString());
		return b.toString();
	}

	/** Add a request object to this message */
	public void add(Object r) {
		if(r instanceof Request)
			requests.add((Request)r);
	}

	/** Perform a "get" request */
	public void getRequest() throws IOException {
		throw new ProtocolException("GET request not supported");
	}

	/** Perform a "set" request */
	public void setRequest() throws IOException {
		String req = toString();
		is.skip(is.available());
		os.write(SOH);
		os.write(req.getBytes());
		os.write(EOM);
		os.flush();
		getResponse();
	}

	/** Perform a "set" request */
	public void setRequest(String community) throws IOException {
		throw new ProtocolException("Community not handled by Pelco");
	}

	/** Get a response from the switcher */
	protected String getResponse() throws IOException {
		StringBuffer resp = new StringBuffer();
		while(true) {
			int value = is.read();
			if(value < 0)
				throw new EOFException("END OF STREAM");
			resp.append((char)value);
			if(value == EOR)
				break;
			else if(resp.length() > MAX_RESPONSE)
				break;
		}
		if(resp.indexOf("$") < 0) {
			throw new ParsingException("PELCO ERROR: " +
				resp.toString());
		}
		return resp.toString();
	}
}
