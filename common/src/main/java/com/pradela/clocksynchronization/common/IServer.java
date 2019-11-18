package com.pradela.clocksynchronization.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IServer extends Remote {
	String registerClient(IClient client) throws RemoteException;

	String unregisterClient(IClient client) throws RemoteException;
}
