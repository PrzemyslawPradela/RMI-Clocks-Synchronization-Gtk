package rmi.clocks.synchronization.gtk.server;

import org.gnome.gtk.TextView;
import rmi.clocks.synchronization.gtk.common.IClient;
import rmi.clocks.synchronization.gtk.common.IServer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class Server extends UnicastRemoteObject implements IServer {
	private static final long serialVersionUID = 1L;

	private final Vector<IClient> connectedClients;
	private int clockSyncFreq;
	private final TextView logsTextView;

	Server(TextView logsTextView) throws RemoteException {
		this.connectedClients = new Vector<>();
		this.clockSyncFreq = 0;
		this.logsTextView = logsTextView;
	}

	@Override
	public String registerClient(IClient client) {
		this.connectedClients.add(client);
		SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		logsTextView.getBuffer().insert(logsTextView.getBuffer().getIterEnd(),
				"[" + displayDateFormat.format(new Date(System.currentTimeMillis())) + "] Połączył sie nowy klient!\n");
		return "Połączono z serwerem: ";
	}

	@Override
	public String unregisterClient(IClient client) {
		this.connectedClients.remove(client);
		SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		logsTextView.getBuffer().insert(logsTextView.getBuffer().getIterEnd(),
				"[" + displayDateFormat.format(new Date(System.currentTimeMillis()))
						+ "] Jeden klient zakonczył połaczenie z serwerem!\n");
		return "Rozłączono z serwerem: ";
	}

	Vector<IClient> getConnectedClients() {
		return connectedClients;
	}

	int getClockSyncFreq() {
		return clockSyncFreq;
	}

	void setClockSyncFreq(int clockSyncFreq) {
		this.clockSyncFreq = clockSyncFreq;
	}
}
