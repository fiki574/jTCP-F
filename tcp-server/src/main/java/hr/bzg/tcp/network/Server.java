package hr.bzg.tcp.network;

import static hr.bzg.tcp.utilities.Constants.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import org.reflections.*;

import hr.bzg.tcp.utilities.*;

public final class Server {
	public static ConcurrentLinkedQueue<User> users = new ConcurrentLinkedQueue<User>() {
		private static final long serialVersionUID = 1L;
		{
			add(new User("test", "123x321", "<no-hash>", "29fe32ab11de345f", "a77bd2156abdc137"));
		}
	};
	
	private static Server instance;
	private ServerSocket serverSocket;
	private ConcurrentHashMap<PacketId, Class<?>> handlers;
	private ConcurrentLinkedQueue<Client> clients;
	private ThreadPoolExecutor executor;
	private boolean isAccepting, isProcessing;

	synchronized public static Server getInstance() {
		try {
			if (instance == null)
				synchronized (Server.class) {
					if (instance == null)
						instance = new Server();
				}
			return instance;
		} catch (Exception ex) {
			Log.error("Server.getInstace()", "Couldn't retrieve server instance, exiting");
			ex.printStackTrace();
			System.exit(EXIT_CODE_GET_INSTANCE);
		}
		return null;
	}

	private Server() {
		try {
			clients = new ConcurrentLinkedQueue<>();
			handlers = new ConcurrentHashMap<>();
			executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_THREAD_POOL_SIZE);
			executor.prestartAllCoreThreads();
			serverSocket = new ServerSocket(SERVER_LISTEN_PORT, MAX_ACCEPT_CONNECTION);
			if (serverSocket.isBound()) {
				mapPacketHandlers();
				Log.success("Server.Server()",
						"Socket is properly bound, server is started on port " + SERVER_LISTEN_PORT);
			} else {
				Log.error("Server.Server()", "Server has not been bound, exiting");
				System.exit(EXIT_CODE_SERVER_CONSTRUCTOR);
			}
		} catch (Exception ex) {
			Log.error("Server.Server()", ex.toString());
			ex.printStackTrace();
		}
		isAccepting = false;
		isProcessing = false;
	}

	private void mapPacketHandlers() {
		try {
			Set<Class<?>> annotatedClasses = new Reflections("hr.bzg.tcp")
					.getTypesAnnotatedWith(PacketHandler.class);
			annotatedClasses.forEach(annotatedClass -> handlers.put(
					PacketId.valueOf(annotatedClass.getAnnotation(PacketHandler.class).packetId().toString()),
					annotatedClass));
			Log.success("Server.mapPacketHandlers()",
					"Mapped total of " + handlers.mappingCount() + " packet handlers");
		} catch (Exception ex) {
			Log.error("Server.mapPacketHandlers()", "Packet handlers have not been mapped, exiting");
			ex.printStackTrace();
			System.exit(EXIT_CODE_MAP_HANDLERS);
		}
	}

	private void removeClient(Client client) {
		try {
			if (client == null)
				return;

			client.setDisconnect(true);

			if (client.getUser() != null) {
				Log.info("Server.removeClient()", "User '" + client.getUser().getUsername() + "' has disconnected");
			} else
				Log.info("Server.removeClient()", "An unknown user has disconnected");

			clients.remove(client);
			client.cleanup();
			client = null;
			System.gc();
		} catch (Exception ex) {
			Log.error("Server.removeClient()", ex.toString());
			ex.printStackTrace();
		}
	}

	private void checkIncomingData(Client client) {
		try {
			if (client == null || client.getSocket() == null || client.getSocket().getInputStream() == null
					|| client.isDisconnected())
				return;

			DataInputStream in = new DataInputStream(client.getSocket().getInputStream());
			if (in.available() > 0) {
				int packetLength = in.readInt();
				if (packetLength > 0) {
					int packetId = in.readInt();
					byte[] packetData = new byte[packetLength - 8];
					in.readFully(packetData, 0, packetData.length);
					executor.execute(() -> {
						client.processPacket(packetId, packetData);
					});
				}
			} else
				client.sendPing();
		} catch (EOFException ex) {
			if (client != null)
				removeClient(client);
		} catch (Exception ex) {
			if (ex.toString().contains("Connection reset") && client != null
					|| ex.toString().contains("Socket closed") && client != null
					|| ex.toString().contains("Software caused connection abort") && client != null)
				removeClient(client);
			else {
				Log.error("Server.checkIncomingData()", ex.toString());
				ex.printStackTrace();
			}
		}
	}

	private void closeAcceptor() {
		try {
			Log.warn("Server.closeAcceptor()", "Server is no longer accepting incoming connections");
			isAccepting = false;
			System.gc();
		} catch (Exception ex) {
			Log.error("Server.closeAcceptor()", ex.toString());
			ex.printStackTrace();
		}
	}

	private void closeProcessor() {
		try {
			Log.warn("Server.processPackets()", "Server is no longer processing packets");
			isProcessing = false;
			System.gc();
		} catch (Exception ex) {
			Log.error("Server.closeProcessor()", ex.toString());
			ex.printStackTrace();
		}
	}

	public void acceptClients() {
		try {
			isAccepting = true;
			Log.info("Server.acceptClients()", "Client acceptor thread has been started");
			while (isAccepting) {
				Socket socket = serverSocket.accept();
				if (socket.isConnected()) {
					Log.info("Server.acceptClients()",
							"New connection from " + socket.getRemoteSocketAddress().toString());
					Client client = new Client(socket);
					checkIncomingData(client);
					clients.add(client);
				}
				Thread.sleep(ACCEPT_THREAD_SLEEP_MS);
			}
		} catch (Exception ex) {
			if (!ex.toString().contains("socket closed")) {
				Log.error("Server.acceptClients()", ex.toString());
				ex.printStackTrace();
			}
			isAccepting = false;
		}
		closeAcceptor();
	}

	public void processPackets() {
		try {
			isProcessing = true;
			Log.info("Server.processPackets()", "Packet processor thread has been started");
			while (isProcessing) {
				clients.forEach(client -> checkIncomingData(client));

				if (executor.getActiveCount() <= 0)
					executor.purge();

				Thread.sleep(PROCESS_THREAD_SLEEP_MS);
			}
		} catch (Exception ex) {
			isProcessing = false;
			Log.error("Server.processPackets()", ex.toString());
			ex.printStackTrace();
		}
		closeProcessor();
	}

	public boolean isAccepting() {
		return isAccepting;
	}

	public boolean isProcessing() {
		return isProcessing;
	}

	public ConcurrentLinkedQueue<Client> getClients() {
		return clients;
	}

	public Class<?> getHandler(PacketId packetId) {
		return handlers.get(packetId);
	}

	public Optional<Client> getClientByUsername(String username) {
		return clients.stream().filter(client -> client.getUser().getUsername().equals(username)).findFirst();
	}
	
	public boolean silentRemoveClient(Client client) {
		return clients.remove(client);
	}

	public void cleanup() {
		try {
			serverSocket.close();
			clients.forEach(client -> client.cleanup());
			clients.clear();
			handlers.clear();
			executor.shutdown();
			serverSocket = null;
			clients = null;
			handlers = null;
			executor = null;
			isAccepting = false;
			isProcessing = false;
			System.gc();
		} catch (Exception ex) {
			Log.error("Server.cleanup()", ex.toString());
			ex.printStackTrace();
		}
	}
}