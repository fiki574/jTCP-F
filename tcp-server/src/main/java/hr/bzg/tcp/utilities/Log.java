package hr.bzg.tcp.utilities;

public final class Log {
	private Log() {
	}

	public static void write(String message) {
		System.out.println(message);
	}

	public static void info(String caller, String message) {
		System.out.println("[INF] " + caller + ": " + message);
	}

	public static void warn(String caller, String message) {
		System.out.println("[WRN] " + caller + ": " + message);
	}

	public static void error(String caller, String message) {
		System.out.println("[ERR] " + caller + ": " + message);
	}

	public static void debug(String caller, String message) {
		System.out.println("[DBG] " + caller + ": " + message);
	}

	public static void success(String caller, String message) {
		System.out.println("[SCS] " + caller + ": " + message);
	}
}