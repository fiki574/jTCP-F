package hr.bzg.tcp.network;

import static hr.bzg.tcp.network.PacketId.*;
import static hr.bzg.tcp.utilities.Constants.*;
import static hr.bzg.tcp.utilities.Converter.*;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

import hr.bzg.tcp.utilities.*;

public class Client {
	private Socket socket;
	private User user;
	private boolean disconnect, keyExchangeDone;
	private int heartBeats, pingCount;
	private ConcurrentHashMap<PacketId, Holder> handles;

	public Client(Socket socket) {
		this.socket = socket;
		user = null;
		disconnect = false;
		keyExchangeDone = false;
		heartBeats = 0;
		pingCount = 0;
		handles = new ConcurrentHashMap<>();
		mapPacketMethods();
	}

	private void mapPacketMethods() {
		try {
			for (PacketId pId : PacketId.values()) {
				if (pId == PacketId.INVALID)
					continue;

				Class<?> clazz = Server.getInstance().getHandler(pId);
				if (clazz == null)
					continue;

				Method handle = clazz.getMethod("handlePacket", Client.class, byte[].class);
				handles.put(pId, new Holder(clazz, handle));
			}
		} catch (Exception ex) {
			Log.error("Client.mapPacketMethods()", "Packet methods have not been mapped for a client");
			ex.printStackTrace();
		}
	}

	private void invokeHandlePacket(PacketId packetId, byte[] packet)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException,
			NoSuchMethodException, SecurityException {
		Holder holder = handles.get(packetId);
		holder.getHandle().invoke(holder.getClazz().getConstructor().newInstance(), this, packet);
	}

	public Socket getSocket() {
		return socket;
	}

	public User getUser() {
		return user;
	}

	public int getHeartBeats() {
		return heartBeats;
	}

	public boolean isDisconnected() {
		return disconnect;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setDisconnect(boolean value) {
		disconnect = value;
	}

	public void setKeyExchangeDone(boolean value) {
		keyExchangeDone = value;
	}

	public synchronized void increaseHeartBeats() {
		heartBeats++;
	}

	public void processPacket(int packetId, byte[] packetData) {
		try {
			if (disconnect) {
				Log.warn("Client.processPacket()", "Not processing packet with ID " + packetId + " because the user '"
						+ user.getUsername() + "' disconnected");
				return;
			}

			byte[] decrypted = Crypto.decrypt((!keyExchangeDone ? CRYPTO_FIRST_PACKET_KEY : user.getCryptoKey()),
					packetData);

			if (decrypted == null) {
				Log.error("Client.processPacket()", "Failed to decrypt data");
				return;
			}

			invokeHandlePacket(intToPacketId(packetId), decrypted);
		} catch (Exception ex) {
			Log.error("Client.processPacket()", ex.toString());
			ex.printStackTrace();
		}
	}

	public void sendPreparedPacket(byte[] packetData) {
		try {
			synchronized (socket) {
				DataOutputStream output = new DataOutputStream(socket.getOutputStream());
				output.write(packetData);
				output.flush();
			}
		} catch (Exception ex) {
			Log.error("Client.sendPreparedPacket()", ex.toString());
			ex.printStackTrace();
		}
	}

	public void sendPacket(PacketId packetId, byte[] packetData, boolean useFirstPacketKey) throws IOException {
		String key = "";
		if (useFirstPacketKey)
			key = CRYPTO_FIRST_PACKET_KEY;
		else
			key = user.getCryptoKey();

		byte[] encrypted = Crypto.encrypt(key, packetData);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream preOutput = new DataOutputStream(bytes);
		preOutput.writeInt(encrypted.length + 8);
		preOutput.writeInt(packetId.getId());
		preOutput.write(encrypted);
		preOutput.flush();
		synchronized (socket) {
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());
			output.write(bytes.toByteArray());
			output.flush();
		}
		preOutput.close();
		bytes.close();
	}

	public void sendKeyExchangeResponse() {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream output = new DataOutputStream(bytes);
			output.writeUTF(user.getCryptoKey());
			output.writeUTF(user.getFileKey());
			output.flush();
			sendPacket(KEY_EXCHANGE_RESPONSE, bytes.toByteArray(), true);
			output.close();
			bytes.close();
		} catch (Exception ex) {
			Log.error("Client.sendKeyExchange()", ex.toString());
			ex.printStackTrace();
		}
	}

	public void sendHeartBeatResponse() {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream output = new DataOutputStream(bytes);
			output.writeInt(heartBeats);
			output.flush();
			sendPacket(HEART_BEAT_RESPONSE, bytes.toByteArray(), false);
			output.close();
			bytes.close();
		} catch (Exception ex) {
			Log.error("Client.sendKeyExchange()", ex.toString());
			ex.printStackTrace();
		}
	}

	public void sendPing() throws IOException {
		if (!keyExchangeDone)
			return;

		if (pingCount++ < MAX_PING_COUNT)
			return;
		else
			pingCount = 0;

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream output = new DataOutputStream(bytes);
		output.writeUTF("ping");
		output.flush();
		sendPacket(PING_NOTIFICATION, bytes.toByteArray(), false);
		output.close();
		bytes.close();
	}

	public void cleanup() {
		try {
			user = null;
			disconnect = true;
			keyExchangeDone = false;
			socket.close();
			socket = null;
			System.gc();
		} catch (NullPointerException ex) {
			Log.warn("Client.cleanup()", "A socket was null while cleaning up");
		} catch (Exception ex) {
			Log.error("Client.cleanup()", ex.toString());
			ex.printStackTrace();
		}
	}
}

class Holder {
	private Class<?> clazz;
	private Method handle;

	public Holder(Class<?> clazz, Method handle) {
		this.clazz = clazz;
		this.handle = handle;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public Method getHandle() {
		return handle;
	}
}