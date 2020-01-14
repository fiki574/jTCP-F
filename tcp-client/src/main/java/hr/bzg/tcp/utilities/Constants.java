package hr.bzg.tcp.utilities;

public final class Constants {
	private Constants() {
	}

	public static final int EXIT_CODE_GET_INSTANCE = 1;
	public static final int EXIT_CODE_CLIENT_CONSTRUCTOR = 2;
	public static final int EXIT_CODE_SERVER_OFFLINE = 3;
	public static final int EXIT_CODE_SOCKET_CLOSED = 4;

	public static final int SERVER_PORT = 4859;
	public static final String SERVER_IP = "127.0.0.1";

	public static final int PROCESS_THREAD_SLEEP_MS = 25;
	public static final int MAIN_THREAD_SLEEP_MS = 10000;

	public static final String CRYPTO_ALG_TRANS = "AES";
	public static final String CRYPTO_FIRST_PACKET_KEY = "25d1ff3411cecb2a";
	public static final String CRYPTO_ALG_HASH = "SHA-512";
}