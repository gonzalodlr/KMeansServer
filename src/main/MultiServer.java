package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The MultiServer class creates a server that listens for incoming client
 * connections on a specified port and creates a new thread for each client
 * connection that will be handle by the class 'ServerOneClient'.
 */
public class MultiServer {
	/** The port in which the server is listening */
	private static final int PORT = 8080;

	/**
	 * This is the main function that creates a new MultiServer object with a
	 * specified port number.
	 */
	public static void main(String[] args) {
		new MultiServer(PORT);
	}

	/**
	 * Constructor method that takes in an integer 'port' as a parameter. When an
	 * instance of the 'Multiserver class' is created with a specific port, this
	 * constructor is called and calls the 'run()' method, that creates a
	 * `ServerSocket` and listens for incoming client connections on the port.
	 * 
	 * @param port integer 'port' where the server is run
	 */
	public MultiServer(int port) {
		run();
	}

	/**
	 * This function creates a server socket on the 'port' and waits for client
	 * connections, when a client connects the server on the port, a thread handle
	 * by the class 'ServerOneClient' is created, then the thread is started.
	 * 
	 * @throws IOException
	 */
	public void run() {
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(PORT);
			System.out.println("Server waiting for response on port " + PORT);
			while (true) {
				Socket socket = serverSocket.accept();
				System.out.println("Client conected: " + socket.getInetAddress().getHostAddress());
				ServerOneClient oneClient = new ServerOneClient(socket);
				oneClient.start();
			}
		} catch (IOException e) {
			System.out.println("Error starting the server: " + e.getMessage());
		}
	}
}
