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

package org.orchestrator.client.dacp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;

import org.orchestrator.client.util.Logging;

public class RequestHelper {
	public static final String PROTOCOL = "http";
	public static final int HTTP_TIMEOUT = 10000;
	public static final int BUFFER_SIZE = 1024;
	
	public static byte[] requestRaw(InetSocketAddress host, String path) throws IOException {
		return requestRaw(host, path, false);
	}
	
	public static byte[] requestRaw(InetSocketAddress host, String path, boolean keepalive) throws IOException {
		URL url = new URL(PROTOCOL, host.getAddress().getHostAddress(), host.getPort(), path);
		Logging.Debug("REQ: %s", url);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestProperty("Viewer-Only-Client", "1");
		if (!keepalive) {
			conn.setConnectTimeout(HTTP_TIMEOUT);
			conn.setReadTimeout(HTTP_TIMEOUT);
		}
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Logging.Debug("RES: HTTP %d", conn.getResponseCode());
		try {
			InputStream is = conn.getInputStream();
			
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = 0;
			
			while ((bytesRead = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			
			os.flush();
			os.close();
			is.close();
			
		} catch (IOException e) {
			throw new HttpRequestException(conn.getResponseCode(), conn.getURL());
		}
		return os.toByteArray();
	}
	
	public static DACPPacket request(InetSocketAddress host, String path) throws IOException {
		return request(host, path, false);
	}
	
	public static DACPPacket request(InetSocketAddress host, String path, boolean keepalive) throws IOException {
		return DACPPacket.create(requestRaw(host, path, keepalive));
	}
}
