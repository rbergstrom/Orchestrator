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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Hashtable;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import org.orchestrator.client.util.Logging;

import com.sun.net.httpserver.HttpServer;

public class PairingClient {
	public final static String MDNS_REMOTE_TYPE = "_touch-remote._tcp.local.";
	public final static String DEVICE_NAME = "Orchestrator Remote";
    public final static String SERVICE_NAME = "orchestrator";
    public final static String PAIR_ID = "0000000000000001";
    public final static String DEVICE_TYPE = "iPod";
	public final static byte[] DEVICE_GUID = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
    public final static int PAIRING_PORT = 1024;

    protected JmDNS mdns;
    protected ServiceInfo remoteService;
    protected HttpServer server;

    public PairingClient(){
    	Hashtable<String, String> values = new Hashtable<String, String>();
        values.put("DvNm", DEVICE_NAME);
        values.put("RemV", "10000");
        values.put("DvTy", "iPod");
        values.put("RemN", "Remote");
        values.put("txtvers", "1");
        values.put("Pair", PAIR_ID);
        this.remoteService = ServiceInfo.create(MDNS_REMOTE_TYPE, SERVICE_NAME, PAIRING_PORT, 0, 0, values);
    }

    public void beginPairing(PairingEventListener listener) throws IOException {
    	this.mdns = JmDNS.create(InetAddress.getLocalHost());
        
        server = HttpServer.create(new InetSocketAddress(PAIRING_PORT), 1);
        server.createContext("/", new PairingClientHttpHandler(this, listener));
        
        new Thread(new Runnable() {
                public void run() {
                    try {
                    	server.start();
                        mdns.registerService(remoteService);
                        Logging.Debug("mDNS service registered, HTTP server started");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
    }

    public void stopPairing() {
    	server.stop(0);
    	server = null;
    	mdns.unregisterService(remoteService);
    	mdns.close();
    	mdns = null;
    }
}
