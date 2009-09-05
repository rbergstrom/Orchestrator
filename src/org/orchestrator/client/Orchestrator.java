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

package org.orchestrator.client;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.ActivityIndicator;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.ListViewSelectionListener;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.Slider;
import org.apache.pivot.wtk.SliderValueListener;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.effects.ShadeDecorator;
import org.apache.pivot.wtk.media.Picture;
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;
import org.orchestrator.client.dacp.DACPPacket;
import org.orchestrator.client.dacp.Session;
import org.orchestrator.client.dacp.SessionEvent;
import org.orchestrator.client.dacp.SessionEventListener;
import org.orchestrator.client.dacp.StatusUpdate;
import org.orchestrator.client.dacp.StatusUpdateEvent;
import org.orchestrator.client.dacp.StatusUpdateListener;
import org.orchestrator.client.dacp.library.Library;
import org.orchestrator.client.dacp.library.LibraryContainer;
import org.orchestrator.client.dacp.library.LibraryEvent;
import org.orchestrator.client.dacp.library.LibraryEventListener;
import org.orchestrator.client.dacp.library.LibraryItem;
import org.orchestrator.client.dacp.library.LibraryProbe;
import org.orchestrator.client.util.Logging;
import org.orchestrator.client.util.Utility;
import org.orchestrator.client.util.Logging.LogLevel;


public final class Orchestrator implements Application {
	private static final int WINDOW_WIDTH = 800;
	private static final int WINDOW_HEIGHT = 530;
	
	private Session session;
	private LibraryProbe probe;
	private ArrayList<Library> libraries;
	
	private long currentPlayTime;
	private long totalPlayTime;
	private byte currentPlayState;
	
	private Window window;
	private Resources resources;
	private ShadeDecorator windowOverlay;

	private LibraryContainer[] containers;
	private ArrayList<LibraryItem> nowPlayingList;
	private ArrayList<LibraryItem> songsList;
	
	@WTKX private Sheet libraryPicker;
	@WTKX(id="libraryPicker.statusLabel") private Label libraryStatusLabel;
	@WTKX(id="libraryPicker.libraryListView") private ListView libraryListView;
	@WTKX(id="libraryPicker.connectButton") private PushButton libraryConnectButton;
	@WTKX(id="libraryPicker.activitiyIndicator") private ActivityIndicator librarySpinner;
	
	@WTKX private ImageView albumArtImageView;
	@WTKX(id="statusPanel.statusTitleLabel") private Label statusTitleLabel;
	@WTKX(id="statusPanel.statusAlbumLabel") private Label statusAlbumLabel;
	@WTKX(id="statusPanel.statusArtistLabel") private Label statusArtistLabel;
	@WTKX(id="statusPanel.statusTimeLabel") private Label statusTimeLabel;
	@WTKX(id="statusPanel.prevButton") private Button prevButton;
	@WTKX(id="statusPanel.playButton") private Button playButton;
	@WTKX(id="statusPanel.pauseButton") private Button pauseButton;
	@WTKX(id="statusPanel.nextButton") private Button nextButton;
	@WTKX(id="statusPanel.seekSlider") private Slider seekSlider;
	@WTKX(id="statusPanel.volumeSlider") private Slider volumeSlider;
	
	@WTKX private TableView nowPlayingTableView;
	@WTKX private TableView songsTableView;
	
	private final Thread timeUpdateThread = new Thread(new Runnable() {
		@Override
		public void run() {
			Logging.Debug("Running timer update thread.");
			while (true) {
				while (currentPlayState == StatusUpdate.STATUS_PLAYING) {
					long startTime = System.currentTimeMillis();
					try {
						// Wait 1s then update the play times
						Thread.sleep(1000);
						long endTime = System.currentTimeMillis();
						currentPlayTime += (endTime - startTime);
						if (currentPlayTime >= totalPlayTime) {
							refreshStatus();
						} else {
							updatePlayTimes();
						}
					} catch (InterruptedException e) {
						// Interrupt is fired when something changes that requires
						// an immediate update
						refreshStatus();
						Logging.Debug("Interrupted timer update thread during PLAY");
						continue;
					}
				}
				while ((currentPlayState == StatusUpdate.STATUS_PAUSED) ||
						(currentPlayState == StatusUpdate.STATUS_STOPPED)) {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						// Interrupt is fired when something changes that requires
						// an immediate update
						Logging.Debug("Interrupted timer update thread during PAUSE/STOP");
						continue;
					}
				}
			}
		}
	});
	
	private final SessionEventListener sessionListener = new SessionEventListener() {

		@Override
		public void beginLogin(SessionEvent event) {
			libraryStatusLabel.setText(resources.getString("LibraryBeginLoginStatus"));
		}

		@Override
		public void beginPairing(SessionEvent event) {
			libraryStatusLabel.setText(resources.getString("LibraryBeginPairingStatus"));
		}
		
		@Override
		public void endPairing(SessionEvent event) {
			libraryStatusLabel.setText(resources.getString("LibraryDonePairingStatus"));
		}

		@Override
		public void loginFailed(SessionEvent event) {
			libraryStatusLabel.setText(resources.getString("LibraryLoginFailedStatus"));
		}

		@Override
		public void loginSucceeded(SessionEvent event) {
			libraryStatusLabel.setText(resources.getString("LibraryLoginSucceededStatus"));
			hideLibraryPicker();
		}
	};
	
	private final StatusUpdateListener statusListener = new StatusUpdateListener() {

		@Override
		public void statusUpdate(StatusUpdateEvent event) {
			refreshStatus(event.getStatusUpdate());
			timeUpdateThread.interrupt();
		}

		@Override
		public void trackChanged(StatusUpdateEvent event) {
			refreshStatus(event.getStatusUpdate());
			timeUpdateThread.interrupt();
			refreshAlbumArt();
		}
		
	};
	
	private LibraryContainer getContainerById(long id) {
		for (LibraryContainer c : containers) {
			if (c.getId() == id) {
				return c;
			}
		}
		return null;
	}
	
	private LibraryContainer getContainerByName(String name) {
		for (LibraryContainer c : containers) {
			if (c.getName().equals(name)) {
				return c;
			}
		}
		return null;
	}
	
	private void initializeMainWindow() {
		prevButton.getButtonPressListeners().add(new ButtonPressListener() {
			@Override
			public void buttonPressed(Button button) {
				session.previousItem();
			}
		});
		playButton.getButtonPressListeners().add(new ButtonPressListener() {
			@Override
			public void buttonPressed(Button button) {
				session.togglePlay();
			}
		});
		pauseButton.getButtonPressListeners().add(new ButtonPressListener() {
			@Override
			public void buttonPressed(Button button) {
				session.togglePlay();
			}
		});
		nextButton.getButtonPressListeners().add(new ButtonPressListener() {
			@Override
			public void buttonPressed(Button button) {
				session.nextItem();
			}
		});
		volumeSlider.getSliderValueListeners().add(new SliderValueListener() {
			@Override
			public void valueChanged(Slider slider, int previousValue) {
				session.setVolume(slider.getValue());
			}
		});
		seekSlider.setEnabled(false);
		// TODO: Fix seeking properly
		/*
		seekSlider.getSliderValueListeners().add(new SliderValueListener() {
			@Override
			public void valueChanged(Slider slider, int previousValue) {
				if (slider.getValue() - previousValue > 1)
					System.err.println(formatTime(slider.getValue() * 1000));
			}
		});
		*/
		nowPlayingTableView.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener() {
			@Override
			public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
				if (count >= 2) {
					int row = nowPlayingTableView.getRowAt(y);
					LibraryItem selectedItem = nowPlayingList.get(row);
					session.playItem(selectedItem);
				}
				return false;
			}

			@Override
			public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
				return false;
			}

			@Override
			public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
				return false;
			}
			
		});
		
		songsTableView.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener() {
			@Override
			public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
				if (count >= 2) {
					int row = songsTableView.getRowAt(y);
					LibraryItem selectedItem = songsList.get(row);
					session.playItem(selectedItem);
				}
				return false;
			}

			@Override
			public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
				return false;
			}

			@Override
			public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
				return false;
			}
			
		});
	}
	
	private void initializeLibraryPicker() throws IOException {
		libraries = new ArrayList<Library>();
		probe = new LibraryProbe(new LibraryEventListener() {
			@Override
			public void libraryFound(LibraryEvent event) {
				libraries.add(event.getLibrary());
				libraryListView.setListData(libraries);
			}
        });
        probe.startProbe();
        
        librarySpinner.setActive(true);
		libraryConnectButton.getButtonPressListeners().add(new ButtonPressListener() {
			@Override
			public void buttonPressed(Button button) {
				beginSession((Library)libraryListView.getSelectedItem());
			}
		});
		libraryListView.getListViewSelectionListeners().add(new ListViewSelectionListener() {
			@Override
			public void selectedRangeAdded(ListView listView, int rangeStart, int rangeEnd) {
				updateConnectButton(listView);
			}
			
			@Override
			public void selectedRangeRemoved(ListView listView, int rangeStart, int rangeEnd) {
				updateConnectButton(listView);
			}
			
			@Override
			public void selectedRangesChanged(ListView listView, Sequence<Span> previousSelectedRanges) {
				updateConnectButton(listView);
			}
			
			private void updateConnectButton(ListView listView) {
				libraryConnectButton.setEnabled(listView.getSelectedIndex() >= 0);
			}
		});
	}
	
	private void beginSession(Library library) {
		session = Session.createFromLibrary(library, sessionListener);
		session.registerStatusUpdateListener(statusListener);
		containers = session.getContainers(session.getDatabases()[0]);
		for (LibraryContainer c : containers)
			System.err.println(c);
		timeUpdateThread.start();
		refreshStatus();
		refreshAlbumArt();
		refreshVolume();
		songsList = new ArrayList<LibraryItem>(session.getItems(getContainerByName(LibraryContainer.MUSIC_CONTAINER)));
		songsTableView.setTableData(songsList);
	}
	
	private void showLibraryPicker() {
		libraryPicker.open(window);
        window.getDecorators().insert(windowOverlay, 0);
	}
	
	private void hideLibraryPicker() {
		libraryPicker.close();
        window.getDecorators().remove(windowOverlay);
	}

	private void refreshStatus() {
		DACPPacket p = session.getStatus(true);
		refreshStatus(StatusUpdate.createFromDACPPacket(p));
	}
	
	private void refreshStatus(StatusUpdate status) {
		currentPlayTime = status.getCurrentTime();
		totalPlayTime = status.getTotalTime();
		currentPlayState = status.getPlayStatus();
		
		statusTitleLabel.setText(status.getMediaName());
		statusArtistLabel.setText(status.getMediaArtist());
		statusAlbumLabel.setText(status.getMediaAlbum());
		seekSlider.setRange(0, (int)Math.floor(status.getTotalTime() / 1000));
		playButton.setVisible(currentPlayState != StatusUpdate.STATUS_PLAYING);
		pauseButton.setVisible(currentPlayState == StatusUpdate.STATUS_PLAYING);
		updatePlayTimes();
		nowPlayingList = new ArrayList<LibraryItem>(session.getItems(getContainerById(status.getContainerId())));
		nowPlayingTableView.setTableData(new ArrayList<LibraryItem>(nowPlayingList));
		for (LibraryItem item : nowPlayingList) {
			if (item.getId() == status.getMediaId()) {
				nowPlayingTableView.setSelectedIndex(nowPlayingList.indexOf(item));
			}
		}
	}
	
	private void updatePlayTimes() {
		statusTimeLabel.setText(String.format("%s/%s", Utility.formatTime(currentPlayTime), Utility.formatTime(totalPlayTime)));
		seekSlider.setValue((int)Math.floor(currentPlayTime/1000));
	}
	
	private void refreshVolume() {
		int volume = session.getVolume();
		volumeSlider.setValue(volume);
	}
	
	private void refreshAlbumArt() {
		BufferedImage albumArt = session.getAlbumArt(200, 200);
		if (albumArt != null) {
			albumArtImageView.setImage(new Picture(albumArt));
		}
	}
	
    public void startup(Display display, Map<String, String> properties) throws Exception {
    	Logging.setLogLevel(LogLevel.DEBUG);
    	resources = new Resources(this);
    	WTKXSerializer wtkxSerializer = new WTKXSerializer(resources);
    	window = (Window)wtkxSerializer.readObject(this, "MainWindow.wtkx");
    	windowOverlay = new ShadeDecorator(0.6f, Color.BLACK);
        window.open(display);
        wtkxSerializer.bind(this, Orchestrator.class);
        initializeLibraryPicker();
        initializeMainWindow();
        showLibraryPicker();
    }

    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }
        probe.stopProbe();
        return false;
    }

    public void suspend() {
    }

    public void resume() {
    }
    
    public static void main(String[] args) {
    	String[] sizeArgs = new String[] {
    			String.format("--width=%d", WINDOW_WIDTH),
    			String.format("--height=%d", WINDOW_HEIGHT)
    			};
        DesktopApplicationContext.main(Orchestrator.class, sizeArgs);
    }
}
