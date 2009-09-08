/* Based on the iTunes pairing code hash from:
 * http://jinxidoru.blogspot.com/2009/06/itunes-remote-pairing-code.html
 * 
 * Which was, in turn, based on the XINE project, released under the GPL.
 * 
 * Adapted from the C code, in order to work around Java's ridiculous and
 * completely arbitrary lack of unsigned integer types.
 */

package org.orchestrator.client.dacp.pairing;

public class PairingCodeGenerator {
	protected static long getIntFromBytes(byte[] array, int offset) {
        long val = ((0xff000000 & (array[offset+3] << 24)) + 
        			(0x00ff0000 & (array[offset+2] << 16)) + 
        			(0x0000ff00 & (array[offset+1] << 8))  + 
        			(0x000000ff & (array[offset])));
        return (val & 0xffffffffL);
    }

    public static String getCode(String passcode, String pair) 
    {
        byte[] paramTemp = new byte[64];
        byte[] passcodeBytes = passcode.getBytes();
        byte[] pairBytes = pair.getBytes();
        paramTemp[56] = (byte)192;
        paramTemp[24] = (byte)128;
        
        // set the pair value
        System.arraycopy(pairBytes, 0, paramTemp, 0, 16);

        // copy the pair code
        System.arraycopy(passcodeBytes, 0, paramTemp, 16, 4);

        paramTemp[22] = paramTemp[19];
        paramTemp[20] = paramTemp[18];
        paramTemp[18] = paramTemp[17];
        paramTemp[19] = paramTemp[17] = 0;
        
        // convert the byte array to an array of ints (well, longs)
        long[] param = new long[16];
        for (int i = 0; i < 16; i++) {
        	param[i] = getIntFromBytes(paramTemp, 4*i);
        }
        
		// initialize a, b, c, d
        long a = 0x67452301L;
        long b = 0xefcdab89L;
        long c = 0x98badcfeL;
        long d = 0x10325476L;

        a = 0xffffffffL & (((b & c) | (~b & d)) + param[0] + a - 0x28955B88);
        a = 0xffffffffL & (((a << 0x07) | (a >> 0x19)) + b);
        d = 0xffffffffL & (((a & b) | (~a & c)) + param[1] + d - 0x173848AA);
        d = 0xffffffffL & (((d << 0x0c) | (d >> 0x14)) + a);
        c = 0xffffffffL & (((d & a) | (~d & b)) + param[2] + c + 0x242070DB);
        c = 0xffffffffL & (((c << 0x11) | (c >> 0x0f)) + d);
        b = 0xffffffffL & (((c & d) | (~c & a)) + param[3] + b - 0x3E423112);
        b = 0xffffffffL & (((b << 0x16) | (b >> 0x0a)) + c);
        a = 0xffffffffL & (((b & c) | (~b & d)) + param[4] + a - 0x0A83F051);
        a = 0xffffffffL & (((a << 0x07) | (a >> 0x19)) + b);
        d = 0xffffffffL & (((a & b) | (~a & c)) + param[5] + d + 0x4787C62A);
        d = 0xffffffffL & (((d << 0x0c) | (d >> 0x14)) + a);
        c = 0xffffffffL & (((d & a) | (~d & b)) + param[6] + c - 0x57CFB9ED);
        c = 0xffffffffL & (((c << 0x11) | (c >> 0x0f)) + d);
        b = 0xffffffffL & (((c & d) | (~c & a)) + param[7] + b - 0x02B96AFF);
        b = 0xffffffffL & (((b << 0x16) | (b >> 0x0a)) + c);
        a = 0xffffffffL & (((b & c) | (~b & d)) + param[8] + a + 0x698098D8);
        a = 0xffffffffL & (((a << 0x07) | (a >> 0x19)) + b);
        d = 0xffffffffL & (((a & b) | (~a & c)) + param[9] + d - 0x74BB0851);
        d = 0xffffffffL & (((d << 0x0c) | (d >> 0x14)) + a);
        c = 0xffffffffL & (((d & a) | (~d & b)) + param[10] + c - 0x0000A44F);
        c = 0xffffffffL & (((c << 0x11) | (c >> 0x0f)) + d);
        b = 0xffffffffL & (((c & d) | (~c & a)) + param[11] + b - 0x76A32842);
        b = 0xffffffffL & (((b << 0x16) | (b >> 0x0a)) + c);
        a = 0xffffffffL & (((b & c) | (~b & d)) + param[12] + a + 0x6B901122);
        a = 0xffffffffL & (((a << 0x07) | (a >> 0x19)) + b);
        d = 0xffffffffL & (((a & b) | (~a & c)) + param[13] + d - 0x02678E6D);
        d = 0xffffffffL & (((d << 0x0c) | (d >> 0x14)) + a);
        c = 0xffffffffL & (((d & a) | (~d & b)) + param[14] + c - 0x5986BC72);
        c = 0xffffffffL & (((c << 0x11) | (c >> 0x0f)) + d);
        b = 0xffffffffL & (((c & d) | (~c & a)) + param[15] + b + 0x49B40821);
        b = 0xffffffffL & (((b << 0x16) | (b >> 0x0a)) + c);

        a = 0xffffffffL & (((b & d) | (~d & c)) + param[1] + a - 0x09E1DA9E);
        a = 0xffffffffL & (((a << 0x05) | (a >> 0x1b)) + b);
        d = 0xffffffffL & (((a & c) | (~c & b)) + param[6] + d - 0x3FBF4CC0);
        d = 0xffffffffL & (((d << 0x09) | (d >> 0x17)) + a);
        c = 0xffffffffL & (((d & b) | (~b & a)) + param[11] + c + 0x265E5A51);
        c = 0xffffffffL & (((c << 0x0e) | (c >> 0x12)) + d);
        b = 0xffffffffL & (((c & a) | (~a & d)) + param[0] + b - 0x16493856);
        b = 0xffffffffL & (((b << 0x14) | (b >> 0x0c)) + c);
        a = 0xffffffffL & (((b & d) | (~d & c)) + param[5] + a - 0x29D0EFA3);
        a = 0xffffffffL & (((a << 0x05) | (a >> 0x1b)) + b);
        d = 0xffffffffL & (((a & c) | (~c & b)) + param[10] + d + 0x02441453);
        d = 0xffffffffL & (((d << 0x09) | (d >> 0x17)) + a);
        c = 0xffffffffL & (((d & b) | (~b & a)) + param[15] + c - 0x275E197F);
        c = 0xffffffffL & (((c << 0x0e) | (c >> 0x12)) + d);
        b = 0xffffffffL & (((c & a) | (~a & d)) + param[4] + b - 0x182C0438);
        b = 0xffffffffL & (((b << 0x14) | (b >> 0x0c)) + c);
        a = 0xffffffffL & (((b & d) | (~d & c)) + param[9] + a + 0x21E1CDE6);
        a = 0xffffffffL & (((a << 0x05) | (a >> 0x1b)) + b);
        d = 0xffffffffL & (((a & c) | (~c & b)) + param[14] + d - 0x3CC8F82A);
        d = 0xffffffffL & (((d << 0x09) | (d >> 0x17)) + a);
        c = 0xffffffffL & (((d & b) | (~b & a)) + param[3] + c - 0x0B2AF279);
        c = 0xffffffffL & (((c << 0x0e) | (c >> 0x12)) + d);
        b = 0xffffffffL & (((c & a) | (~a & d)) + param[8] + b + 0x455A14ED);
        b = 0xffffffffL & (((b << 0x14) | (b >> 0x0c)) + c);
        a = 0xffffffffL & (((b & d) | (~d & c)) + param[13] + a - 0x561C16FB);
        a = 0xffffffffL & (((a << 0x05) | (a >> 0x1b)) + b);
        d = 0xffffffffL & (((a & c) | (~c & b)) + param[2] + d - 0x03105C08);
        d = 0xffffffffL & (((d << 0x09) | (d >> 0x17)) + a);
        c = 0xffffffffL & (((d & b) | (~b & a)) + param[7] + c + 0x676F02D9);
        c = 0xffffffffL & (((c << 0x0e) | (c >> 0x12)) + d);
        b = 0xffffffffL & (((c & a) | (~a & d)) + param[12] + b - 0x72D5B376);
        b = 0xffffffffL & (((b << 0x14) | (b >> 0x0c)) + c);

        a = 0xffffffffL & ((b ^ c ^ d) + param[5] + a - 0x0005C6BE);
        a = 0xffffffffL & (((a << 0x04) | (a >> 0x1c)) + b);
        d = 0xffffffffL & ((a ^ b ^ c) + param[8] + d - 0x788E097F);
        d = 0xffffffffL & (((d << 0x0b) | (d >> 0x15)) + a);
        c = 0xffffffffL & ((d ^ a ^ b) + param[11] + c + 0x6D9D6122);
        c = 0xffffffffL & (((c << 0x10) | (c >> 0x10)) + d);
        b = 0xffffffffL & ((c ^ d ^ a) + param[14] + b - 0x021AC7F4);
        b = 0xffffffffL & (((b << 0x17) | (b >> 0x09)) + c);
        a = 0xffffffffL & ((b ^ c ^ d) + param[1] + a - 0x5B4115BC);
        a = 0xffffffffL & (((a << 0x04) | (a >> 0x1c)) + b);
        d = 0xffffffffL & ((a ^ b ^ c) + param[4] + d + 0x4BDECFA9);
        d = 0xffffffffL & (((d << 0x0b) | (d >> 0x15)) + a);
        c = 0xffffffffL & ((d ^ a ^ b) + param[7] + c - 0x0944B4A0);
        c = 0xffffffffL & (((c << 0x10) | (c >> 0x10)) + d);
        b = 0xffffffffL & ((c ^ d ^ a) + param[10] + b - 0x41404390);
        b = 0xffffffffL & (((b << 0x17) | (b >> 0x09)) + c);
        a = 0xffffffffL & ((b ^ c ^ d) + param[13] + a + 0x289B7EC6);
        a = 0xffffffffL & (((a << 0x04) | (a >> 0x1c)) + b);
        d = 0xffffffffL & ((a ^ b ^ c) + param[0] + d - 0x155ED806);
        d = 0xffffffffL & (((d << 0x0b) | (d >> 0x15)) + a);
        c = 0xffffffffL & ((d ^ a ^ b) + param[3] + c - 0x2B10CF7B);
        c = 0xffffffffL & (((c << 0x10) | (c >> 0x10)) + d);
        b = 0xffffffffL & ((c ^ d ^ a) + param[6] + b + 0x04881D05);
        b = 0xffffffffL & (((b << 0x17) | (b >> 0x09)) + c);
        a = 0xffffffffL & ((b ^ c ^ d) + param[9] + a - 0x262B2FC7);
        a = 0xffffffffL & (((a << 0x04) | (a >> 0x1c)) + b);
        d = 0xffffffffL & ((a ^ b ^ c) + param[12] + d - 0x1924661B);
        d = 0xffffffffL & (((d << 0x0b) | (d >> 0x15)) + a);
        c = 0xffffffffL & ((d ^ a ^ b) + param[15] + c + 0x1fa27cf8);
        c = 0xffffffffL & (((c << 0x10) | (c >> 0x10)) + d);
        b = 0xffffffffL & ((c ^ d ^ a) + param[2] + b - 0x3B53A99B);
        b = 0xffffffffL & (((b << 0x17) | (b >> 0x09)) + c);

        a = 0xffffffffL & (((~d | b) ^ c) + param[0] + a - 0x0BD6DDBC);
        a = 0xffffffffL & (((a << 0x06) | (a >> 0x1a)) + b); 
        d = 0xffffffffL & (((~c | a) ^ b) + param[7] + d + 0x432AFF97);
        d = 0xffffffffL & (((d << 0x0a) | (d >> 0x16)) + a); 
        c = 0xffffffffL & (((~b | d) ^ a) + param[14] + c - 0x546BDC59);
        c = 0xffffffffL & (((c << 0x0f) | (c >> 0x11)) + d); 
        b = 0xffffffffL & (((~a | c) ^ d) + param[5] + b - 0x036C5FC7);
        b = 0xffffffffL & (((b << 0x15) | (b >> 0x0b)) + c); 
        a = 0xffffffffL & (((~d | b) ^ c) + param[9] + a + 0x655B59C3);
        a = 0xffffffffL & (((a << 0x06) | (a >> 0x1a)) + b); 
        d = 0xffffffffL & (((~c | a) ^ b) + param[3] + d - 0x70F3336E);
        d = 0xffffffffL & (((d << 0x0a) | (d >> 0x16)) + a); 
        c = 0xffffffffL & (((~b | d) ^ a) + param[10] + c - 0x00100B83);
        c = 0xffffffffL & (((c << 0x0f) | (c >> 0x11)) + d); 
        b = 0xffffffffL & (((~a | c) ^ d) + param[1] + b - 0x7A7BA22F);
        b = 0xffffffffL & (((b << 0x15) | (b >> 0x0b)) + c); 
        a = 0xffffffffL & (((~d | b) ^ c) + param[8] + a + 0x6FA87E4F);
        a = 0xffffffffL & (((a << 0x06) | (a >> 0x1a)) + b); 
        d = 0xffffffffL & (((~c | a) ^ b) + param[15] + d - 0x01D31920);
        d = 0xffffffffL & (((d << 0x0a) | (d >> 0x16)) + a); 
        c = 0xffffffffL & (((~b | d) ^ a) + param[6] + c - 0x5CFEBCEC);
        c = 0xffffffffL & (((c << 0x0f) | (c >> 0x11)) + d); 
        b = 0xffffffffL & (((~a | c) ^ d) + param[13] + b + 0x4E0811A1);
        b = 0xffffffffL & (((b << 0x15) | (b >> 0x0b)) + c); 
        a = 0xffffffffL & (((~d | b) ^ c) + param[4] + a - 0x08AC817E);
        a = 0xffffffffL & (((a << 0x06) | (a >> 0x1a)) + b); 
        d = 0xffffffffL & (((~c | a) ^ b) + param[11] + d - 0x42C50DCB);
        d = 0xffffffffL & (((d << 0x0a) | (d >> 0x16)) + a); 
        c = 0xffffffffL & (((~b | d) ^ a) + param[2] + c + 0x2AD7D2BB);
        c = 0xffffffffL & (((c << 0x0f) | (c >> 0x11)) + d); 
        b = 0xffffffffL & (((~a | c) ^ d) + param[9] + b - 0x14792C6F);
        b = 0xffffffffL & (((b << 0x15) | (b >> 0x0b)) + c); 

        a += 0x67452301;
        b += 0xefcdab89;
        c += 0x98badcfe;
        d += 0x10325476;

        // switch to little endian
        a = 0xffffffffL & (((a&0xff000000L)>>24) + ((a&0xff0000)>>8) + ((a&0xff00)<<8) + ((a&0xff)<<24));
        b = 0xffffffffL & (((b&0xff000000L)>>24) + ((b&0xff0000)>>8) + ((b&0xff00)<<8) + ((b&0xff)<<24));
        c = 0xffffffffL & (((c&0xff000000L)>>24) + ((c&0xff0000)>>8) + ((c&0xff00)<<8) + ((c&0xff)<<24));
        d = 0xffffffffL & (((d&0xff000000L)>>24) + ((d&0xff0000)>>8) + ((d&0xff00)<<8) + ((d&0xff)<<24));
        
        // write the pairing id and return it
        return String.format("%08X%08X%08X%08X", a, b, c, d);
    }

}

