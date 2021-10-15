/*
 * Copyright (c) 2019, 2021 Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.microprofile.rest.client.tck.ssl;

import static java.lang.System.getProperty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.function.Consumer;

import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 *
 * A superclass for SSL-related tests
 *
 * Certificates were generated with Tomas Terem's script:
 * https://gist.github.com/tterem/8c4891641eddd6f070c6cdc738738c34
 *
 */
public abstract class AbstractSslTest extends Arquillian {

    public static final String CERT_LOCATION_FILE = "certificates-dir.txt";

    static final String HTTPS_HOST = System.getProperty("org.eclipse.microprofile.rest.client.ssl.host", "localhost");
    static final int HTTPS_PORT = Integer.valueOf(getProperty("org.eclipse.microprofile.rest.client.ssl.port", "8948"));
    static final String BASE_URI_STRING = "https://" + HTTPS_HOST + ":" + HTTPS_PORT;
    static final URI BASE_URI = URI.create(BASE_URI_STRING);

    protected static File serverKeystore;
    protected static File serverTruststore;

    protected static File clientKeystore;
    protected static File clientTruststore;
    protected static String clientTruststoreFromClasspath = "client.truststore";
    protected static String clientKeystoreFromClasspath = "client.keystore";

    protected static File serverWrongHostnameKeystore;
    protected static File clientWrongHostnameTruststore;
    protected static String clientWrongHostnameTruststoreFromClasspath = "client-wrong-hostname.truststore";

    protected static File anotherTruststore;

    static final String PASSWORD = "password";

    public static KeyStore getKeyStore(File keystoreFile) throws Exception {
        KeyStore keystore = KeyStore.getInstance("pkcs12");
        try (FileInputStream input = new FileInputStream(keystoreFile)) {
            keystore.load(input, PASSWORD.toCharArray());
        }
        return keystore;
    }

    private static HttpsServer httpsServer;

    /**
     * Initializes the https server and prepares a directory with certificates for testing usage of certificates stored
     * on disk. Additionally, to pass the information about the directory with the certificates to the container,
     * creates a <code>META-INF/certificates-dir.txt</code> file in the web archive with the location
     *
     * @param webArchive
     *            performs a test-specific configuration of the https server
     * @param serverInitializer
     *            performs a test-specific configuration of the https server
     * @return the disk directory containing certificates
     */
    static void initializeTest(WebArchive webArchive, Consumer<HttpsServer> serverInitializer) {
        Path certificatesDirectory = prepareCertificates();
        initializeCertPaths(certificatesDirectory.toAbsolutePath().toString());

        startServer(serverInitializer);

        webArchive.addAsResource(new StringAsset(certificatesDirectory.toAbsolutePath().toString()),
                "META-INF/" + CERT_LOCATION_FILE);
    }

    static void initializeCertificateLocations() {
        InputStream certLocationStream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream("/META-INF/" + CERT_LOCATION_FILE);
        if (certLocationStream != null) {
            try (InputStreamReader streamReader = new InputStreamReader(certLocationStream);
                    BufferedReader reader = new BufferedReader(streamReader)) {
                String certsDir = reader.readLine();

                initializeCertPaths(certsDir);
            } catch (IOException e) {
                throw new RuntimeException("failed to read certification file", e);
            }
        }
    }

    private static void initializeCertPaths(String certsDir) {
        serverKeystore = Paths.get(certsDir, "server.keystore").toFile();
        serverTruststore = Paths.get(certsDir, "server.truststore").toFile();

        clientKeystore = Paths.get(certsDir, "client.keystore").toFile();
        clientTruststore = Paths.get(certsDir, "client.truststore").toFile();

        anotherTruststore = Paths.get(certsDir, "client-different-cert.truststore").toFile();
        serverWrongHostnameKeystore = Paths.get(certsDir, "server-wrong-hostname.keystore").toFile();
        clientWrongHostnameTruststore = Paths.get(certsDir, "client-wrong-hostname.truststore").toFile();
    }

    private static Path prepareCertificates() {
        try {
            Path result = Files.createTempDirectory("ssl-test");
            result.toFile().deleteOnExit();

            copyResourceTo("client.keystore", result);
            copyResourceTo("client.truststore", result);
            copyResourceTo("client-different-cert.truststore", result);
            copyResourceTo("client-wrong-hostname.truststore", result);
            copyResourceTo("server.keystore", result);
            copyResourceTo("server.truststore", result);
            copyResourceTo("server-wrong-hostname.keystore", result);

            return result;
        } catch (IOException e) {
            throw new RuntimeException("Unable to prepare certificates for tests that use certificates from disk");
        }
    }

    private static void startServer(Consumer<HttpsServer> serverInitializer) {
        httpsServer = new HttpsServer();
        serverInitializer.accept(httpsServer);

        httpsServer.start(HTTPS_PORT, HTTPS_HOST);
    }

    private static void copyResourceTo(String resource, Path directory) {
        String resourceLocation = "/ssl/" + resource;
        Path diskLocation = directory.resolve(resource);

        try (InputStream input = AbstractSslTest.class.getResourceAsStream(resourceLocation)) {
            Files.copy(input, diskLocation);
            diskLocation.toFile().deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy " + resource + " to " + directory.toAbsolutePath(), e);
        }
    }

    static String filePath(File file) {
        return file.toURI().toString();
    }

    @BeforeClass
    public static void initHttpsServer() {
        initializeCertificateLocations();
    }

    @AfterClass
    public static void stopHttpsServer() {
        if (httpsServer != null) {
            httpsServer.stop();
        }
    }
}
