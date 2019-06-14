package client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
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

import common.IServer;
import utils.DateValidator;
import utils.DigitsValidator;
import utils.IpAddressValidator;
import utils.SystemIpAddress;
import utils.TimeValidator;

public class StartClient {
	private static Client client;
	private static IServer server;
	private static String ipAddressString;
	private static String urlString;
	private static boolean connected = false;

	public static void main(String[] args) {
		IpAddressValidator ipAddressValidator = new IpAddressValidator();
		DigitsValidator digitsValidator = new DigitsValidator();
		DateValidator dateValidator = new DateValidator();
		TimeValidator timeValidator = new TimeValidator();
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
			builder.addFromFile("res/glade/client_ui.glade");
		} catch (FileNotFoundException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Window clientWindow = (Window) builder.getObject("clientWindow");

		MessageDialog dateTimeEmptyMsg = (MessageDialog) builder.getObject("dateTimeEmptyMsg");
		MessageDialog ipPortNameEmptyMsg = (MessageDialog) builder.getObject("ipPortNameEmptyMsg");
		MessageDialog wrongDateFormatMsg = (MessageDialog) builder.getObject("wrongDateFormatMsg");
		MessageDialog wrongTimeFormatMsg = (MessageDialog) builder.getObject("wrongTimeFormatMsg");
		MessageDialog wrongIpFormatMsg = (MessageDialog) builder.getObject("wrongIpFormatMsg");
		MessageDialog wrongPortFormatMsg = (MessageDialog) builder.getObject("wrongPortFormatMsg");
		MessageDialog connectionErrorMsg = (MessageDialog) builder.getObject("connectionErrorMsg");
		MessageDialog lostConnMsg = (MessageDialog) builder.getObject("lostConnMsg");

		Label ipAddrLabel = (Label) builder.getObject("ipAddrLabel");
		ipAddrLabel.setLabel(ipAddressString);

		TextView logsTextView = (TextView) builder.getObject("logsTextView");

		Entry dateEntry = (Entry) builder.getObject("dateEntry");
		Entry timeEntry = (Entry) builder.getObject("timeEntry");
		Entry portEntry = (Entry) builder.getObject("portEntry");
		Entry serverNameEntry = (Entry) builder.getObject("serverNameEntry");
		Entry ipAddrEntry = (Entry) builder.getObject("ipAddrEntry");

		Button setDateTimeBtn = (Button) builder.getObject("setDateTimeBtn");
		Button connectBtn = (Button) builder.getObject("connectBtn");
		Button disconnectBtn = (Button) builder.getObject("disconnectBtn");
		disconnectBtn.setSensitive(false);

		clientWindow.showAll();

		clientWindow.connect(new Window.DeleteEvent() {

			@Override
			public boolean onDeleteEvent(Widget arg0, Event arg1) {
				if (connected) {
					try {
						server.unregisterClient(client);
					} catch (RemoteException e) {
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

		connectBtn.connect(new Button.Clicked() {

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
					urlString = "rmi://" + ipAddrEntry.getText() + ":" + portEntry.getText() + "/"
							+ serverNameEntry.getText();
					System.setProperty("java.rmi.server.hostname", ipAddressString);
					try {
						client = new Client(logsTextView);
						server = (IServer) Naming.lookup(urlString);
						logsTextView.getBuffer().insert(logsTextView.getBuffer().getIterEnd(),
								"[" + displayDateFormat.format(new Date(System.currentTimeMillis())) + "] "
										+ server.registerClient(client) + urlString + "\n");
						connected = true;
						connectBtn.setSensitive(false);
						disconnectBtn.setSensitive(true);
						ipAddrEntry.setSensitive(false);
						portEntry.setSensitive(false);
						serverNameEntry.setSensitive(false);
					} catch (MalformedURLException | RemoteException | NotBoundException e) {
						connectionErrorMsg.run();
						connectionErrorMsg.hide();
						e.printStackTrace();
					}
				}
			}
		});

		disconnectBtn.connect(new Button.Clicked() {

			@Override
			public void onClicked(Button arg0) {
				connected = false;
				connectBtn.setSensitive(true);
				disconnectBtn.setSensitive(false);
				ipAddrEntry.setSensitive(true);
				portEntry.setSensitive(true);
				serverNameEntry.setSensitive(true);
				try {
					logsTextView.getBuffer().insert(logsTextView.getBuffer().getIterEnd(),
							"[" + displayDateFormat.format(new Date(System.currentTimeMillis())) + "] "
									+ server.unregisterClient(client) + urlString + "\n");
				} catch (RemoteException e) {
					lostConnMsg.run();
					lostConnMsg.hide();
					e.printStackTrace();
				}
			}
		});

		Gtk.main();
	}
}
