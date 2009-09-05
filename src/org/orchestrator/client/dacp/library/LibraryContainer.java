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

import java.math.BigInteger;
import java.util.ArrayList;

import org.orchestrator.client.dacp.DACPNode;

public class LibraryContainer {
	public static final String MUSIC_CONTAINER = "Music";
	public static final String MOVIE_CONTAINER = "Movies";
	public static final String TV_CONTAINER = "TV Shows";
	
	protected long id;
	protected long count;
	protected String name;
	protected BigInteger pid;
	protected LibraryDatabase parent;
	
	protected ArrayList<LibraryItem> items;
	
	protected LibraryContainer(LibraryDatabase parent) {
		this.parent = parent;
		this.items = new ArrayList<LibraryItem>();
	}
	
	public String getName() {
		return this.name;
	}
	
	public long getId() {
		return this.id;
	}
	
	public BigInteger getPersistentId() {
		return this.pid;
	}
	
	public long getCount() {
		return this.count;
	}
	
	public BigInteger getPid() {
		return pid;
	}
	
	public LibraryDatabase getParent() {
		return this.parent;
	}
	
	public LibraryItem[] getItems() {
		return this.items.toArray(new LibraryItem[this.items.size()]);
	}
	
	public void addItem(LibraryItem item) {
		this.items.add(item);
	}
	
	public String toString() {
		return String.format("%X - %s (%d items)", this.pid, this.name, this.count);
	}
	
	public static LibraryContainer createFromDACPNode(LibraryDatabase db, DACPNode n) {
		LibraryContainer c = new LibraryContainer(db);
		c.name = n.get("minm").getString();
		c.pid = n.get("mper").getNumber();
		c.id = n.get("miid").getNumber().longValue();
		c.count = n.get("mimc").getNumber().longValue();
		return c;
	}
}
