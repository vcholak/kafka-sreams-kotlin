# Kafka Streaming demo app

It demonstrates joining two Kafka streams into a resulting stream.

## Install Confluence Platform

Just once, install a local Confluence Platform stack using Docker:

```bash
docker compose up -d
```
It will first install a local Confluence Platform stack using _./docker-compose.yml_ file.
Then it starts the local platform.

Further on: in Docker Desktop, just start the **confluence** container stack.

## Create Topics

Visit [Topics](http://localhost:9021/clusters/MkU3OEVBNTcwNTJENDM2Qg/management/topics) in the local Control Center and add the following topics:

* Customer
* Balance
* CustomerBalance

## Register Avro Schemas

For each topic in [Topics](http://localhost:9021/clusters/MkU3OEVBNTcwNTJENDM2Qg/management/topics), register a corresponding Avro schema, located in the _src/avro_ folder.

## Building

Run the following command:

```bash
./gradlew build
```

It also generates Java classes for Avro schemas.


## Running the demo

If the local Docker container stack is not running then start it using the **confluence** link in Docker Desktop.

Start the demo app:

```bash
./gradlew run
```

Then send new messages to the Customer and the Balance topics:

```bash
./scripts/customer_data_gen.sh
./scripts/balance_data_gen.sh
```