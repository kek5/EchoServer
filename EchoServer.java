package EchoServer;
import java.net.*;
import java.util.ArrayList;
import java.io.*;


public class EchoServer {
	private ServerSocket serverSocket = null;
	private int port;
	private boolean isRunning = false;
	private static ArrayList<ClientHandler> currentlyConnected = new ArrayList<ClientHandler>();
	private static ArrayList<ClientHandler> awaitingList = new ArrayList<ClientHandler>();
	
	private EchoServer() {
		this(7);
	}
	private EchoServer(int port) {
		this.port = port;
	}

	public void start() {
		this.isRunning = true;
		
		try {
			this.serverSocket = new ServerSocket(this.port);
			while(this.isRunning) {
				ClientHandler newClient = new ClientHandler(this.serverSocket.accept());
				if(ClientHandler.getConnectedAmount() < 2) { // if there're open place in thread pool
					EchoServer.currentlyConnected.add(newClient); // add the thread to working ones 
					newClient.start(); // and run it
				} else { // if laden
					System.out.println("This guy has to wait\n");
					EchoServer.awaitingList.add(newClient);
					newClient.await(); // add the thread to working ones 
					newClient.start(); // and run it
					 // add to awaiting list
				}				
			}
		} catch(IOException e) {
			System.out.println(e);
		} finally {
			try {
				if(this.serverSocket != null) {
					this.stop();
				}
			} catch(Exception e) {
				System.out.println(e);
			}
		}
	}
	
	public void stop() {
		this.isRunning = false;
		try {
			this.serverSocket.close();
			for(ClientHandler client : EchoServer.currentlyConnected) { // cleaning running connections
				client.interrupt();
			}
			for(ClientHandler client : EchoServer.awaitingList) { // cleaning awaiting connections
				client.interrupt();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//getters
	public boolean isRunning() {
		return this.isRunning;
	}
	public int getPort() {
		return this.port;
	}
	public static ArrayList<ClientHandler> getCurrentlyConnected() {
    	return EchoServer.currentlyConnected;
    }
    public static ArrayList<ClientHandler> getAwaitingList() {
    	return EchoServer.awaitingList;
    }
	
	//main
	public static void main(String[] args) {
		EchoServer server = new EchoServer(5566);
  		server.start();
	}
}
	
	
	
	
	
	
	