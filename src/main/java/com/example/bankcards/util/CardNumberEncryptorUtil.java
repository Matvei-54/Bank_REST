package com.example.bankcards.util;

import com.example.bankcards.exception.card.encryptor.DecryptException;
import com.example.bankcards.exception.card.encryptor.EncryptorException;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

@Getter
@Component
public class CardNumberEncryptorUtil {

    private final SecretKeySpec secretKey;


    public CardNumberEncryptorUtil(@Value("${card.encryption.key}") String password,
                                   @Value("${card.encryption.salt}") String salt) {

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 128);
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            this.secretKey = new SecretKeySpec(keyBytes, "AES");
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new EncryptorException(e.getMessage());
        }
    }

    public String encryptCardNumber(String cardNumber) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(cardNumber.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchAlgorithmException |
                 NoSuchPaddingException e) {
            throw new EncryptorException(e.getMessage());
        }
    }

    public String decryptCardNumber(String dbCardNumber) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decoded = Base64.getDecoder().decode(dbCardNumber);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchAlgorithmException |
                 NoSuchPaddingException e) {
            throw new DecryptException(e.getMessage());
        }
    }
}
