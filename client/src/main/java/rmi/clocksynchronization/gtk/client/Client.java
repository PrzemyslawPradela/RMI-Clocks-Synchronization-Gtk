package rmi.clocksynchronization.gtk.client;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.gnome.gtk.TextView;

import rmi.clocksynchronization.gtk.common.IClient;

public class Client extends UnicastRemoteObject implements IClient {
	private static final long serialVersionUID = 1L;

    private final TextView logsTextView;

    Client(TextView logsTextView) throws RemoteException {
		this.logsTextView = logsTextView;
	}

	@Override
    public long getTimeDifference(long serverTime) {
		SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		logsTextView.getBuffer().insert(logsTextView.getBuffer().getIterEnd(),
				"[" + displayDateFormat.format(new Date(System.currentTimeMillis()))
						+ "] Wysłano czas do serwera. Trwa synchronizacja czasu...\n");
		return System.currentTimeMillis() - serverTime;
	}

	@Override
    public void setSynchronizedTime(long synchronizedTime) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date synchronizeDate = new Date(synchronizedTime);
		ProcessBuilder setTime = new ProcessBuilder("bash", "-c",
				"date -s '" + simpleDateFormat.format(synchronizeDate) + "'");
		try {
			setTime.start();
			TimeUnit.SECONDS.sleep(1);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		logsTextView.getBuffer().insert(logsTextView.getBuffer().getIterEnd(),
				"[" + displayDateFormat.format(new Date(System.currentTimeMillis()))
						+ "] Ustawiono czas otrzymany od serwera\n\n");
	}

}
