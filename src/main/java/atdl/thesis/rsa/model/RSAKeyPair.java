package atdl.thesis.rsa.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RSAKeyPair {
    private String publicKey;
    private String privateKey;
}
