package hr.bzg.tcp.network.packets;

import static hr.bzg.tcp.network.PacketId.*;
import static hr.bzg.tcp.network.Server.*;
import static hr.bzg.tcp.utilities.Converter.*;

import java.util.*;

import hr.bzg.tcp.network.*;
import hr.bzg.tcp.utilities.*;

@PacketHandler(packetId = KEY_EXCHANGE_REQUEST)
public final class KeyExchangeRequest {
	public void handlePacket(Client client, byte[] packet) {
		try {
			User user = userFromByteArray(packet);
			Optional<User> findUser = users.stream().filter(u -> u.getUsername().equals(user.getUsername())
					&& u.getPin().equals(user.getPin()) && u.getHash().equals(user.getHash())).findFirst();
			if (findUser.isPresent()) {
				User foundUser = findUser.get();
				Server.getInstance().getClients().removeIf(
						c -> c.getUser() != null && c.getUser().getUsername().equals(foundUser.getUsername()));
				if (user.compareTo(foundUser)) {
					client.setUser(foundUser);
					client.sendKeyExchangeResponse();
					client.setKeyExchangeDone(true);
					Log.info("KeyExchangeRequest.handlePacket()",
							"User '" + foundUser.getUsername() + "' has finished the key exchange");
				}
			} else {
				if (Server.getInstance().silentRemoveClient(client))
					Log.warn("KeyExchangeRequest.handlePacket()",
							"Unknown user tried to connect: " + user.getUsername());
			}
		} catch (Exception ex) {
			Log.error("KeyExchangeRequest.handlePacket()", ex.toString());
			ex.printStackTrace();
		}
	}
}