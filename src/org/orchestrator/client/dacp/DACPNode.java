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

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import org.orchestrator.client.util.Utility;


public class DACPNode {
	
	
	protected String tag;
	protected Object value;
	
	public DACPNode(String tag, long value) {
		this(tag, Utility.toByteArray(value));
	}
	
	public DACPNode(String tag, int value) {
		this(tag, Utility.toByteArray(value));
	}
	
	public DACPNode(String tag, short value) {
		this(tag, Utility.toByteArray(value));
	}
	
	public DACPNode(String tag, byte value) {
		this(tag, Utility.toByteArray(value));
	}
	
	public DACPNode(String tag, Object value) {
		this.tag = tag;
		this.setValue(value);
	}
	
	public int getValueSize() {
		if (this.value instanceof String) {
			try {
				return ((String)this.value).getBytes("UTF-8").length;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return ((String)this.value).getBytes().length;
			}
		} else if (this.value instanceof BigInteger) {
			int bitlength = ((BigInteger)this.value).bitLength() + 1;
			if (bitlength <= 8) return 1;
			else if (bitlength <= 16) return 2;
			else if (bitlength <= 32) return 4;
			else if (bitlength <= 64) return 8;
			else return (int) Math.ceil(bitlength / 8.0);
		} else if (this.value instanceof DACPPacket) {
			return ((DACPPacket)this.value).getSize();
		} else if (this.value instanceof byte[]) {
			return ((byte[])this.value).length;
		} else {
			return 0;
		}
	}
	
	public int getSize() {
		return (8 + this.getValueSize());
	}
	
	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getTag() {
		return this.tag;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}
	
	public Object getValue() {
		return this.value;
	}
	
	public byte[] getByteArray() {
		if (this.value instanceof String) {
			try {
				return ((String)this.value).getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return ((String)this.value).getBytes();
			}
		} else if (this.value instanceof DACPPacket) {
			return ((DACPPacket)this.value).serialize();
		} else if (this.value instanceof byte[]) {
			return (byte[])this.value;
		} else {
			return new byte[0];
		}
	}
	
	public DACPNode get(String tag) {
		if (this.value instanceof DACPPacket) {
			return ((DACPPacket)this.value).get(tag);
		} else {
			return null;
		}
	}
	
	public DACPNode[] getMultiple(String tag) {
		if (this.value instanceof DACPPacket) {
			return ((DACPPacket)this.value).getMultiple(tag);
		} else {
			return null;
		}
	}
	
	public String getString() {
		if (this.value instanceof String) return (String)this.value;
		else return "";
	}
	
	public BigInteger getNumber() {
		if (this.value instanceof byte[] && ((byte[])this.value).length > 0) 
			return new BigInteger((byte[])this.value);
		else return BigInteger.valueOf(-1);
	}
	
	public DACPPacket getPacket() {
		if (this.value instanceof DACPPacket) return (DACPPacket)this.value;
		else return new DACPPacket();
	}

	public byte[] serialize() {
		int valueSize = this.getValueSize();
		byte[] temp;
		byte[] buffer = new byte[valueSize + 8];
		
		// 4-byte tag
		temp = this.tag.getBytes();
		System.arraycopy(temp, 0, buffer, 0, temp.length < 4 ? temp.length : 4);
		
		// 4-byte size of the value
		temp = Utility.toByteArray(valueSize);
		System.arraycopy(temp, 0, buffer, 4, temp.length);
		
		// arbitrarily sized value
		temp = this.getByteArray();
		System.arraycopy(temp, 0, buffer, 8, temp.length);
		return buffer;
	}
	
	public static DACPNode create(byte[] source) {
		return DACPNode.create(source, 0);
	}
	
	public static DACPNode create(byte[] source, int offset) {
		byte[] tagbuf = new byte[4], sizebuf = new byte[4];
		System.arraycopy(source, offset, tagbuf, 0, 4);
		System.arraycopy(source, offset+4, sizebuf, 0, 4);
		
		int size = Utility.toInt(sizebuf);
		String tag = new String(tagbuf);
		byte[] value = new byte[size];
		System.arraycopy(source, offset+8, value, 0, size);
		
		if (DACPPacket.BRANCHES.matcher(tag).matches()) {
			return new DACPNode(tag, DACPPacket.create(value));
		} else if (DACPPacket.STRINGS.matcher(tag).matches()) {
			try {
				return new DACPNode(tag, new String(value, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return new DACPNode(tag, new String(value));
			}
		} else {
			return new DACPNode(tag, value);
		}
	}
	
	public String toString() {
		return this.toString(0);
	}
	
	public String toString(int depth) {
		StringBuffer os = new StringBuffer();
		for (int i = 0; i < depth; i++) {
			os.append('\t');
		}
		if (this.value instanceof String)
			os.append(String.format("%s\t%d\t%s", this.tag, this.getValueSize(), this.getString()));
		else if (this.getNumber().intValue() > 0)
			os.append(String.format("%s\t%d\t%X (%d)", this.tag, this.getValueSize(), this.getNumber(), this.getNumber()));
		else if (this.value instanceof DACPPacket)
			os.append(String.format("%s\t--+\n%s", this.tag, this.getPacket().toString(depth+1)));
		else 
			os.append(String.format("%s\t%d\t(raw binary data)", this.tag, this.getValueSize()));
		return os.toString();
	}
}
