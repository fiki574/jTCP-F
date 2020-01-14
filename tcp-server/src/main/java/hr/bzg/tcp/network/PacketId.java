package hr.bzg.tcp.network;

public enum PacketId {
	INVALID(0), KEY_EXCHANGE_REQUEST(1), KEY_EXCHANGE_RESPONSE(2), HEART_BEAT_REQUEST(3), HEART_BEAT_RESPONSE(4),
	PING_NOTIFICATION(5);

	private final int id;

	PacketId(int id) {
		this.id = id;
	}

	public int getId() {
		return this.id;
	}
}