package hr.bzg.tcp;

import static hr.bzg.tcp.utilities.Constants.*;

import hr.bzg.tcp.network.*;
import hr.bzg.tcp.utilities.*;

public class Main {
	public static void main(String[] args) {
		runServer(args);
	}

	private static void runServer(String[] args) {
		try {
			if (!System.getProperty("os.name").toLowerCase().contains("linux") && ALLOW_ONLY_LINUX_RUNTIME)
				System.exit(EXIT_CODE_LINUX_RUNTIME);

			if (args != null && args.length == 1)
				SERVER_LISTEN_PORT = Integer.parseInt(args[0].split("--port=")[1]);
			else
				Log.warn("Main.main()", "Parameter for port definition has not been found, using the default one");

			Server server = Server.getInstance();
			Thread acceptor = new Thread(() -> server.acceptClients());
			Thread processor = new Thread(() -> server.processPackets());
			acceptor.start();
			processor.start();
			Thread.sleep(MAIN_THREAD_SLEEP_MS);
			while (server.isAccepting() && server.isProcessing())
				Thread.sleep(MAIN_THREAD_SLEEP_MS);
			server.cleanup();
			acceptor.join();
			processor.join();
			acceptor = null;
			processor = null;
			System.gc();
			Log.success("Main.runServer()", "Graceful shutdown");
		} catch (Exception ex) {
			Log.error("Main.runServer()", ex.toString());
			ex.printStackTrace();
		}
	}
}