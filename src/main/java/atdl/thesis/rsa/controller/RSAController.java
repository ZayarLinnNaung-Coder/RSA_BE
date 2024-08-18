package atdl.thesis.rsa.controller;

import atdl.thesis.rsa.model.RSAKeyPair;
import atdl.thesis.rsa.model.RSAResponse;
import atdl.thesis.rsa.service.RSAService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/rsa")
@AllArgsConstructor
@CrossOrigin
public class RSAController {

    private final RSAService rsaService;

    @GetMapping("/generate-key")
    ResponseEntity<RSAKeyPair> generateKey(){
        return ResponseEntity.ok(rsaService.generateKey());
    }

    @PostMapping("/encrypt/message")
    ResponseEntity<RSAResponse> encrypt(@RequestParam String publicKeyPair,
                                        @RequestParam(value = "message", required = false) String message
    ) {
        return ResponseEntity.ok(new RSAResponse(rsaService.encryptMessage(message, publicKeyPair)));
    }

    @PostMapping("/encrypt/file")
    public ResponseEntity<byte[]> encryptFile(@RequestParam("file") MultipartFile file, @RequestParam String publicKeyPair) throws IOException {

        byte[] fileBytes = file.getBytes();
        byte[] encryptedFileBytes = rsaService.encryptFile(fileBytes, publicKeyPair);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=encryptedFile.enc")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(encryptedFileBytes);
    }

    @PostMapping("/decrypt/message")
    ResponseEntity<RSAResponse> decrypt(@RequestParam String privateKeyPair,
                                   @RequestParam(value = "message", required = false) String message
    ) {
        return ResponseEntity.ok(new RSAResponse(rsaService.decryptMessage(message, privateKeyPair)));
    }

    @PostMapping("/decrypt/file")
    public ResponseEntity<byte[]> decryptFile(@RequestParam("file") MultipartFile file, @RequestParam String privateKeyPair) throws IOException {

        byte[] encryptedFileBytes = file.getBytes();
        byte[] decryptedFileBytes = rsaService.decryptFile(encryptedFileBytes, privateKeyPair);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=decryptedFile")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(decryptedFileBytes);
    }

}
