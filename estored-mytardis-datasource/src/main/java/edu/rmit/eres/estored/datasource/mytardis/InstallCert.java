/*
 * The original source code on which this is based on, is subject to the following copyright:
 * 
 * Copyright 2006 Sun Microsystems, Inc.  All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.rmit.eres.estored.datasource.mytardis;

import javax.net.ssl.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Class used to add the server's certificate to the KeyStore with your trusted certificates.
 * Based on : http://nodsw.com/blog/leeland/2006/12/06-no-more-unable-find-valid-certification-path-requested-target
 */
public class InstallCert {

	private static final Logger logger = 
			LoggerFactory.getLogger(InstallCert.class);
	
	private static final String DEFAULT_PASSPHRASE = "changeit";
	private static final Integer DEFAULT_PORT = 443;
	private static final String CERT_PATH = "/lib/security/cacerts";
	
	public static void installCert(String host) throws Exception {
		installCert(host, DEFAULT_PORT, DEFAULT_PASSPHRASE);
	}
	
	public static void installCert(String host, Integer port) throws Exception {
		installCert(host, port, DEFAULT_PASSPHRASE);
	}
	
	public static void installCert(String host, String passphrase) throws Exception {
		installCert(host, DEFAULT_PORT, passphrase);
	}
	
	public static void installCert(String host, Integer port, String passphraseStr) throws Exception {
		char[] passphrase = passphraseStr.toCharArray();
		String certName = System.getProperty("java.home") + CERT_PATH;
		
		File file = new File(certName);
        if (file.isFile() == false) {
            char SEP = File.separatorChar;
            File dir = new File(System.getProperty("java.home") + SEP + "lib" + SEP + "security");
            file = new File(dir, certName);
            if (file.isFile() == false) {
                file = new File(dir, "cacerts");
            }
        }
        logger.debug("Loading KeyStore " + file + "...");
        InputStream in = new FileInputStream(file);
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(in, passphrase);
        in.close();
        
        SSLContext context = SSLContext.getInstance("TLS");
        TrustManagerFactory tmf =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);
        X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
        SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
        context.init(null, new TrustManager[]{tm}, null);
        SSLSocketFactory factory = context.getSocketFactory();

        // Try opening connection and SSL Handshake the host
        logger.info("Opening connection to " + host + ":" + port + "...");
        SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
        socket.setSoTimeout(10000);
        try {
        	logger.info("Starting SSL handshake...");
            socket.startHandshake();
            socket.close();
            
            // If there was no error, return
            logger.info("No errors, certificate is already trusted");
            return;
        } catch (SSLException e) {
        	logger.debug(e.getMessage());
        	logger.info("Could not perform SSL Handshake. Importing certificate...");
        }

        // If there was an error, import the SSL certificate to the trust store
        X509Certificate[] chain = tm.chain;
        if (chain == null) {
        	logger.error("Could not obtain server certificate chain");
            return;
        }

        logger.debug("Server sent " + chain.length + " certificate(s):");
        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        for (int i = 0; i < chain.length; i++) {
            X509Certificate cert = chain[i];
            logger.debug(" " + (i + 1) + " Subject " + cert.getSubjectDN());
            logger.debug("   Issuer  " + cert.getIssuerDN());
            sha1.update(cert.getEncoded());
            logger.debug("   sha1    " + toHexString(sha1.digest()));
            md5.update(cert.getEncoded());
            logger.debug("   md5     " + toHexString(md5.digest()));
            
            String alias = host + "-" + (i + 1);
            ks.setCertificateEntry(alias, cert);

            OutputStream out = new FileOutputStream(certName);
            ks.store(out, passphrase);
            out.close();

            logger.debug(cert.toString());
            logger.info("Added certificate to keystore '" + certName + "' using alias '" + alias + "'");
        }
	}    
    
    private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();

    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (int b : bytes) {
            b &= 0xff;
            sb.append(HEXDIGITS[b >> 4]);
            sb.append(HEXDIGITS[b & 15]);
            sb.append(' ');
        }
        return sb.toString();
    }

    private static class SavingTrustManager implements X509TrustManager {
    	 
        private final X509TrustManager tm;
        private X509Certificate[] chain;
 
        SavingTrustManager(final X509TrustManager tm) {
            this.tm = tm;
        }
 
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
            // throw new UnsupportedOperationException();
        }
 
        @Override
        public void checkClientTrusted(final X509Certificate[] chain,
                final String authType)
                throws CertificateException {
            throw new UnsupportedOperationException();
        }
 
        @Override
        public void checkServerTrusted(final X509Certificate[] chain,
                final String authType)
                throws CertificateException {
            this.chain = chain;
            this.tm.checkServerTrusted(chain, authType);
        }
    }
}