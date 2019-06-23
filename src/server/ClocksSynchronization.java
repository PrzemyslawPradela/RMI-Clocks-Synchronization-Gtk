package server;

import java.io.IOException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.gnome.gtk.TextView;

import common.IClient;

public class ClocksSynchronization implements Runnable {
	private Thread worker;
	private boolean running = true;
	private Server server;
	private TextView logsTextView;

	public ClocksSynchronization(Server server, TextView logsTextView) {
		super();
		this.server = server;
		this.logsTextView = logsTextView;
	}

	public void start() {
		worker = new Thread(this);
		worker.start();
	}

	public void stop() {
		running = false;
	}

	@Override
	public void run() {
		SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		while (running) {
			if (server.getConnectedClients().isEmpty()) {
				logsTextView.getBuffer().insert(logsTextView.getBuffer().getIterEnd(),
						"[" + displayDateFormat.format(new Date(System.currentTimeMillis()))
								+ "] Oczekiwanie na połączenie klientów...\n");
				try {
					TimeUnit.SECONDS.sleep(5);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				logsTextView.getBuffer().insert(logsTextView.getBuffer().getIterEnd(),
						"[" + displayDateFormat.format(new Date(System.currentTimeMillis()))
								+ "] Rozpoczeto synchronizację czasu.\n");
				logsTextView.getBuffer().insert(logsTextView.getBuffer().getIterEnd(),
						"[" + displayDateFormat.format(new Date(System.currentTimeMillis()))
								+ "] Oczekiwanie na czas od klientów...\n");

				long differencesSum = 0;
				long difference;
				long delta = 604800000; // tydzien w milisekundach
				int rejectedDifferences = 0;
				long serverTime = System.currentTimeMillis();
				for (IClient client : server.getConnectedClients()) {
					try {
						difference = client.getTimeDifference(serverTime);
						if (Math.abs(difference) > delta) {
							difference = 0;
							rejectedDifferences++;
						}
						differencesSum += difference;
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				long average = differencesSum / (server.getConnectedClients().size() - rejectedDifferences + 1);
				long synchronizedTime = serverTime + average;

				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date synchronizedDate = new Date(synchronizedTime);
				ProcessBuilder setTime = new ProcessBuilder("bash", "-c",
						"date -s '" + simpleDateFormat.format(synchronizedDate) + "'");
				try {
					setTime.start();
					TimeUnit.SECONDS.sleep(1);
				} catch (IOException | InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				logsTextView.getBuffer().insert(logsTextView.getBuffer().getIterEnd(),
						"[" + displayDateFormat.format(new Date(System.currentTimeMillis()))
								+ "] Ustawiono aktualny czas.\n");

				for (IClient client : server.getConnectedClients()) {
					try {
						client.setSynchronizedTime(synchronizedTime);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				logsTextView.getBuffer().insert(logsTextView.getBuffer().getIterEnd(),
						"[" + displayDateFormat.format(new Date(System.currentTimeMillis()))
								+ "] Wysłano aktualny czas do klientów.\n");
				logsTextView.getBuffer().insert(logsTextView.getBuffer().getIterEnd(),
						"[" + displayDateFormat.format(new Date(System.currentTimeMillis()))
								+ "] Synchronizacja czasu zakończona.\n\n");
				
				logsTextView.getBuffer().insert(logsTextView.getBuffer().getIterEnd(),
						"[" + displayDateFormat.format(new Date(System.currentTimeMillis()))
								+ "] Synchronizacja czasu rozpocznie się za " + server.getClockSyncFreq()
								+ " sekund\n");

				try {
					TimeUnit.SECONDS.sleep(server.getClockSyncFreq());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
