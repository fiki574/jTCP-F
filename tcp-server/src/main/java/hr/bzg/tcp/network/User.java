package hr.bzg.tcp.network;

public final class User {
	private String username, pin, hash, cryptoKey, fileKey;

	public User(String username, String pin, String hash, String cryptoKey, String fileKey) {
		this.username = username;
		this.pin = pin;
		this.hash = hash;
		this.cryptoKey = cryptoKey;
		this.fileKey = fileKey;
	}

	public String getUsername() {
		return username;
	}

	public String getPin() {
		return pin;
	}

	public String getHash() {
		return hash;
	}

	public String getCryptoKey() {
		return cryptoKey;
	}
	
	public String getFileKey() {
		return fileKey;
	}
	
	public boolean compareTo(User user) {
		if (username.equals(user.username) && pin.equals(user.pin) && hash.equals(user.hash))
			return true;
		else
			return false;
	}
}