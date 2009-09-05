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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.orchestrator.client.dacp.library.Library;
import org.orchestrator.client.dacp.library.LibraryContainer;
import org.orchestrator.client.dacp.library.LibraryDatabase;
import org.orchestrator.client.dacp.library.LibraryItem;
import org.orchestrator.client.dacp.pairing.PairingClient;
import org.orchestrator.client.dacp.pairing.PairingEvent;
import org.orchestrator.client.dacp.pairing.PairingEventListener;
import org.orchestrator.client.util.Logging;
import org.orchestrator.client.util.Utility;


/**
 * @author rbergstrom
 *
 */
public class Session {
	
	protected final InetSocketAddress host;
	protected BigInteger sessionId;
	protected int revision;
	protected SessionEventListener sessionListener;
	protected StatusUpdateListener statusListener = null;
	
	public static Session createFromLibrary(Library library, SessionEventListener listener) {
		Session s = new Session(library.getAddress(), listener);
		s.login(PairingClient.DEVICE_GUID);
		return s;
	}
	
	protected Session(InetSocketAddress host, SessionEventListener listener) {
		this.sessionListener = listener;
		this.host = host;
		this.revision = 1;
	}
	
	public void registerStatusUpdateListener(StatusUpdateListener listener) {
		this.statusListener = listener;
	}

	/**
	 * @return 64-bit unsigned session ID
	 */
	public BigInteger getSessionId() {
		return this.sessionId;
	}
	
	protected void pairWithHost() {
		final Session s = this;
		sessionListener.beginPairing(new SessionEvent(this));
		PairingClient pc = new PairingClient();
		try {
			pc.beginPairing(new PairingEventListener() {

				@Override
				public void pairingFailed(PairingEvent event) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void pairingSuccessful(PairingEvent event) {
					InetAddress pairedAddr = event.getAddress();
					if (pairedAddr.equals(host.getAddress())) {
						Logging.Debug("Paired with %s, retrying login", pairedAddr);
						sessionListener.endPairing(new SessionEvent(s));
						event.getClient().stopPairing();
						login(PairingClient.DEVICE_GUID);
					} else {
						Logging.Debug("Paired with %s, but that's not the library we want", pairedAddr);
					}
				}
				
			});
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	protected void login(byte[] guid) {
		Logging.Info("Beginning login with GUID 0x%s", Utility.toHex(guid));
		sessionListener.beginLogin(new SessionEvent(this));
		String path = String.format("/login?pairing-guid=%s", Utility.toHex(guid));
		try {
			DACPPacket p = RequestHelper.request(this.host, path);
			this.sessionId = p.get("mlog").get("mlid").getNumber();
			sessionListener.loginSucceeded(new SessionEvent(this));
		} catch (IOException e) {
			// iTunes will return an HTTP 503 response code if it rejects our login
			if (e instanceof HttpRequestException) {
				if (((HttpRequestException) e).getErrorCode() == HttpURLConnection.HTTP_UNAVAILABLE) {
					Logging.Info("Invalid login, begining pairing.");
					this.pairWithHost();
				}
			} else {
				sessionListener.loginFailed(new SessionEvent(this));
				e.printStackTrace();
			}
		}
	}
	
	public void updateRevisionNumber() {
		try {
			String path = String.format("/update?session-id=%d&revision-number=1", this.sessionId);
			DACPPacket p = RequestHelper.request(this.host, path).get("mupd").getPacket();
			this.revision = p.get("musr").getNumber().intValue();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @return list of all databases in the connected library
	 */
	public LibraryDatabase[] getDatabases() {
		DACPPacket p;
		try {
			p = RequestHelper.request(this.host, 
					String.format("/databases?session-id=%d", this.sessionId));
			DACPNode[] dbnodes = p.get("avdb").get("mlcl").getMultiple("mlit");
			ArrayList<LibraryDatabase> dbs = new ArrayList<LibraryDatabase>();
			for (DACPNode n : dbnodes) {
				LibraryDatabase db = LibraryDatabase.createFromDACPNode(n);
				LibraryContainer[] containers = this.getContainers(db);
				for (LibraryContainer c : containers) {
					db.addContainer(c);
				}
				dbs.add(db);
			}
			return dbs.toArray(new LibraryDatabase[dbs.size()]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}	
	}
	
	/**
	 * @param database - database to search for containers
	 * @return list of containers inside the specified database
	 */
	public LibraryContainer[] getContainers(LibraryDatabase database) {
		try {
			DACPPacket p = RequestHelper.request(this.host,
					String.format("/databases/%d/containers?session-id=%d&meta=dmap.itemname,dmap.itemcount,dmap.itemid,dmap.persistentid,daap.baseplaylist,com.apple.itunes.special-playlist,com.apple.itunes.smart-playlist,com.apple.itunes.saved-genius,dmap.parentcontainerid,dmap.editcommandssupported,com.apple.itunes.jukebox-current", database.getId(), this.sessionId));
			System.err.println(p);
			DACPNode[] cnodes =  p.get("aply").get("mlcl").getMultiple("mlit");
			ArrayList<LibraryContainer> containers = new ArrayList<LibraryContainer>();
			for (DACPNode n : cnodes) {
				LibraryContainer c = LibraryContainer.createFromDACPNode(database, n);
				if (!c.getName().equals(LibraryContainer.TV_CONTAINER) && !c.getName().equals(LibraryContainer.MOVIE_CONTAINER))
					containers.add(c);
			}
			return containers.toArray(new LibraryContainer[containers.size()]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return the container that contains the iTunes audio files
	 */
	public LibraryContainer getMusicContainer() {
		LibraryDatabase[] databases = this.getDatabases();
		Logging.Info("Found %d databases, picking '%s'", databases.length, databases[0]);
		LibraryContainer[] containers = databases[0].getContainers();
		for (LibraryContainer c : containers) {
			if (c.getName().equals(LibraryContainer.MUSIC_CONTAINER)) {
				Logging.Info("Found Music container - ID#%d", c.getId());
				return c;
			}
		}
		return null;
	}
	
	/**
	 * @param container - container to search for items
	 * @return list of items in the specified container
	 */
	public LibraryItem[] getItems(LibraryContainer container) {
		try {
			DACPPacket p = RequestHelper.request(this.host,
					String.format("/databases/%d/containers/%d/items?session-id=%d&meta=dmap.itemname,dmap.itemid,daap.songartist,daap.songalbum,dmap.containeritemid,daap.songtime", 
							container.getParent().getId(), container.getId(), this.sessionId));
			DACPNode[] inodes = p.get("apso").get("mlcl").getMultiple("mlit");
			ArrayList<LibraryItem> items = new ArrayList<LibraryItem>();
			for (DACPNode n : inodes) {
				items.add(LibraryItem.createFromDACPNode(container, n));
			}
			return items.toArray(new LibraryItem[items.size()]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public DACPPacket getStatus(boolean immediate) {
		try {
			int revision = immediate ? 1 : this.revision;
			String path = String.format("/ctrl-int/1/playstatusupdate?revision-number=%d&session-id=%d", revision, this.sessionId);
			DACPPacket p = RequestHelper.request(this.host, path, !immediate).get("cmst").getPacket();
			this.revision = p.get("cmsr").getNumber().intValue();
			return p;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * @param position - seek position in milliseconds
	 */
	public void setPlayPosition(long position) {
		try {
			RequestHelper.request(this.host, 
					String.format("/ctrl-int/1/setproperty?dacp.playingtime=%d&session-id=%d", position, this.sessionId));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @param newStatus - new shuffle status (ON or OFF)
	 */
	public void setShuffleStatus(byte newStatus) {
		try {
			RequestHelper.request(this.host, 
					String.format("/ctrl-int/1/setproperty?dacp.shufflestate=%d&session-id=%d", newStatus, this.sessionId));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @param newStatus - new repeat status (ALL, SINGLE or OFF)
	 */
	public void setRepeatStatus(byte newStatus) {
		try {
			RequestHelper.request(this.host, 
					String.format("/ctrl-int/1/setproperty?dacp.repeatstate=%d&session-id=%d", newStatus, this.sessionId));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @param newVolume - new volume, between 0 and 100 inclusive
	 */
	public void setVolume(int newVolume) {
		if (newVolume < 0) newVolume = 0;
		if (newVolume > 100) newVolume = 100;
		try {
			RequestHelper.request(this.host, 
					String.format("/ctrl-int/1/setproperty?dmcp.volume=%d&session-id=%d", newVolume, this.sessionId));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @return current volume, from 0 to 100 inclusive
	 */
	public int getVolume() {
		try {
			DACPPacket p = RequestHelper.request(this.host, 
					String.format("/ctrl-int/1/getproperty?properties=dmcp.volume&session-id=%d", this.sessionId));
			return p.get("cmgt").get("cmvo").getNumber().intValue();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		
	}
	
	/**
	 * @param width - width of returned image
	 * @param height - height of returned image
	 * @return BufferedImage containing the decoded album art image, or null if no art is available
	 */
	public BufferedImage getAlbumArt(int width, int height) {
		try {
			byte[] image = RequestHelper.requestRaw(this.host,
					String.format("/ctrl-int/1/nowplayingartwork?mw=%d&mh=%d&session-id=%d", width, height, this.sessionId));
			ByteArrayInputStream is = new ByteArrayInputStream(image);
			try {
				return ImageIO.read(is);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		} catch (IOException e) {
			return null;
		}
	}
	
	public void togglePlay() {
		try {
			RequestHelper.request(this.host, 
					String.format("/ctrl-int/1/playpause?session-id=%d", this.sessionId));
			if (statusListener != null) {
				statusListener.statusUpdate(
						new StatusUpdateEvent(this, StatusUpdate.createFromDACPPacket(this.getStatus(true))));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void nextItem() {
		try {
			RequestHelper.request(this.host, 
					String.format("/ctrl-int/1/nextitem?session-id=%d", this.sessionId));
			if (statusListener != null) {
				statusListener.trackChanged(
						new StatusUpdateEvent(this, StatusUpdate.createFromDACPPacket(this.getStatus(true))));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void previousItem() {
		try {
			RequestHelper.request(this.host, 
					String.format("/ctrl-int/1/previtem?session-id=%d", this.sessionId));
			if (statusListener != null) {
				statusListener.trackChanged(
						new StatusUpdateEvent(this, StatusUpdate.createFromDACPPacket(this.getStatus(true))));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void playItem(LibraryItem item) {
		LibraryContainer c = item.getParent();
		LibraryDatabase db = c.getParent();
		try {
			RequestHelper.request(this.host, 
					String.format("/ctrl-int/1/playspec?database-spec='dmap.persistentid:0x%08X'&container-spec='dmap.persistentid:0x%08X'&container-item-spec='dmap.containeritemid:0x%x'&session-id=%d",
							db.getPid(), c.getPid(), item.getIndex(), this.sessionId));
			if (statusListener != null) {
				statusListener.trackChanged(
						new StatusUpdateEvent(this, StatusUpdate.createFromDACPPacket(this.getStatus(true))));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
