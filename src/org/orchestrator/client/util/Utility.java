package org.orchestrator.client.util;

import java.math.BigInteger;

public class Utility {
	public static String formatTime(long timeInMillis) {
		int seconds = (int)Math.round(timeInMillis / 1000.0);
		int hours = (int)Math.floor(seconds / 3600);
		seconds -= hours * 3600;
		int minutes = (int)Math.floor(seconds / 60);
		seconds -= minutes * 60;
		if (hours > 0)
			return String.format("%d:%02d:%02d", hours, minutes, seconds);
		else
			return String.format("%d:%02d", minutes, seconds);
	}
	
	public static int toInt(byte[] array) {
		return new BigInteger(array).intValue();
	}
	
	public static long toLong(byte[] array) {
		return new BigInteger(array).longValue();
	}
	
	public static short toShort(byte[] array) {
		return new BigInteger(array).shortValue();
	}
	
	public static int toByte(byte[] array) {
		return new BigInteger(array).byteValue();
	}
	
	public static byte[] padByteArray(byte[] in, int size) {
		byte[] padded = new byte[size];
		System.arraycopy(in, 0, padded, size - in.length, in.length);
		return padded;
	}
	
	public static byte[] toByteArray(byte value) {
		return new byte[] { value };
	}
	
	public static byte[] toByteArray(short value) {
		return padByteArray(BigInteger.valueOf(value).toByteArray(), 2);
	}
	
	public static byte[] toByteArray(int value) {
		return padByteArray(BigInteger.valueOf(value).toByteArray(), 4);
	}
	
	public static byte[] toByteArray(long value) {
		return padByteArray(BigInteger.valueOf(value).toByteArray(), 8);
	}
	
	public static String toHex(byte[] value) {
		StringBuffer sb = new StringBuffer();
		for (byte b : value) {
			sb.append(String.format("%02X", b));
		}
		return sb.toString();
	}
}
