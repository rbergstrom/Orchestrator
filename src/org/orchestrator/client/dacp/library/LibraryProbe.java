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

package org.orchestrator.client.dacp.library;

import java.io.IOException;
import java.net.InetAddress;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;
import javax.jmdns.impl.DNSCache;
import javax.jmdns.impl.DNSConstants;
import javax.jmdns.impl.DNSRecord;
import javax.jmdns.impl.JmDNSImpl;
import javax.jmdns.impl.MDNSServiceRecord;
import javax.jmdns.impl.MDNSTextRecord;

import org.orchestrator.client.util.Logging;


public class LibraryProbe {
	public static final String MDNS_TOUCH_ABLE_TYPE = "_touch-able._tcp.local.";
	public static final String MDNS_DACP_TYPE = "_dacp._tcp.local.";
	
	protected JmDNS mdns;
	protected ServiceListener mdnsListener;
	protected LibraryEventListener libraryListener;
	
	public LibraryProbe(LibraryEventListener listener) {
		this.libraryListener = listener;
		this.mdnsListener = new ServiceListener() {
			
			@Override
			public void serviceAdded(final ServiceEvent event) {
				// This is the routine that runs whenever any mdns service broadcast
				// is discovered on the network, not just iTunes
				
				final String address = event.getName() + "." + event.getType();
				new Thread(new Runnable() {
					public void run() {
						try {
							DNSCache cache = ((JmDNSImpl)mdns).getCache();
							
							// Give it some time to resolve the TXT records
							Thread.sleep(500);
							
							// This is pretty hack-ish, since as far as I can tell JmDNS doesn't
							// let you get the raw record contents
							MDNSTextRecord textRecord = new MDNSTextRecord((DNSRecord.Text)cache.get(address, DNSConstants.TYPE_TXT, DNSConstants.CLASS_IN));
							String[] headers = textRecord.getText().split("[\u0000-\u001f]");
							String type = "", name = "", id = "";
							for(String header : headers) {
								if(header.startsWith("DvTy"))
									type = header.substring(5);
								if(header.startsWith("CtlN"))
									name = header.substring(5);
								if(header.startsWith("DbId"))
									id = header.substring(5);
							}
							
							MDNSServiceRecord serviceRecord = new MDNSServiceRecord((DNSRecord.Service)cache.get(address, DNSConstants.TYPE_SRV, DNSConstants.CLASS_IN));
							String host = serviceRecord.getServer();
							int port = serviceRecord.getPort();
							Logging.Debug("Found Library '%s' @ %s port %d - Type: %s, ID: %s", name, host, port, type, id);
							Library l = new Library(host, port, name, type, id);
							libraryListener.libraryFound(new LibraryEvent(event.getSource(), l));
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
				}).start();
			}
			
			public void serviceRemoved(ServiceEvent event) {}
			public void serviceResolved(ServiceEvent event) {}
		};
	}
	
	public void startProbe() throws IOException {
		this.mdns = JmDNS.create(InetAddress.getLocalHost());
		this.mdns.addServiceListener(MDNS_TOUCH_ABLE_TYPE, this.mdnsListener);
		this.mdns.addServiceListener(MDNS_DACP_TYPE, this.mdnsListener);
	}
	
	public void stopProbe() {
		this.mdns.removeServiceListener(MDNS_TOUCH_ABLE_TYPE, this.mdnsListener);
		this.mdns.removeServiceListener(MDNS_DACP_TYPE, this.mdnsListener);
		this.mdns.close();
	}
}
