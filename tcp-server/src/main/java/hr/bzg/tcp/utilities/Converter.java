package hr.bzg.tcp.utilities;

import java.io.*;

import hr.bzg.tcp.network.*;

public final class Converter {
	private Converter() {
	}

	public static PacketId intToPacketId(int packetId) {
		PacketId pId = PacketId.INVALID;
		for (PacketId id : PacketId.values())
			if (id.getId() == packetId) {
				pId = id;
				break;
			}
		return pId;
	}

	public static User userFromByteArray(byte[] data) {
		try {
			ByteArrayInputStream bytes = new ByteArrayInputStream(data);
			DataInputStream input = new DataInputStream(bytes);
			String username = input.readUTF();
			String pin = input.readUTF();
			String hash = input.readUTF();
			String cryptoKey = input.readUTF();
			String fileKey = input.readUTF();
			return new User(username, pin, hash, cryptoKey, fileKey);
		} catch (Exception ex) {
			Log.error("Converter.userFromByteArray()", ex.getMessage());
			ex.printStackTrace();
			return null;
		}
	}
}