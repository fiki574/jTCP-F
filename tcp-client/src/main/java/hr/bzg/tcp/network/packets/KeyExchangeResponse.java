package hr.bzg.tcp.network.packets;

import static hr.bzg.tcp.network.PacketId.*;

import java.io.*;

import hr.bzg.tcp.network.*;

@PacketHandler(packetId = KEY_EXCHANGE_RESPONSE)
public final class KeyExchangeResponse {
	public void handlePacket(Client client, byte[] packet) {
		try {
			ByteArrayInputStream bytes = new ByteArrayInputStream(packet);
			DataInputStream input = new DataInputStream(bytes);
			String cryptoKey = input.readUTF();
			String fileKey = input.readUTF();
			client.getUser().setCryptoKey(cryptoKey);
			client.getUser().setFileKey(fileKey);
			client.setKeyExchangeDone(true);
			if (client.getClientExchangeSuccessAction() != null)
				client.getClientExchangeSuccessAction().perform(client);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}