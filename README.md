# Coupa Kafka OAuth2 Library

### Container Environments
For client broker authentication, configure this environment variable:

- COUPA_SAND_CONFIG_PATH: path to properties file which holds config for sand server.

### Kafka Sand Client Configuration (Producer/Consumer)
Add this properties in your kafka configuration

```
coupa.cloud.sand.client.scopes=coupa
coupa.cloud.sand.client.secret=some_secret
coupa.cloud.sand.client.id=some_client
coupa.cloud.sand.client.grant_type=client_credentials
```

### Example (Producer)

```bash
export COUPA_SAND_CONFIG_PATH=/home/deploy/sand.properties
KAFKA_OPTS="-Dlog4j.configuration=file:/srv/kafka/2.12-2.0.0/kafka_2.12-2.0.0/config/log4j.properties  -Djava.security.properties=/etc/kafka/bcfips.java.security -Djavax.net.ssl.trustStore=/var/private/ssl/kafka.truststore.bcfks -Djavax.net.ssl.trustStorePassword=some_password" /srv/kafka/current/kafka_2.12-2.0.0/bin/kafka-console-producer.sh --broker-list 10.1.11.43:9092 --producer.config client.properties --topic test-topic1
```

## Broker X Broker Authentication
```java
class com.coupa.cloud.operations.ce.kafka.security.sasl.oauth.KafkaBrokerSandTokenCreator
```
### How to build/package the library.

```bash
mvn3/maven/mvn package
```

### Kafka Server Configuration

Add this properties in server.properties

- security.inter.broker.protocol=SASL_PLAINTEXT or (SASL_SSL)
- sasl.mechanism.inter.broker.protocol=OAUTHBEARER
- sasl.enabled.mechanisms=OAUTHBEARER
- listener.name.sasl_plaintext.oauthbearer.sasl.login.callback.handler.class=com.coupa.cloud.operations.ce.kafka.security.sasl.oauth.KafkaBrokerSandTokenCreator
- listener.name.sasl_plaintext.oauthbearer.sasl.server.callback.handler.class=com.coupa.cloud.operations.ce.kafka.security.sasl.oauth.KafkaSandTokenValidator
- listeners=SASL_PLAINTEXT://:{PORT} or (SASL_SSL://:{PORT})
- advertised.listeners=SASL_PLAINTEXT://{HOST_IP}:{PORT} or (SASL_SSL://{HOST_IP}:{PORT})

### JAAS Security Configuration

1. Create an file called kafka_server_jaas.conf with this content:

    ```
    KafkaServer {
        org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required ;
    };
    ```
2. Add this file to config path of kafka.

3. Add `-Djava.security.auth.login.config=/opt/kafka/config/kafka_server_jaas.conf` at java args to load the configuration file.
    
