/*
 * The MIT License
 *
 * Copyright 2013-2014 Jakub Jirutka <jakub@jirutka.cz>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package cz.jirutka.validator.collection.internal;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.HibernateValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static java.lang.Integer.parseInt;

public abstract class HibernateValidatorInfo {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateValidatorInfo.class);
    public static final String HIBERNATE_VERSION_FILE =
        "validator-collection-hibernate-version.txt";

    /**
     * Returns version of the Hibernate Validator determined from the package
     * info on classpath.
     *
     * <p>Version number is parsed as: 5.1.1.Final -> 511, 5.2.0-SNAPSHOT -> 520.</p>
     *
     * @return A parsed version number, or {@link Integer#MAX_VALUE} if could not be determined.
     */
    public static int getVersion() {
        return getVersion(HIBERNATE_VERSION_FILE);
    }

    public static int getVersion(String versionFile) {
        // try to get the version from HIBERNATE_VERSION_FILE if it exists
        String maybeFileVersion = null;
        try {
            maybeFileVersion = getVersionFromFile(versionFile);
        } catch (IOException e) {
            LOG.error("Failed to determine hibernate version from " + HIBERNATE_VERSION_FILE +
                "; falling back to trying to determine hibernate version from package metadata.");
        }

        if (maybeFileVersion != null) {
            return parseVersion(maybeFileVersion);
        }

        // otherwise, rely on the package metadata
        Package pkg = HibernateValidator.class.getPackage();

        if (pkg != null) {
            String version = pkg.getImplementationVersion();

            if (version != null && !version.isEmpty()) {
                LOG.info("Found Hibernate Validator {}", version);
                return parseVersion(version);
            }
        }
        LOG.warn("Could not determine Hibernate Validator version");
        return Integer.MAX_VALUE;
    }

    static String getVersionFromFile(String resourceName) throws IOException {
        InputStream resourceAsStream =
            HibernateValidatorInfo.class.getClassLoader().getResourceAsStream(resourceName);

        if (resourceAsStream == null) {
            return null;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(resourceAsStream));

            return StringUtils.strip(reader.readLine());
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    static int parseVersion(String version) {
        String[] tokens = version.split("[.-]");

        return parseInt(tokens[0]) * 100
                + parseInt(tokens[1]) * 10
                + parseInt(tokens[2]);
    }
}
