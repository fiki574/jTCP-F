package hr.bzg.tcp.network;

import java.io.*;

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

	public void setCryptoKey(String key) {
		cryptoKey = key;
	}
	
	public void setFileKey(String key) {
		fileKey = key;
	}

	public byte[] toByteArray() {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream output = new DataOutputStream(bytes);
			output.writeUTF(username);
			output.writeUTF(pin);
			output.writeUTF(hash);
			output.writeUTF(cryptoKey);
			output.writeUTF(fileKey);
			output.flush();
			return bytes.toByteArray();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}