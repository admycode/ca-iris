/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  AHMCT, University of California
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

package us.mn.state.dot.tms.server.comm.cohuptz;

import java.io.IOException;
import java.io.OutputStream;


/**
 * This class creates a Cohu PTZ request to instruct the camera
 * to reset.
 *
 * @author Travis Swanston
 */
public class ResetCameraProperty extends CohuPTZProperty {

	/** Create the property. */
	public ResetCameraProperty() {
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(OutputStream os, int drop) throws IOException {

		byte[] message = new byte[5];
		message[0] = (byte)0xf8;
		message[1] = (byte)drop;
		message[2] = (byte)0x72;
		message[3] = (byte)0x73;
		message[4] = calculateChecksum(message, 1, 3);
		os.write(message);
	}

}
