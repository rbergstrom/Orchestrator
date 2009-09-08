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

import org.orchestrator.client.util.Utility;

public class StatusUpdate {
	public static final byte STATUS_SHUFFLE_OFF = 0;
	public static final byte STATUS_SHUFFLE_ON = 1;
	public static final byte STATUS_REPEAT_OFF = 0;
	public static final byte STATUS_REPEAT_SINGLE = 1;
	public static final byte STATUS_REPEAT_ALL = 2;
	public static final byte STATUS_STOPPED = 2;
	public static final byte STATUS_PAUSED = 3;
	public static final byte STATUS_PLAYING = 4;
	
	protected String mediaName;
	protected String mediaAlbum;
	protected String mediaArtist;
	protected byte shuffleStatus;
	protected byte repeatStatus;
	protected byte playStatus;
	protected int totalTime;
	protected int remainingTime;
	
	protected int databaseId;
	protected int containerId;
	protected int mediaId;
	
	public String getMediaName() {
		return mediaName;
	}

	public String getMediaAlbum() {
		return mediaAlbum;
	}

	public String getMediaArtist() {
		return mediaArtist;
	}

	public byte getShuffleStatus() {
		return shuffleStatus;
	}
	
	public byte getRepeatStatus() {
		return repeatStatus;
	}

	public byte getPlayStatus() {
		return playStatus;
	}

	public int getTotalTime() {
		return totalTime;
	}

	public int getRemainingTime() {
		return remainingTime;
	}
	
	public int getCurrentTime() {
		return totalTime - remainingTime;
	}

	public long getDatabaseId() {
		return databaseId;
	}
	
	public long getContainerId() {
		return containerId;
	}
	
	public long getMediaId() {
		return mediaId;
	}

	public static StatusUpdate createFromDACPPacket(DACPPacket packet) {
		StatusUpdate s = new StatusUpdate();
		s.mediaName = packet.get("cann").getString();
		s.mediaAlbum = packet.get("canl").getString();
		s.mediaArtist = packet.get("cana").getString();
		s.playStatus = packet.get("caps").getNumber().byteValue();
		s.repeatStatus = packet.get("carp").getNumber().byteValue();
		s.shuffleStatus = packet.get("cash").getNumber().byteValue();
		s.totalTime = packet.get("cast").getNumber().intValue();
		try {
			s.remainingTime = packet.get("cant").getNumber().intValue();
		} catch (NullPointerException e) {
			s.remainingTime = s.getTotalTime();
		}
		
		byte[] ids = packet.get("canp").getByteArray();
		byte[] database = new byte[4];
		byte[] container = new byte[4];
		byte[] media = new byte[4];
		System.arraycopy(ids, 0, database, 0, 4);
		System.arraycopy(ids, 4, container, 0, 4);
		System.arraycopy(ids, 12, media, 0, 4);
		s.databaseId = Utility.toInt(database);
		s.containerId = Utility.toInt(container);
		s.mediaId = Utility.toInt(media);
		
		return s;
	}
}
