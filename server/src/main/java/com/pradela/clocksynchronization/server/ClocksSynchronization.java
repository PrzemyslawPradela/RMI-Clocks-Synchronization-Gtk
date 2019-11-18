package com.pradela.clocksynchronization.server;

import com.pradela.clocksynchronization.common.IClient;
import org.gnome.gtk.TextView;

import java.io.IOException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

class ClocksSynchronization implements Runnable {
	private boolean running = true;
	private final Server server;
	private final TextView logsTextView;

	ClocksSynchronization(Server server, TextView logsTextView) {
		super();
		this.server = server;
		this.logsTextView = logsTextView;
	}

	void start() {
		Thread worker = new Thread(this);
		worker.start();
	}

	void stop() {
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
					e1.printStackTrace();
				}
				logsTextView.getBuffer().insert(logsTextView.getBuffer().getIterEnd(),
						"[" + displayDateFormat.format(new Date(System.currentTimeMillis()))
								+ "] Ustawiono aktualny czas.\n");

				for (IClient client : server.getConnectedClients()) {
					try {
						client.setSynchronizedTime(synchronizedTime);
					} catch (RemoteException e) {
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
					e.printStackTrace();
				}
			}
		}
	}
}
