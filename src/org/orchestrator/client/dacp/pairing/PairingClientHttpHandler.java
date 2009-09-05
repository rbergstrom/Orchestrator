/*
 *  The MIT License
 * 
 *  Copyright 2009 Ryan Bergstrom.
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 * 
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 * 
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package org.orchestrator.client.dacp.pairing;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;

import org.orchestrator.client.dacp.DACPPacket;
import org.orchestrator.client.util.Logging;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class PairingClientHttpHandler implements HttpHandler {
	public final static String PAIRING_PATH = "/pair";
	public final static String PAIRING_PARAM = "pairingcode";
	
	protected PairingEventListener listener;
	protected PairingClient client;
	
	public PairingClientHttpHandler(PairingClient client, PairingEventListener listener) {
		super();
		this.client = client;
		this.listener = listener;
	}
	
	protected static HashMap<String, String> parseQuery(String query) {
		String[] queryItems = query.split("&");
	    HashMap<String, String> map = new HashMap<String, String>();  
	    for (String param : queryItems) {  
	    	String[] bits = param.split("=");
	        map.put(bits[0], bits[1]);  
	    }
	    return map;
	}
	
	@Override
	public void handle(HttpExchange t) throws IOException {
		URI u = t.getRequestURI();
		Logging.Debug("SRV REQ: %s", u);
		if (t.getRequestMethod().equalsIgnoreCase("GET") && u.getPath().equals(PAIRING_PATH)) {
			if (u.getQuery() != null) {
				HashMap<String, String> params = parseQuery(u.getQuery());
				if (params.get(PAIRING_PARAM) != null) {
					HashMap<String, Object> values = new HashMap<String, Object>();
					values.put("cmpg", PairingClient.DEVICE_GUID);
					values.put("cmnm", PairingClient.DEVICE_NAME);
					values.put("cmty", PairingClient.DEVICE_TYPE);
					DACPPacket response = DACPPacket.createSimple("cmpa", values);
					byte[] data = response.serialize();
					t.sendResponseHeaders(200, data.length);
					Logging.Debug("SRV RES: HTTP 200");
					OutputStream os = t.getResponseBody();
					os.write(data);
					os.flush();
					os.close();
					listener.pairingSuccessful(new PairingEvent(client, t.getRemoteAddress().getAddress()));
					return;
				}
			}			
		}
		t.sendResponseHeaders(404, 0);
		t.getResponseBody().close();
		Logging.Debug("SRV RES: HTTP 404");
	}

}
