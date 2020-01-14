package hr.bzg.tcp.utilities;

import static hr.bzg.tcp.utilities.Constants.*;

import java.math.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public final class Crypto {
	private Crypto() {
	}

	public static String hash(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance(CRYPTO_ALG_HASH);
			byte[] messageDigest = md.digest(input.getBytes());
			BigInteger no = new BigInteger(1, messageDigest);
			String hashtext = no.toString(16);
			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}
			return hashtext;
		} catch (Exception ex) {
			Log.error("Crypto.Sha512Hash()", ex.getMessage());
			ex.printStackTrace();
			return null;
		}
	}

	public static byte[] encrypt(String key, byte[] input) {
		return doCrypto(Cipher.ENCRYPT_MODE, key, input);
	}

	public static byte[] decrypt(String key, byte[] input) {
		return doCrypto(Cipher.DECRYPT_MODE, key, input);
	}

	private static byte[] doCrypto(int cipherMode, String key, byte[] input) {
		try {
			Key secretKey = new SecretKeySpec(key.getBytes(), CRYPTO_ALG_TRANS);
			Cipher cipher = Cipher.getInstance(CRYPTO_ALG_TRANS);
			cipher.init(cipherMode, secretKey);
			return cipher.doFinal(input);
		} catch (Exception ex) {
			if (!ex.getMessage().equals("Given final block not properly padded")) {
				Log.error("Crypto.doCrypto()", ex.getMessage());
				ex.printStackTrace();
			}
			return null;
		}
	}
}