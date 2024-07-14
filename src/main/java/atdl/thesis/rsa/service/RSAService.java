package atdl.thesis.rsa.service;

import atdl.thesis.rsa.model.RSAKeyPair;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Service
public class RSAService {

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

        return RSAKeyPair.builder()
                .publicKey(e + "." + n)
                .privateKey(d + "." + n)
                .build();
    }

    public String encryptMessage(String message, String publicKeyPair) {

        BigInteger publicKey = new BigInteger(publicKeyPair.split("\\.")[0]);
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
        BigInteger publicKey = new BigInteger(publicKeyPair.split("\\.")[0]);
        BigInteger modulus = new BigInteger(publicKeyPair.split("\\.")[1]);

        int chunkSize = modulus.bitLength() / 8 - 11; // RSA encryption padding
        int numChunks = (int) Math.ceil((double) messageBytes.length / chunkSize);

        List<byte[]> encryptedChunks = new ArrayList<>();

        for (int i = 0; i < numChunks; i++) {
            int start = i * chunkSize;
            int length = Math.min(chunkSize, messageBytes.length - start);

            byte[] chunk = new byte[length];
            System.arraycopy(messageBytes, start, chunk, 0, length);

            BigInteger chunkBigInt = new BigInteger(1, chunk);
            BigInteger encryptedChunk = encrypt(chunkBigInt, publicKey, modulus);

            encryptedChunks.add(encryptedChunk.toByteArray());
        }

        // Convert encrypted chunks to a single byte array with Base64 encoding
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        Base64.Encoder encoder = Base64.getEncoder();

        for (byte[] chunk : encryptedChunks) {
            byte[] encodedChunk = encoder.encode(chunk);
            try {
                byteStream.write(encodedChunk);
                byteStream.write(',');
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return byteStream.toByteArray();
    }


    public byte[] decryptFile(byte[] encryptedFileBytes, String privateKeyPair) {
        BigInteger privateKey = new BigInteger(privateKeyPair.split("\\.")[0]);
        BigInteger modulus = new BigInteger(privateKeyPair.split("\\.")[1]);

        String encryptedFileString = new String(encryptedFileBytes, StandardCharsets.UTF_8);
        String[] encryptedChunks = encryptedFileString.split(",");

        List<byte[]> decryptedChunks = new ArrayList<>();

        for (String chunk : encryptedChunks) {
            if (!chunk.isEmpty()) { // Ensure chunk is not empty
                byte[] chunkBytes = Base64.getDecoder().decode(chunk);
                BigInteger encryptedChunk = new BigInteger(1, chunkBytes);
                BigInteger decryptedChunk = decrypt(encryptedChunk, privateKey, modulus);

                byte[] decryptedChunkBytes = decryptedChunk.toByteArray();
                // Remove leading zero byte if exists (BigInteger adds it to ensure positive number)
                if (decryptedChunkBytes[0] == 0) {
                    decryptedChunkBytes = java.util.Arrays.copyOfRange(decryptedChunkBytes, 1, decryptedChunkBytes.length);
                }

                decryptedChunks.add(decryptedChunkBytes);
            }
        }

        // Combine decrypted chunks into a single byte array
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        for (byte[] chunk : decryptedChunks) {
            try {
                byteStream.write(chunk);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return byteStream.toByteArray();
    }


//    public byte[] encryptFile(byte[] messageBytes, String publicKeyPair) {
//        return encryptMessage(new String(messageBytes, StandardCharsets.UTF_16LE), publicKeyPair).getBytes(StandardCharsets.UTF_16LE);
//    }
//
//
//    public byte[] decryptFile(byte[] encryptedFileBytes, String privateKeyPair) {
//        return encryptMessage(new String(encryptedFileBytes, StandardCharsets.UTF_16LE), privateKeyPair).getBytes(StandardCharsets.UTF_16LE);
//    }


    public static BigInteger encrypt(BigInteger message, BigInteger e, BigInteger n) {
        return message.modPow(e, n);
    }

    public static BigInteger decrypt(BigInteger encrypted, BigInteger d, BigInteger n) {
        return encrypted.modPow(d, n);
    }

}
