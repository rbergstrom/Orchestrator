package org.orchestrator.dacp;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.orchestrator.client.dacp.DACPNode;
import org.orchestrator.client.dacp.DACPPacket;



public class DACPNodeTest {
	@Test
	public void testString() {
		DACPNode n = new DACPNode("asdf", "1234");
		assertEquals("asdf", n.getTag());
		assertEquals("1234", n.getString());
	}
	
	@Test
	public void testInt() {
		DACPNode n = new DACPNode("asdf", 1234);
		assertEquals("asdf", n.getTag());
		assertArrayEquals(new byte[] {0x00, 0x00, 0x04, (byte) 0xd2}, n.getByteArray());
		assertEquals(1234, n.getNumber().intValue());
	}
	
	@Test
	public void testBigInt() {
		DACPNode n = new DACPNode("asdf", 0x12345678abcdefL);
		assertEquals("asdf", n.getTag());
		assertArrayEquals(new byte[] {0x00, 0x12, 0x34, 0x56, 0x78, (byte) 0xab, (byte) 0xcd, (byte) 0xef}, n.getByteArray());
		assertEquals(0x12345678abcdefL, n.getNumber().longValue());
	}
	
	@Test
	public void testNested() {
		DACPNode inner1 = new DACPNode("asdf", "12345678");
		DACPNode inner2 = new DACPNode("qwer", "87654321");
		DACPPacket tree = new DACPPacket();
		tree.addNode(inner1);
		tree.addNode(inner2);
		DACPNode root = new DACPNode("cmpa", tree);
		assertEquals("cmpa", root.getTag());
		assertEquals("12345678", root.get("asdf").getString());
		assertEquals("87654321", root.get("qwer").getString());
	}
	
	@Test
	public void testSerialize() {
		DACPNode n = new DACPNode("asdf", "1234");
		assertArrayEquals(new byte[] {
				'a', 's', 'd', 'f', 0x0, 0x0, 0x0, 0x4, '1', '2', '3', '4'
				}, n.serialize());
	}
	
	@Test
	public void testCreateSimple() {
		DACPNode n = DACPNode.create(new byte[] {'c', 'a', 'n', 'a', 0x0, 0x0, 0x0, 0x4, '1', '2', '3', '4'});
		assertEquals("cana", n.getTag());
		assertEquals("1234", n.getString());
	}
	
	@Test
	public void testCreateNested() {
		DACPNode n = DACPNode.create(new byte[] {
				'c', 'm', 'p', 'a', 0x0, 0x0, 0x0, 0x10,
				'c', 'a', 'n', 'a', 0x0, 0x0, 0x0, 0x8,
				'1', '2', '3', '4', '5', '6', '7', '8'});
		assertEquals("cmpa", n.getTag());
		DACPPacket p = n.getPacket();
		assertArrayEquals(new byte[] {
				'c', 'a', 'n', 'a', 0x0, 0x0, 0x0, 0x8,
				'1', '2', '3', '4', '5', '6', '7', '8'},
				p.serialize());
		DACPNode p1 = p.getNodes()[0];
		assertEquals("cana", p1.getTag());
		assertEquals("12345678", p1.getString());
	}
}
