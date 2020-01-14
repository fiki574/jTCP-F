package hr.bzg.tcp.utilities;

public final class Constants {
	private Constants() {
	}

	public static final int EXIT_CODE_GET_INSTANCE = 1;
	public static final int EXIT_CODE_SERVER_CONSTRUCTOR = 2;
	public static final int EXIT_CODE_MAP_HANDLERS = 3;
	public static final int EXIT_CODE_LINUX_RUNTIME = 4;

	public static final int PROCESS_THREAD_SLEEP_MS = 25;
	public static final int ACCEPT_THREAD_SLEEP_MS = 50;
	public static final int MAIN_THREAD_SLEEP_MS = 10000;

	public static final int MAX_THREAD_POOL_SIZE = 16;
	public static final int MAX_ACCEPT_CONNECTION = 64;
	public static final int MAX_PING_COUNT = PROCESS_THREAD_SLEEP_MS * 5;

	public static final String CRYPTO_ALG_TRANS = "AES";
	public static final String CRYPTO_FIRST_PACKET_KEY = "25d1ff3411cecb2a";
	public static final String CRYPTO_ALG_HASH = "SHA-512";

	public static int SERVER_LISTEN_PORT = 4859;
	public static boolean ALLOW_ONLY_LINUX_RUNTIME = false;
}