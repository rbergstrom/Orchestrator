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

import org.orchestrator.client.dacp.DACPNode;
import org.orchestrator.client.util.Utility;

public class LibraryItem {
	protected LibraryContainer parent;
	protected String name;
	protected String artist;
	protected String album;
	protected String time;
	protected long id;
	protected long index;
	
	protected LibraryItem(LibraryContainer parent) {
		this.parent = parent;
	}
	
	public LibraryContainer getParent() {
		return this.parent;
	}
	
	public String getName() {
		return name;
	}

	public String getArtist() {
		return artist;
	}

	public String getAlbum() {
		return album;
	}

	public String getTime() {
		return time;
	}

	public long getId() {
		return id;
	}
	
	public long getIndex() {
		return index;
	}

	public String toString() {
		return String.format("%d - %s", this.id, this.name);
	}
	
	public static LibraryItem createFromDACPNode(LibraryContainer parent, DACPNode n) {
		LibraryItem i = new LibraryItem(parent);
		i.id = n.get("miid").getNumber().longValue();
		i.name = n.get("minm").getString();
		i.album = n.get("asal").getString();
		i.artist = n.get("asar").getString();
		i.index = n.get("mcti").getNumber().longValue();
		i.time = Utility.formatTime(n.get("astm").getNumber().longValue());
		return i;
	}
}
