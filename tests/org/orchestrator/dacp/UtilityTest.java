package org.orchestrator.dacp;

import static org.junit.Assert.*;
import org.junit.Test;
import org.orchestrator.client.util.Utility;

public class UtilityTest {
	@Test
	public void testIntToBytes() {
		int a = 0xab;
		int b = 0xabcd;
		int c = 0xabcdef;
		int d = 0xabcdef12;
		byte[] ab = Utility.toByteArray(a);
		byte[] bb = Utility.toByteArray(b);
		byte[] cb = Utility.toByteArray(c);
		byte[] db = Utility.toByteArray(d);
		assertArrayEquals(new byte[] {0x00, 0x00, 0x00, (byte) 0xab}, ab);
		assertArrayEquals(new byte[] {0x00, 0x00, (byte) 0xab, (byte) 0xcd}, bb);
		assertArrayEquals(new byte[] {0x00, (byte) 0xab, (byte) 0xcd, (byte) 0xef}, cb);
		assertArrayEquals(new byte[] {(byte) 0xab, (byte) 0xcd, (byte) 0xef, 0x12}, db);
	}
	
	@Test
	public void testBytesToInt() {
		byte[] a = new byte[] {0x00, 0x00, 0x00, (byte) 0xab};
		byte[] b = new byte[] {0x00, 0x00, (byte) 0xab, (byte) 0xcd};
		byte[] c = new byte[] {0x00, (byte) 0xab, (byte) 0xcd, (byte) 0xef};
		byte[] d = new byte[] {(byte) 0xab, (byte) 0xcd, (byte) 0xef, 0x12};
		int ai = Utility.toInt(a);
		int bi = Utility.toInt(b);
		int ci = Utility.toInt(c);
		int di = Utility.toInt(d);
		assertEquals(0xab, ai);
		assertEquals(0xabcd, bi);
		assertEquals(0xabcdef, ci);
		assertEquals(0xabcdef12, di);
	}
}
