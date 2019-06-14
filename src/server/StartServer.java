package server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.gnome.gdk.Event;
import org.gnome.gtk.Builder;
import org.gnome.gtk.Button;
import org.gnome.gtk.Entry;
import org.gnome.gtk.Gtk;
import org.gnome.gtk.Label;
import org.gnome.gtk.MessageDialog;
import org.gnome.gtk.TextView;
import org.gnome.gtk.Widget;
import org.gnome.gtk.Window;

import utils.DateValidator;
import utils.DigitsValidator;
import utils.IpAddressValidator;
import utils.SystemIpAddress;
import utils.TimeValidator;

public class StartServer {
	private static Server server;
	private static Registry registry;
	private static ClocksSynchronization clocksSynchronization;
	private static boolean registryRunning = false;

	public static void main(String[] args) {
		IpAddressValidator ipAddressValidator = new IpAddressValidator();
		DigitsValidator digitsValidator = new DigitsValidator();
		DateValidator dateValidator = new DateValidator();
		TimeValidator timeValidator = new TimeValidator();
		String ipAddressString = new String();
		SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

		try {
			ipAddressString = new SystemIpAddress().getInet4AddressString();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Gtk.init(args);

		Builder builder = new Builder();
		try {
			builder.addFromFile("res/glade/server_ui.glade");
		} catch (FileNotFoundException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Window serverWindow = (Window) builder.getObject("serverWindow");

		MessageDialog dateTimeEmptyMsg = (MessageDialog) builder.getObject("dateTimeEmptyMsg");
		MessageDialog ipPortNameEmptyMsg = (MessageDialog) builder.getObject("ipPortNameEmptyMsg");
		MessageDialog wrongDateFormatMsg = (MessageDialog) builder.getObject("wrongDateFormatMsg");
		MessageDialog wrongTimeFormatMsg = (MessageDialog) builder.getObject("wrongTimeFormatMsg");
		MessageDialog wrongIpFormatMsg = (MessageDialog) builder.getObject("wrongIpFormatMsg");
		MessageDialog wrongPortFormatMsg = (MessageDialog) builder.getObject("wrongPortFormatMsg");
		MessageDialog syncFreqEmptyMsg = (MessageDialog) builder.getObject("syncFreqEmptyMsg");
		MessageDialog wrongSyncFreqFromatMsg = (MessageDialog) builder.getObject("wrongSyncFreqFromatMsg");
		MessageDialog rmiBindErrorMsg = (MessageDialog) builder.getObject("rmiBindErrorMsg");

		Label ipAddrLabel = (Label) builder.getObject("ipAddrLabel");
		ipAddrLabel.setLabel(ipAddressString);

		TextView logsTextView = (TextView) builder.getObject("logsTextView");

		Entry dateEntry = (Entry) builder.getObject("dateEntry");
		Entry timeEntry = (Entry) builder.getObject("timeEntry");
		Entry portEntry = (Entry) builder.getObject("portEntry");
		Entry serverNameEntry = (Entry) builder.getObject("serverNameEntry");

		Entry ipAddrEntry = (Entry) builder.getObject("ipAddrEntry");
		ipAddrEntry.setText(ipAddressString);

		Entry syncFreqEntry = (Entry) builder.getObject("syncFreqEntry");
		syncFreqEntry.setSensitive(false);

		Button setDateTimeBtn = (Button) builder.getObject("setDateTimeBtn");
		Button startServerBtn = (Button) builder.getObject("startServerBtn");

		Button stopServerBtn = (Button) builder.getObject("stopServerBtn");
		stopServerBtn.setSensitive(false);

		Button startSyncBtn = (Button) builder.getObject("startSyncBtn");
		startSyncBtn.setSensitive(false);

		Button stopSyncBtn = (Button) builder.getObject("stopSyncBtn");
		stopSyncBtn.setSensitive(false);

		serverWindow.showAll();

		serverWindow.connect(new Window.DeleteEvent() {

			@Override
			public boolean onDeleteEvent(Widget arg0, Event arg1) {
				if (registryRunning) {
					try {
						UnicastRemoteObject.unexportObject(registry, true);
					} catch (NoSuchObjectException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				Gtk.mainQuit();
				System.exit(0);
				return false;
			}
		});

		setDateTimeBtn.connect(new Button.Clicked() {

			@Override
			public void onClicked(Button arg0) {
				if (dateEntry.getText().isEmpty() || timeEntry.getText().isEmpty()) {
					dateTimeEmptyMsg.run();
					dateTimeEmptyMsg.hide();
				} else if (!dateValidator.validate(dateEntry.getText())) {
					wrongDateFormatMsg.run();
					wrongDateFormatMsg.hide();
				} else if (!timeValidator.validate(timeEntry.getText())) {
					wrongTimeFormatMsg.run();
					wrongTimeFormatMsg.hide();
				} else {
					String dateTimeString = dateEntry.getText() + " " + timeEntry.getText();
					ProcessBuilder setDate = new ProcessBuilder("bash", "-c", "date -s '" + dateTimeString + "'");
					try {
						setDate.start();
						TimeUnit.SECONDS.sleep(1);
					} catch (IOException | InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					logsTextView.getBuffer().insert(logsTextView.getBuffer().getIterEnd(),
							"[" + displayDateFormat.format(new Date(System.currentTimeMillis()))
									+ "] Ustawiono datę i czas.\n");
				}
			}
		});

		startServerBtn.connect(new Button.Clicked() {

			@Override
			public void onClicked(Button arg0) {
				if (ipAddrEntry.getText().isEmpty() || portEntry.getText().isEmpty()
						|| serverNameEntry.getText().isEmpty()) {
					ipPortNameEmptyMsg.run();
					ipPortNameEmptyMsg.hide();
				} else if (!ipAddressValidator.validate(ipAddrEntry.getText())) {
					wrongIpFormatMsg.run();
					wrongIpFormatMsg.hide();
				} else if (!digitsValidator.validate(portEntry.getText())) {
					wrongPortFormatMsg.run();
					wrongPortFormatMsg.hide();
				} else {
					String url = "rmi://" + ipAddrEntry.getText() + ":" + portEntry.getText() + "/"
							+ serverNameEntry.getText();
					System.setProperty("java.rmi.server.hostname", ipAddrEntry.getText());
					try {
						server = new Server(logsTextView);
						registry = LocateRegistry.createRegistry(Integer.parseInt(portEntry.getText()));
						Naming.rebind(url, server);
						logsTextView.getBuffer().insert(logsTextView.getBuffer().getIterEnd(),
								"[" + displayDateFormat.format(new Date(System.currentTimeMillis()))
										+ "] Serwer działa na: " + url + "\n");
						registryRunning = true;
						startServerBtn.setSensitive(false);
						stopServerBtn.setSensitive(true);
						startSyncBtn.setSensitive(true);
						syncFreqEntry.setSensitive(true);
					} catch (NumberFormatException | RemoteException | MalformedURLException e) {
						rmiBindErrorMsg.run();
						rmiBindErrorMsg.hide();
						e.printStackTrace();
					}
				}
			}
		});

		stopServerBtn.connect(new Button.Clicked() {

			@Override
			public void onClicked(Button arg0) {
				try {
					UnicastRemoteObject.unexportObject(registry, true);
					logsTextView.getBuffer().insert(logsTextView.getBuffer().getIterEnd(), "["
							+ displayDateFormat.format(new Date(System.currentTimeMillis())) + "] Serwer wyłączony\n");
					registryRunning = false;
					stopServerBtn.setSensitive(false);
					startSyncBtn.setSensitive(false);
					startServerBtn.setSensitive(true);
					syncFreqEntry.setSensitive(false);
				} catch (NoSuchObjectException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		startSyncBtn.connect(new Button.Clicked() {

			@Override
			public void onClicked(Button arg0) {
				if (syncFreqEntry.getText().isEmpty()) {
					syncFreqEmptyMsg.run();
					syncFreqEmptyMsg.hide();
				} else if (!digitsValidator.validate(syncFreqEntry.getText())) {
					wrongSyncFreqFromatMsg.run();
					wrongDateFormatMsg.hide();
				} else {
					startSyncBtn.setSensitive(false);
					stopServerBtn.setSensitive(false);
					stopSyncBtn.setSensitive(true);
					syncFreqEntry.setSensitive(false);
					server.setClockSyncFreq(Integer.parseInt(syncFreqEntry.getText()));
					logsTextView.getBuffer().insert(logsTextView.getBuffer().getIterEnd(),
							"[" + displayDateFormat.format(new Date(System.currentTimeMillis()))
									+ "] Synchronizacja zegarów włączona.\n");
					clocksSynchronization = new ClocksSynchronization(server, logsTextView);
					clocksSynchronization.start();
				}
			}
		});

		stopSyncBtn.connect(new Button.Clicked() {

			@Override
			public void onClicked(Button arg0) {
				clocksSynchronization.stop();
				logsTextView.getBuffer().insert(logsTextView.getBuffer().getIterEnd(),
						"[" + displayDateFormat.format(new Date(System.currentTimeMillis()))
								+ "] Synchronizacja zegarów wyłączona.\n");
				stopSyncBtn.setSensitive(false);
				startSyncBtn.setSensitive(true);
				stopServerBtn.setSensitive(true);
				syncFreqEntry.setSensitive(true);
			}
		});

		Gtk.main();
	}

}
