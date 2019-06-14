package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.gnome.gtk.TextView;

import common.IClient;
import common.IServer;

public class Server extends UnicastRemoteObject implements IServer {
	private static final long serialVersionUID = 1L;

	private Vector<IClient> connectedClients;
	private int clockSyncFreq;
	private TextView logsTextView;

	public Server(TextView logsTextView) throws RemoteException {
		this.connectedClients = new Vector<IClient>();
		this.clockSyncFreq = 0;
		this.logsTextView = logsTextView;
	}

	@Override
	public String registerClient(IClient client) throws RemoteException {
		this.connectedClients.add(client);
		SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		logsTextView.getBuffer().insert(logsTextView.getBuffer().getIterEnd(),
				"[" + displayDateFormat.format(new Date(System.currentTimeMillis())) + "] Połączył sie nowy klient!\n");
		return "Połączono z serwerem: ";
	}

	@Override
	public String unregisterClient(IClient client) throws RemoteException {
		this.connectedClients.remove(client);
		SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		logsTextView.getBuffer().insert(logsTextView.getBuffer().getIterEnd(),
				"[" + displayDateFormat.format(new Date(System.currentTimeMillis()))
						+ "] Jeden klient zakonczył połaczenie z serwerem!\n");
		return "Rozłączono z serwerem: ";
	}

	public Vector<IClient> getConnectedClients() {
		return connectedClients;
	}

	public int getClockSyncFreq() {
		return clockSyncFreq;
	}

	public void setClockSyncFreq(int clockSyncFreq) {
		this.clockSyncFreq = clockSyncFreq;
	}
}
