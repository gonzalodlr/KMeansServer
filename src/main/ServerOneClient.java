package main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;

import agent.DbAccess;
import exceptions.DatabaseConnectionException;
import exceptions.EmptySetException;
import exceptions.NoValueException;
import exceptions.OutOfRangeSampleSize;
import data.Data;
import mining.KMeansMiner;

/**
 * The ServerOneClient class is a thread that handles client requests for
 * clustering data using the KMeans algorithm and saving or opening serialised
 * files.
 */
public class ServerOneClient extends Thread {
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private KMeansMiner kmeans;

	/**
	 * The constructor of the 'ServerOneClient' class. It takes a 'Socket' as a
	 * parameter and initializes the 'socket' with it. It also initializes
	 * the 'in' and 'out' with 'ObjectInputStream' and 'ObjectOutputStream'
	 * objects respectively, which are obtained from the 'socket' object.These
	 * streams are used to communicate with the client over the network. The
	 * constructor throws an 'IOException' if there is an error while creating the
	 * input and output streams.
	 * 
	 * @param socket
	 * @throws IOException
	 */
	public ServerOneClient(Socket socket) throws IOException {
		this.socket = socket;
		this.in = new ObjectInputStream(socket.getInputStream());
		this.out = new ObjectOutputStream(socket.getOutputStream());
	}

	@Override
	public void run() {
		Data data = null;
		try {
			while (true) {
				int option = (int) in.readObject();
				switch (option) {
					case 0:
						data = getConnection();
						break;
					case 1:
						clustering(data);
						break;
					case 2:
						saveFile();
						break;
					case 3:
						openFile();
						break;
					default:
						System.out.println("Invalid request");
				}
			}

		} catch (IOException e) {
			System.out.println("Error: " + e);
		} catch (ClassNotFoundException e) {
			System.out.println("Error: " + e);
		} catch (SQLException e) {
			System.out.println("Error: " + e);
		} catch (NoValueException e) {
			System.out.println("Error: " + e);
		} catch (EmptySetException e) {
			System.out.println("Error: " + e);
		} catch (DatabaseConnectionException e) {
			System.out.println("Error: " + e);
		} catch (OutOfRangeSampleSize e) {
			System.out.println("Error: " + e);
		} finally {
			// In any case, closing resources
			try {
				in.close();
				out.close();
				socket.close();
			} catch (IOException e) {
				System.out.println("Socket not closed: " + e.getMessage());
			}
		}
	}

	/**
	 * This function saves the KMeans clustering discovered to a file with the
	 * given file name by the client with the 'in' stream. Sends OK if it is
	 * correct.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void saveFile() throws FileNotFoundException, IOException, ClassNotFoundException {
		String fileName = (String) in.readObject();
		kmeans.save(fileName);
		out.writeObject("OK");
	}

	/**
	 * This function performs clustering using the KMeans algorithm on a given
	 * dataset and sends the results to the client via an output stream.
	 * 
	 * @param data a dataset of examples to be clustered
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws OutOfRangeSampleSize
	 */
	private void clustering(Data data) throws IOException, ClassNotFoundException, OutOfRangeSampleSize {
		int k = (int) in.readObject();
		kmeans = new KMeansMiner(k);
		int numIter = kmeans.kmeans(data);
		String text = "Data Examples:\n" + data + "\n\n";

		text += "Numero di Iterazione:" + numIter + "\n\n" + kmeans.getC().toString(data) + "\n";

		// All this ok, send message
		out.writeObject("OK");

		out.writeObject(text);

		String clusters = kmeans.getC().toString(data);
		out.writeObject(clusters);
	}

	/**
	 * This function opens a serialise file, it reads the table name, creates a
	 * KMeansMiner object with the table name by deserialising the file,
	 * obtains the clusters, and writes them to an output stream.
	 * 
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws OutOfRangeSampleSize
	 */
	private void openFile() throws FileNotFoundException, ClassNotFoundException, IOException, OutOfRangeSampleSize {
		String tableName = (String) in.readObject();

		kmeans = new KMeansMiner(tableName);
		String text = kmeans.getC().toString(); // Obtain the clusters

		out.writeObject("OK");
		out.writeObject(text);
	}

	/**
	 * This function receives a name of a table in a database by the client
	 * and establishes a database connection, it returns a Data object filled with
	 * the examples for the specified table name.
	 * 
	 * @return returns the Data
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws DatabaseConnectionException
	 * @throws SQLException
	 * @throws NoValueException
	 * @throws EmptySetException
	 * @throws OutOfRangeSampleSize
	 */
	private Data getConnection() throws IOException, ClassNotFoundException, DatabaseConnectionException, SQLException,
			NoValueException, EmptySetException, OutOfRangeSampleSize {
		String tableName = (String) in.readObject();
		DbAccess db = new DbAccess();
		db.initConnection();
		Data data = new Data(tableName, db);
		db.closeConnection();

		out.writeObject("OK");
		return data;
	}
}