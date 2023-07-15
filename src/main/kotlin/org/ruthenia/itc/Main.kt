package org.ruthenia.itc

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import mu.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.*
import org.ruthenia.itc.schema.Balance
import org.ruthenia.itc.schema.Customer
import org.ruthenia.itc.schema.CustomerBalance
import java.time.Duration
import java.util.*

private val logger = KotlinLogging.logger {}

@Volatile
var keepRunning = true

fun main() {

    // add a shutdown hook (on pressing Ctrl+C keys)
    val mainThread = Thread.currentThread()
    val shutdownListener = Thread {
        keepRunning = false
        try {
            mainThread.join()
        } catch (ignored: InterruptedException) {
        }
    }
    Runtime.getRuntime().addShutdownHook(shutdownListener)

    val bootstrapServers = "localhost:9092"
    val schemaRegistryUrl = "http://localhost:8081"
    val config = Properties()
    config[StreamsConfig.APPLICATION_ID_CONFIG] = "3rivers-demo"
    config[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
    config[AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG] = schemaRegistryUrl
    config[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"

    Serdes.String().use { stringSerde ->
        config[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = stringSerde.javaClass.name
        config[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = SpecificAvroSerde::class.java.name
        val serdeConfigs: Map<String, *> = Collections.singletonMap(
            AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
            schemaRegistryUrl
        )

        val builder = StreamsBuilder()
        val customerSerde: Serde<Customer> = SpecificAvroSerde()
        customerSerde.configure(serdeConfigs, false)
        val customers: KStream<String, Customer> =
            builder.stream("Customer", Consumed.with(stringSerde, customerSerde))
        customers.peek { key, customer -> logger.info("Customer: {} -> {}", key, customer) }

        val balanceSerde: Serde<Balance> = SpecificAvroSerde()
        balanceSerde.configure(serdeConfigs, false)
        val balances: KStream<String, Balance> = builder.stream("Balance", Consumed.with(stringSerde, balanceSerde))
        balances.peek { key, balance -> logger.info("Balance: {} -> {}", key, balance) }

        val customerBalances: KStream<String, CustomerBalance> = customers.join(
            balances,
            { customer: Customer, balance: Balance ->
                if (customer.accountId.equals(balance.accountId)) CustomerBalance.newBuilder()
                    .setCustomerId(customer.customerId)
                    .setAccountId(customer.accountId)
                    .setPhoneNumber(customer.phoneNumber)
                    .setBalance(balance.balance)
                    .build() else null
            },
            JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofMinutes(5)),
            StreamJoined.with(stringSerde, customerSerde, balanceSerde)
        )
        customerBalances.peek { key, value -> logger.info("CustomerBalance: {} -> {}", key, value) }
        val customerBalanceSerde: Serde<CustomerBalance> = SpecificAvroSerde()
        customerBalanceSerde.configure(serdeConfigs, false)
        customerBalances.to("CustomerBalance", Produced.with(stringSerde, customerBalanceSerde))

        val topology: Topology = builder.build()
        KafkaStreams(topology, config).use { streams ->
            streams.start()

            // run forever until Ctrl+C is pressed
            while (keepRunning) {
                Thread.onSpinWait()
            }
        }
    }
}
