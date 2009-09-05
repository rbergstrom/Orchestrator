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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class DACPPacket {
	public static final Pattern BRANCHES = Pattern.compile("(cmpa|msrv|cmst|mlog|agal|mlcl|mshl|mlit|abro|abar|apso|caci|avdb|cmgt|aply|adbs)");
	public static final Pattern STRINGS = Pattern.compile("(minm|cann|cana|canl|asaa|asal|asar)");
	
	protected HashMap<String, ArrayList<DACPNode>> nodes;
	protected int length;
	
	public DACPPacket() {
		this.length = 0;
		this.nodes = new HashMap<String, ArrayList<DACPNode>>();
	}
	
	public DACPPacket(DACPNode n) {
		this();
		this.addNode(n);	
	}
	
	public DACPPacket(HashMap<String, Object> values) {
		this();
		for (Map.Entry<String, Object> e : values.entrySet()) {
			this.addNode(new DACPNode(e.getKey(), e.getValue()));
		}
	}
	
	public DACPNode get(String tag) {
		return this.nodes.get(tag).get(0);
	}
	
	public DACPNode[] getMultiple(String tag) {
		ArrayList<DACPNode> tagList = this.nodes.get(tag);
		return tagList == null ? new DACPNode[0] : tagList.toArray(new DACPNode[this.length]);
	}
	
	public DACPNode[] getNodes() {
		ArrayList<DACPNode> nodes = new ArrayList<DACPNode>();
		for (ArrayList<DACPNode> c : this.nodes.values()) {
			nodes.addAll(c);
		}
		return nodes.toArray(new DACPNode[this.length]);
	}
	
	public void addNode(DACPNode n) {
		// Supports multiple nodes with the same tag
		ArrayList<DACPNode> l = this.nodes.get(n.getTag());
		if (l == null) {
			l = new ArrayList<DACPNode>();
			this.nodes.put(n.getTag(), l);
		}
		l.add(n);
		this.length++;
	}
	
	public int getSize() {
		int size = 0;
		for (ArrayList<DACPNode> l : this.nodes.values()) {
			for (DACPNode n : l) {
				size += n.getSize();
			}
		}
		return size;
	}
	
	public byte[] serialize() {
		byte[] buffer;
		int size = this.getSize();
		buffer = new byte[size];
		int offset = 0;
		for (ArrayList<DACPNode> l : this.nodes.values()) {
			for (DACPNode n : l) {
				size = n.getSize();
				System.arraycopy(n.serialize(), 0, buffer, offset, size);
				offset += size;
			}
		}
		return buffer;
	}
	
	// Shortcut to make a packet with a single tag containing a list of nodes
	public static DACPPacket createSimple(String tag, HashMap<String, Object> values) {
		DACPPacket inner = new DACPPacket(values);
		DACPNode root = new DACPNode(tag, inner);
		DACPPacket pkt = new DACPPacket(root);
		return pkt;
	}
	
	public static DACPPacket create(byte[] source) {
		return DACPPacket.create(source, 0);
	}
	
	public static DACPPacket create(byte[] source, int offset) {
		int pos = offset;
		DACPPacket pkt = new DACPPacket();
		while (pos < (source.length - offset)) {
			DACPNode n = DACPNode.create(source, pos);
			pos += n.getSize();
			pkt.addNode(n);
		}
		return pkt;
	}
	
	public String toString() {
		return this.toString(0);
	}
	
	public String toString(int depth) {
		StringBuffer os = new StringBuffer();
		for (ArrayList<DACPNode> l : this.nodes.values()) {
			for (DACPNode n : l) {
				for (int i = 0; i < depth; i++) {
					os.append('\t');
				}
				os.append(String.format("%s\n", n.toString()));
			}
		}
		return os.toString();
	}
}
