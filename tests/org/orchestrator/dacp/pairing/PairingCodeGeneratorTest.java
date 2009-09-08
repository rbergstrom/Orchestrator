package org.orchestrator.dacp.pairing;

import org.junit.Test;
import org.orchestrator.client.dacp.pairing.PairingCodeGenerator;

import static org.junit.Assert.*;

public class PairingCodeGeneratorTest {
	@Test
	public void testHashes() {
		String passcode, pair;
		passcode = "1111";
		pair = "0000000000000001";
		assertEquals(PairingCodeGenerator.getCode(passcode, pair), "AAB15DF9F73AA252A7934E0AF9C86B13");
		pair = "0000000000000002";
		assertEquals(PairingCodeGenerator.getCode(passcode, pair), "BCFEFF3DE84C137648870733879F00D3");
		pair = "0000000000000003";
		assertEquals(PairingCodeGenerator.getCode(passcode, pair), "187E563379BCC3DC54DE5F3537D2E599");
	}
}
