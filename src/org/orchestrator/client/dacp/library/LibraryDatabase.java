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

public class LibraryDatabase {

	protected String name;
	protected BigInteger pid;
	protected long count;
	protected long id;
	
	protected ArrayList<LibraryContainer> containers;
	
	protected LibraryDatabase() {
		this.containers = new ArrayList<LibraryContainer>();
	}
	
	public String getName() {
		return this.name;
	}
	
	public long getId() {
		return this.id;
	}
	
	public long getCount() {
		return this.count;
	}
	
	public BigInteger getPid() {
		return pid;
	}
	
	public LibraryContainer[] getContainers() {
		return this.containers.toArray(new LibraryContainer[this.containers.size()]);
	}
	
	public void addContainer(LibraryContainer c) {
		this.containers.add(c);
	}
	
	public String toString() {
		return String.format("%d - %s (%d items)", this.id, this.name, this.count);
	}
	
	public static LibraryDatabase createFromDACPNode(DACPNode n) {
		LibraryDatabase db = new LibraryDatabase();
		db.name = n.get("minm").getString();
		db.id = n.get("miid").getNumber().longValue();
		db.pid = n.get("mper").getNumber();
		db.count = n.get("mctc").getNumber().longValue();
		return db;
	}
}
