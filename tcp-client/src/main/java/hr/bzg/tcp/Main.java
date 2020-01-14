package hr.bzg.tcp;

import static hr.bzg.tcp.utilities.Constants.*;

import hr.bzg.tcp.network.*;
import hr.bzg.tcp.utilities.*;

public class Main {
	public static void main(String[] args) {
		User user = new User("tester", "123x321", "<no-hash>", "<no-key>", "<no-key>");
		Action[] actions = new Action[] { null, null };
		runClient(user, actions);
	}

	public static void runClient(User user, Action[] actions) {
		try {
			if (user == null || actions == null | actions.length != 2)
				return;

			Client client = Client.getInstance();
			client.setPingMismatchAction(actions[0]);
			client.setClientExchangeSuccessAction(actions[1]);
			client.setUser(user);
			Thread processor = new Thread(() -> client.processPackets());
			processor.start();
			client.sendKeyExchangeRequest();
			while (client.isRunning()) {
				Thread.sleep(MAIN_THREAD_SLEEP_MS);
				client.sendHeartBeat();
			}
			client.cleanup();
			processor.join();
			user = null;
			processor = null;
			System.gc();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}