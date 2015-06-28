package eu.hinsch.spring.propertiesdecrypter;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.salt.ZeroSaltGenerator;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.*;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

/**
 * Created by lh on 02/04/15.
 */
public class DecryptingPropertiesApplicationListener
        implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {
    static final String PASSWORD_PROPERTY = "propertyDecryption.password";
    public static final String DEFAULT_ALGORITHM = "PBEWithMD5AndDES";
    public static final String PREFIX_KEY = "propertyDecryption.prefix";

    private String prefix;
    private StandardPBEStringEncryptor encrypter = new StandardPBEStringEncryptor();

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        initializeEncrypter(environment);
        prefix = environment.getProperty(PREFIX_KEY, "{encrypted}");

        final MutablePropertySources propertySources = environment.getPropertySources();

        List<String> encryptedKeys = getKeysOfEncryptedPropertyValues(environment, propertySources);
        addDecryptedValues(environment, propertySources, encryptedKeys);
    }

    private void initializeEncrypter(Environment environment) {
        encrypter.setAlgorithm(environment.getProperty("propertyDecryption.algorithm", DEFAULT_ALGORITHM));
        encrypter.setSaltGenerator(new ZeroSaltGenerator());
        encrypter.setPassword(environment.getRequiredProperty(PASSWORD_PROPERTY));
    }

    private List<String> getKeysOfEncryptedPropertyValues(Environment environment, MutablePropertySources propertySources) {
        return getPropertySourceStream(propertySources)
                .filter(source -> source instanceof EnumerablePropertySource)
                .map(source -> (EnumerablePropertySource)source)
                .flatMap(source -> asList(source.getPropertyNames()).stream())
                .filter(this::isNotEncryptionConfigProperty)
                .filter(key -> isEncrypted(environment.getProperty(key)))
                .collect(toList());
    }

    private boolean isNotEncryptionConfigProperty(String key) {
        return !PREFIX_KEY.equals(key);
    }

    private Stream<PropertySource<?>> getPropertySourceStream(final MutablePropertySources propertySources) {
        final Iterator<PropertySource<?>> iterator = propertySources.iterator();
        Iterable<PropertySource<?>> iterable = () -> iterator;
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    private void addDecryptedValues(Environment environment, MutablePropertySources propertySources, List<String> encryptedKeys) {
        // TODO collect to properties?
        final Properties decryptedValues = new Properties();
        encryptedKeys.stream()
                .forEach(key -> decryptAndAdd(environment, key, decryptedValues));
        propertySources.addFirst(new PropertiesPropertySource("decryptedValues", decryptedValues));
    }

    private void decryptAndAdd(Environment environment, String key, Properties decryptedValues) {
        final String property = environment.getProperty(key);
        final String propertyValue = decryptPropertyValue(property);
        decryptedValues.put(key, propertyValue);
    }

    private String decryptPropertyValue(String encryptedPropertyValue) {
        String cypher = getCypher(encryptedPropertyValue);
        try {
            return encrypter.decrypt(cypher);
        }
        catch (EncryptionOperationNotPossibleException e) {
            throw new RuntimeException("Unable to decrypt property value '" + encryptedPropertyValue + "'", e);
        }
    }

    private boolean isEncrypted(Object propertyValue) {
        return propertyValue != null && propertyValue instanceof String && ((String)propertyValue).startsWith(prefix);
    }

    private String getCypher(String encryptedPropertyValue) {
        return encryptedPropertyValue.substring(prefix.length());
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
