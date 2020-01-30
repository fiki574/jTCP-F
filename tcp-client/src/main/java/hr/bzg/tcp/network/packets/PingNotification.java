package hr.bzg.tcp.network.packets;

import static hr.bzg.tcp.network.PacketId.*;

import java.io.*;

import hr.bzg.tcp.network.*;

@PacketHandler(packetId = PING_NOTIFICATION)
public final class PingNotification {
	public void handlePacket(Client client, byte[] packet) {
		try {
			ByteArrayInputStream bytes = new ByteArrayInputStream(packet);
			DataInputStream input = new DataInputStream(bytes);
			String s = input.readUTF();
			if (!s.equals("ping"))
				if (client.getPingMismatchAction() != null)
					client.getPingMismatchAction().perform(client);
			input.close();
			bytes.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}