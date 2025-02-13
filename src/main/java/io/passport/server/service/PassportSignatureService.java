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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.List;

/**
 * Service class for digital signature generation.
 */
@Service
public class PassportSignatureService {

    @Value("${dss.keystore.path}")
    private String KEYSTORE_PATH;

    @Value("${dss.keystore.password}")
    private String KEYSTORE_PASSWORD;

    /**
     * Digital signature generation and signing logic with europa esig package.
     *
     * @param documentContent Unsigned PDF file in Byte Array form.
     * @return Signed PDF file in Byte Array form.
     */
    public byte[] generateSignature(byte[] documentContent) {
        File keystoreFile = new File(KEYSTORE_PATH);
        try (
                FileInputStream keystoreStream = new FileInputStream(keystoreFile);
                SignatureTokenConnection signingToken = new Pkcs12SignatureToken(
                        keystoreStream,
                        new KeyStore.PasswordProtection(KEYSTORE_PASSWORD.toCharArray())
                )
        ) {
            if (!keystoreFile.exists()) {
                throw new IllegalArgumentException("Keystore file not found in the classpath at path: " + KEYSTORE_PATH);
            }

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

            DocumentSignatureService<PAdESSignatureParameters, PAdESTimestampParameters> signatureService =
                    new PAdESService(new CommonCertificateVerifier());
            ToBeSigned dataToSign = signatureService.getDataToSign(document, signatureParameters);

            SignatureValue signatureValue = signingToken.sign(dataToSign, signatureParameters.getDigestAlgorithm(), privateKey);

            DSSDocument signedDocument = signatureService.signDocument(document, signatureParameters, signatureValue);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            signedDocument.writeTo(outputStream);
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF could not be signed: " + e.getMessage(), e);
        }
    }
}