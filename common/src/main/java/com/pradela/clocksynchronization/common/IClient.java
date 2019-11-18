package com.pradela.clocksynchronization.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IClient extends Remote {
	long getTimeDifference(long serverTime) throws RemoteException;

	void setSynchronizedTime(long synchronizedTime) throws RemoteException;
}
