package atdl.thesis.rsa.service;

import atdl.thesis.rsa.model.RSAKeyPair;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Service
public class RSAService {

    private Charset charSet = StandardCharsets.ISO_8859_1;

    public RSAKeyPair generateKey() {

        BigInteger p, q, n, phi, e, d;
        int bitLength = 1024;
        java.security.SecureRandom random = new java.security.SecureRandom();

        p = BigInteger.probablePrime(bitLength / 2, random);
        q = BigInteger.probablePrime(bitLength / 2, random);
        n = p.multiply(q);
        phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        e = BigInteger.valueOf(65537); // Common choice for e

        // Calculate d
        d = e.modInverse(phi);

        RSAKeyPair pair = new RSAKeyPair();
        pair.setPublicKey(e + "." + n);
        pair.setPrivateKey(d + "." + n);

        return pair;
    }

    public String encryptMessage(String message, String publicKeyPair) {

        BigInteger publicKey = new  BigInteger(publicKeyPair.split("\\.")[0]);
        BigInteger modulus = new BigInteger(publicKeyPair.split("\\.")[1]);

        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        int chunkSize = modulus.bitLength() / 8 - 1;
        int numChunks = (int) Math.ceil((double) messageBytes.length / chunkSize);

        BigInteger[] encryptedChunks = new BigInteger[numChunks];

        for (int i = 0; i < numChunks; i++) {
            int start = i * chunkSize;
            int length = Math.min(chunkSize, messageBytes.length - start);

            byte[] chunk = new byte[length];
            System.arraycopy(messageBytes, start, chunk, 0, length);

            BigInteger chunkBigInt = new BigInteger(1, chunk);
            encryptedChunks[i] = encrypt(chunkBigInt, publicKey, modulus);
        }

        // Convert encrypted chunks to Base64 string
        StringBuilder encryptedMessage = new StringBuilder();
        for (BigInteger chunk : encryptedChunks) {
            encryptedMessage.append(Base64.getEncoder().encodeToString(chunk.toByteArray())).append(",");
        }

        // Remove the last comma
        encryptedMessage.setLength(encryptedMessage.length() - 1);

        return encryptedMessage.toString();
    }

    public String decryptMessage(String encryptedMessage, String privateKeyPair) {

        BigInteger privateKey = new BigInteger(privateKeyPair.split("\\.")[0]);
        BigInteger modulus = new BigInteger(privateKeyPair.split("\\.")[1]);

        String[] encryptedChunks = encryptedMessage.split(",");
        int numChunks = encryptedChunks.length;
        BigInteger[] decryptedChunks = new BigInteger[numChunks];

        for (int i = 0; i < numChunks; i++) {
            byte[] chunkBytes = Base64.getDecoder().decode(encryptedChunks[i]);
            BigInteger encryptedChunk = new BigInteger(chunkBytes);
            decryptedChunks[i] = decrypt(encryptedChunk, privateKey, modulus);
        }

        byte[] decryptedMessageBytes = new byte[0];
        int pos = 0;
        for (BigInteger chunk : decryptedChunks) {
            byte[] chunkBytes = chunk.toByteArray();
            byte[] newDecryptedMessageBytes = new byte[decryptedMessageBytes.length + chunkBytes.length];
            System.arraycopy(decryptedMessageBytes, 0, newDecryptedMessageBytes, 0, decryptedMessageBytes.length);
            System.arraycopy(chunkBytes, 0, newDecryptedMessageBytes, pos, chunkBytes.length);
            decryptedMessageBytes = newDecryptedMessageBytes;
            pos += chunkBytes.length;
        }

        return new String(decryptedMessageBytes, StandardCharsets.UTF_8);
    }

    public byte[] encryptFile(byte[] messageBytes, String publicKeyPair) {
        String message = encryptMessage(bytesToHex(messageBytes), publicKeyPair);
        return message.getBytes(charSet);
    }

    public byte[] decryptFile(byte[] encryptedFileBytes, String privateKeyPair) {
        String message = decryptMessage(new String(encryptedFileBytes, charSet), privateKeyPair);
        return hexToBytes(message);
    }

    public static BigInteger encrypt(BigInteger message, BigInteger e, BigInteger n) {
        return message.modPow(e, n);
    }

    public static BigInteger decrypt(BigInteger encrypted, BigInteger d, BigInteger n) {
        return encrypted.modPow(d, n);
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }

    // Convert hexadecimal string to byte[]
    public static byte[] hexToBytes(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i+1), 16));
        }
        return data;
    }



}