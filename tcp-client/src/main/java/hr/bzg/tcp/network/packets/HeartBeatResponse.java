package hr.bzg.tcp.network.packets;

import static hr.bzg.tcp.network.PacketId.*;

import java.io.*;

import hr.bzg.tcp.network.*;

@PacketHandler(packetId = HEART_BEAT_RESPONSE)
public final class HeartBeatResponse {
	public void handlePacket(Client client, byte[] packet) {
		try {
			ByteArrayInputStream bytes = new ByteArrayInputStream(packet);
			DataInputStream input = new DataInputStream(bytes);
			int heartBeats = input.readInt();
			client.setHeartBeats(heartBeats);
			input.close();
			bytes.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}