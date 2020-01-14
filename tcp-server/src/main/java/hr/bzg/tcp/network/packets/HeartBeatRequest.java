package hr.bzg.tcp.network.packets;

import static hr.bzg.tcp.network.PacketId.*;

import java.io.*;

import hr.bzg.tcp.network.*;
import hr.bzg.tcp.utilities.*;

@PacketHandler(packetId = HEART_BEAT_REQUEST)
public final class HeartBeatRequest {
	public void handlePacket(Client client, byte[] packet) {
		try {
			ByteArrayInputStream bytes = new ByteArrayInputStream(packet);
			DataInputStream input = new DataInputStream(bytes);
			int heartBeats = input.readInt();
			if (client.getHeartBeats() == heartBeats)
				client.increaseHeartBeats();
			else
				Log.warn("HeartBeatRequest.handlePacket()",
						"Heartbeat mismatch for user '" + client.getUser().getUsername() + "'");
			client.sendHeartBeatResponse();
		} catch (Exception ex) {
			Log.error("HeartBeatRequest.handlePacket()", ex.toString());
			ex.printStackTrace();
		}
	}
}