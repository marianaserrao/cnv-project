# CNV Project - Group 24: Mariana Serrão Guilherme - 105045, João Miguel Elias Bagorro - 	95604

Repo for the Cloud Computing and Virualization project - The goal of this project is to design and implement a service hosted in an elastic public cloud to give support to an hypothetical study of ecosystems. The service is meant to execute a number of computationally-intensive tasks, namely simulation of ecosystems and compression of nature-related imagery. 

## Types of requests

- Foxes and Rabbits: carries out a simulation of evolution of an ecosystem of foxes and rabbits in a given terrain, over a number of rounds;
- Insect War Simulation: carries out a simulation of a war between several insect species in a terrain, where each species has several insect roles (soldiers, workers, drones and queens);
- Image Compression: returns a compressed version of a nature-related image, using a given algorithm and compression factor

## Architecture

The system runs within the Amazon Web Services ecosystem, organized in four main components (**in development**):

- **Workers** receive web requests to perform an operation. There will be a varying number of identical VMs and Lambdas running simualtion and image compression operations;
- **Load Balancer (LB)** is the entry point of the system. It receives all web requests, and for each one, it selects an active VM to serve the request and forwards it to that server. In alternative, it can also trigger a Lambda function invocation;
- **Auto-Scaler (AS)** is in charge of collecting system performance metrics and, based on them, adjusting the number of active VM instances;
- **Metrics Storage System** uses Amazon DynamoDB to store request performance metrics. These will help the LB choose the most appropriate worker.

## Running the project

```
source <path-to-aws-credentials>/config.sh
```

```
mvn clean package
```

```
java -cp webserver/target/webserver-1.0.0-SNAPSHOT-jar-with-dependencies.jar -javaagent:javassist/target/JavassistWrapper-1.0-jar-with-dependencies.jar=RequestAnalyser:pt.ulisboa.tecnico.cnv:output pt.ulisboa.tecnico.cnv.webserver.WebServer
```