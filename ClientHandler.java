package EchoServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler extends Thread {
	private Socket clientSocket;
	private int uniqueId;
	private static int idCounter = 1;
	private static int connectedAmount = 0;
	private PrintWriter toTheClient = null;
	private BufferedReader fromTheClient = null;
	private String output;
	
    public ClientHandler(Socket socket) {
    	this.uniqueId = ClientHandler.idCounter;
    	this.clientSocket = socket;
		ClientHandler.connectedAmount++;
		ClientHandler.idCounter++;
		
    }
    
    //getters
    public static int getConnectedAmount() {
    	return ClientHandler.connectedAmount;
    }
    public Socket getClientSocket() {
    	return this.clientSocket;
    }
    public int getUniquieId() {
    	return this.uniqueId;
    }

	// @Runnable
	public void run() {		
		System.out.println("one run\n");
		try {
			toTheClient = new PrintWriter(this.clientSocket.getOutputStream());
			fromTheClient = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
			toTheClient.write("You are connected. Type 'exit' to quit\n");
            toTheClient.flush();
            
            
            
            while( (output = fromTheClient.readLine()) != null && !this.clientSocket.isClosed()) {
            	if(!output.isEmpty()) {
	            	toTheClient.write("ECHO: " + output + "\n");
	            	if(output.equals("exit")) {
	            		this.clientSocket.close();
	            	}
	            	toTheClient.flush();
            	}
            	
            }
		} catch(IOException e) {
			System.out.println("userId" + this.uniqueId + " just broke the connection\n");
		} finally {
			toTheClient.close();
			try {
				fromTheClient.close();
				if(!this.clientSocket.isClosed()) {					
					this.clientSocket.close();
				}
				EchoServer.getCurrentlyConnected().remove(this); // remove this from current connections
			} catch (IOException e) {
				e.printStackTrace();
			}
		} // when thread is done running
		ClientHandler.connectedAmount--; // decrement amount of connections
		if(!EchoServer.getAwaitingList().isEmpty()) { // if any client is awaiting for connection
		    synchronized (EchoServer.getAwaitingList().get(0)) {
				EchoServer.getAwaitingList().get(0).notify();
			    EchoServer.getCurrentlyConnected().add(EchoServer.getAwaitingList().get(0)); // add to connected list
			    EchoServer.getAwaitingList().remove(0); // remove just added thread from awaiting list
		    }		
		}
	}

	public synchronized void await() {
		try {
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
