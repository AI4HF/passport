package io.passport.server.service;


import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.pades.PAdESTimestampParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import eu.europa.esig.dss.token.SignatureTokenConnection;
import eu.europa.esig.dss.signature.DocumentSignatureService;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.List;

@Service
public class PassportSignatureService {

    private static final String KEYSTORE_PATH = "docs/keystore.p12";
    private static final String KEYSTORE_PASSWORD = "password";

    public byte[] generateSignature(byte[] documentContent) {
        try (SignatureTokenConnection signingToken = new Pkcs12SignatureToken(
                new FileInputStream(KEYSTORE_PATH), new KeyStore.PasswordProtection(KEYSTORE_PASSWORD.toCharArray()))) {

            List<DSSPrivateKeyEntry> keys = signingToken.getKeys();
            if (keys.isEmpty()) {
                throw new IllegalArgumentException("No private key found in the keystore");
            }

            DSSPrivateKeyEntry privateKey = keys.get(0);

            DSSDocument document = new InMemoryDocument(documentContent);

            PAdESSignatureParameters signatureParameters = new PAdESSignatureParameters();
            signatureParameters.setSigningCertificate(privateKey.getCertificate());
            signatureParameters.setCertificateChain(privateKey.getCertificateChain());
            signatureParameters.setDigestAlgorithm(DigestAlgorithm.SHA256);

            signatureParameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);

            DocumentSignatureService<PAdESSignatureParameters, PAdESTimestampParameters> signatureService = new PAdESService(new CommonCertificateVerifier());
            ToBeSigned dataToSign = signatureService.getDataToSign(document, signatureParameters);

            SignatureValue signatureValue = signingToken.sign(dataToSign, signatureParameters.getDigestAlgorithm(), privateKey);

            DSSDocument signedDocument = signatureService.signDocument(document, signatureParameters, signatureValue);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            signedDocument.writeTo(outputStream);
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
