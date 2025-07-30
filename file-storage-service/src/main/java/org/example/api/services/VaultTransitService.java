package org.example.api.services;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.Ciphertext;
import org.springframework.vault.support.Plaintext;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class VaultTransitService {
    VaultOperations vaultOperations;

    public String encrypt(String plaintext) {
        Plaintext payload = Plaintext.of(plaintext);
        Ciphertext ciphertext = vaultOperations.opsForTransit().encrypt("master-key", payload);
        return ciphertext.getCiphertext();
    }

    public String decrypt(String ciphertext) {
        Ciphertext payload = Ciphertext.of(ciphertext);
        Plaintext plaintext = vaultOperations.opsForTransit().decrypt("master-key", payload);
        return plaintext.asString();
    }
}
