package hr.bzg.tcp.network;

import static hr.bzg.tcp.network.PacketId.*;
import static hr.bzg.tcp.utilities.Constants.*;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import org.reflections.*;

import hr.bzg.tcp.utilities.*;

public final class Client {
	private static Client instance;
	private Socket server;
	private boolean isRunning, keyExchangeDone;
	private ConcurrentHashMap<PacketId, Class<?>> handlers;
	private ConcurrentHashMap<PacketId, Method> handles;
	private User user;
	private Action pingMismatchAction, clientExchangeSuccessAction;
	private int heartBeats;

	synchronized public static Client getInstance() {
		try {
			if (instance == null)
				synchronized (Client.class) {
					if (instance == null)
						instance = new Client();
				}
			return instance;
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(EXIT_CODE_GET_INSTANCE);
		}
		return null;
	}

	private Client() {
		try {
			server = new Socket(SERVER_IP, SERVER_PORT);
			handlers = new ConcurrentHashMap<>();
			handles = new ConcurrentHashMap<>();
			user = null;
			pingMismatchAction = null;
			clientExchangeSuccessAction = null;
			heartBeats = 0;
			if (server.isConnected()) {
				mapPacketHandlers();
				mapPacketMethods();
			} else
				System.exit(EXIT_CODE_CLIENT_CONSTRUCTOR);
		} catch (Exception ex) {
			if (!ex.toString().contains("Connection refused"))
				ex.printStackTrace();
			System.exit(EXIT_CODE_CLIENT_CONSTRUCTOR);
		}
		isRunning = false;
		keyExchangeDone = false;
	}

	private void mapPacketHandlers() {
		try {
			Set<Class<?>> annotatedClasses = new Reflections("hr.bzg.tcp").getTypesAnnotatedWith(PacketHandler.class);
			annotatedClasses.forEach(annotatedClass -> handlers.put(
					PacketId.valueOf(annotatedClass.getAnnotation(PacketHandler.class).packetId().toString()),
					annotatedClass));
		} catch (Exception ex) {
			ex.printStackTrace();
			isRunning = false;
		}
	}

	private void mapPacketMethods() {
		try {
			for (PacketId pId : PacketId.values()) {
				if (pId == PacketId.INVALID)
					continue;

				Class<?> c = handlers.get(pId);
				if (c == null)
					continue;

				Method handle = c.getMethod("handlePacket", Client.class, byte[].class);
				handles.put(pId, handle);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private PacketId intToPacketId(int packetId) {
		PacketId pId = PacketId.INVALID;
		for (PacketId id : PacketId.values())
			if (id.getId() == packetId) {
				pId = id;
				break;
			}
		return pId;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setKeyExchangeDone(boolean value) {
		keyExchangeDone = value;
	}

	public void setPingMismatchAction(Action action) {
		pingMismatchAction = action;
	}

	public void setClientExchangeSuccessAction(Action action) {
		clientExchangeSuccessAction = action;
	}

	public void setHeartBeats(int heartBeats) {
		this.heartBeats = heartBeats;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public User getUser() {
		return user;
	}

	public Action getPingMismatchAction() {
		return pingMismatchAction;
	}

	public Action getClientExchangeSuccessAction() {
		return clientExchangeSuccessAction;
	}

	public void processPackets() {
		try {
			isRunning = true;
			while (isRunning) {
				DataInputStream in = new DataInputStream(server.getInputStream());
				int packetLength = in.readInt();
				if (packetLength > 0) {
					int packetId = in.readInt();
					byte[] packetData = new byte[packetLength - 8];
					in.readFully(packetData, 0, packetData.length);

					byte[] decrypted = Crypto
							.decrypt((!keyExchangeDone ? CRYPTO_FIRST_PACKET_KEY : user.getCryptoKey()), packetData);

					invokeHandlePacket(intToPacketId(packetId), decrypted);
				}
				Thread.sleep(PROCESS_THREAD_SLEEP_MS);
			}
		} catch (EOFException ex) {
			isRunning = false;
			System.exit(EXIT_CODE_SOCKET_CLOSED);
		} catch (Exception ex) {
			isRunning = false;
			if (ex.toString().contains("Connection reset"))
				System.exit(EXIT_CODE_SERVER_OFFLINE);

			if (!ex.toString().contains("Socket closed"))
				ex.printStackTrace();
		}
	}

	public void invokeHandlePacket(PacketId packetId, byte[] packet)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException,
			NoSuchMethodException, SecurityException {
		Class<?> clazz = handlers.get(packetId);
		Method handle = handles.get(packetId);
		handle.invoke(clazz.getConstructor().newInstance(), this, packet);
	}

	public void sendPacket(PacketId packetId, byte[] packetData, boolean useFirstPacketKey) {
		try {
			byte[] encrypted = Crypto.encrypt((useFirstPacketKey ? CRYPTO_FIRST_PACKET_KEY : user.getCryptoKey()),
					packetData);

			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream preOutput = new DataOutputStream(bytes);
			preOutput.writeInt(encrypted.length + 8);
			preOutput.writeInt(packetId.getId());
			preOutput.write(encrypted);
			preOutput.flush();
			DataOutputStream output = new DataOutputStream(server.getOutputStream());
			output.write(bytes.toByteArray());
			output.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
			isRunning = false;
		}
	}

	public void sendKeyExchangeRequest() {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream output = new DataOutputStream(bytes);
			output.write(user.toByteArray());
			output.flush();
			sendPacket(KEY_EXCHANGE_REQUEST, bytes.toByteArray(), true);
			output.close();
			bytes.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			isRunning = false;
		}
	}

	public void sendHeartBeat() {
		try {
			if (!keyExchangeDone)
				return;

			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream output = new DataOutputStream(bytes);
			output.writeInt(heartBeats++);
			output.flush();
			sendPacket(HEART_BEAT_REQUEST, bytes.toByteArray(), false);
			output.close();
			bytes.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			isRunning = false;
		}
	}

	public void cleanup() {
		try {
			handlers.clear();
			handlers = null;
			user = null;
			heartBeats = 0;
			isRunning = false;
			keyExchangeDone = false;
			server.close();
			server = null;
			System.gc();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}