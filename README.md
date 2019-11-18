## Table of contents
* [General info](#general-info)
* [Technologies](#technologies)
* [Setup](#setup)

## General info
The client-server application using Java RMI and Berkeley’s Algorithm for clock synchronization on Linux machines with GNOME graphical interface
	
## Technologies
Project is created with:
* Java version: 11
* Java-Gnome version: 4.1.3
* Maven version: 3.6.0
	
## Setup
1. Install Java Runtime Environment
2. Install [Maven](https://maven.apache.org/install.html)
3. Install [java-gnome](http://java-gnome.sourceforge.net/get/)
4. Add java-gnome to the Maven local repository
   ```bash
   $ sudo mvn install:install-file -Dfile=/usr/share/java/gtk-4.1.jar -DgroupId=org.gnome \
   	-DartifactId=java-gnome -Dversion=4.1.3 -Dpackaging=jar
   ```
5. Disable NTP
6. Clone this repository
7. To run this project, compile it and run it using mvn:

    *Server*
   ```bash
   $ cd ../rmi-clocks-synchronization-gtk
   $ sudo mvn install
   $ sudo mvn exec:java -pl server -Dexec.mainClass=com.pradela.clocksynchronization.server.StartServer
   ```
    *Client*
      ```bash
      $ cd ../rmi-clocks-synchronization-gtk
      $ sudo mvn install
      $ sudo mvn exec:java -pl client -Dexec.mainClass=com.pradela.clocksynchronization.client.StartClient
      ```