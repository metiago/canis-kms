## CANIS - KMS

**CANIS** is a custom-built Key Management System (KMS) designed to manage public and private keys
for applications through the official Java client. The client and server communicate through an
internal Redis-inspired protocol called **CANISP (Canis Protocol)**. CANISP is not RESP-compatible
and is not the application-facing API.

Applications generating text files containing sensitive information leverage CANIS for encryption
and decryption. Before saving such files to disk, these applications encrypt them using a public key
stored in CANIS. Later, when processing shared encrypted files, authenticated applications ask CANIS
to decrypt data with the stored private key, keeping private keys on the server.
The official client exposes file envelope helpers so applications can encrypt shared files and
decrypt them by asking CANIS to unwrap the file key.

In addition to its key management functionality, CANIS provides a key-value store where the key is
an arbitrary identifier, and the value is an object containing attributes such as name, public key,
and private key. These key-value pairs are persisted in .dat files for secure and reliable storage.
The CANISP protocol supports various data types, including arrays of maps, strings, integers, and
individual maps, enabling flexible and structured data communication between the official client
and server.
The current protocol is documented in [docs/canisp-protocol.md](docs/canisp-protocol.md).
Threat model and deployment assumptions are documented in
[docs/threat-model.md](docs/threat-model.md).
Client package publishing steps are documented in
[docs/client-package-deploy.md](docs/client-package-deploy.md).
Service-name validation follow-up work is tracked in
[docs/service-name-validation-backlog.md](docs/service-name-validation-backlog.md).

#### Key Features:

1. **Socket Communication:**
    - The server listens for incoming connections from client applications over a specified port.
    - Utilizes TCP/IP for reliable communication, ensuring data integrity and order.

2. **Custom Protocol (CANISP):**
    - The CANISP protocol defines a structured internal format for data transmission between the
      CANIS server and official client.
    - Application developers should use the official Java client instead of writing raw CANISP
      socket messages.
    - Supports the following data types:
        - **Arrays of Maps:** Allows clients to send multiple key-value pairs in a structured
          format.
            - Example: `|a>|ms>name:john|mi>age:25|ms>city:poa|ms>state:rs`
        - **Strings:** Simple text messages can be sent.
            - Example: `|s>Hello World`
        - **Integers:** Numeric values can be transmitted.
            - Example: `|i>100`
        - **Maps:** Key-value pairs can be sent as a single unit.
            - Example: `|ms>name:john|mi>age:25|ms>city:poa|ms>state:rs`

3. **Key-Value Store:**
    - The server maintains an in-memory key-value store to temporarily hold registered application
      data.
    - Each application can register its name and associated data, which is stored in a structured
      format.

4. **Persistence:**
    - Data from the key-value store is periodically persisted to a `.dat` file, ensuring that
      registered application information is not lost between server restarts.
    - The persistence mechanism allows for easy recovery and access to previously registered
      applications.

5. **Command Handling:**
    - The server processes incoming CANISP commands from authenticated clients, allowing for
      operations such as:
        - Registering a new application with its associated data.
        - Retrieving application data based on the registered name.
        - Updating or deleting application data as needed.

**Technologies Used:**

- Programming Language: Java
- Networking: TCP/IP Sockets
- Data Storage: File I/O for `.dat` encrypted file persistence

### Prerequisites
- Java 17+
- Maven

### Testing & Running

Before running the server or tests, export these environment variables

```bash
# server port
export CANIS_PORT=3307;
# client server host
export CANIS_SERVER_HOST=localhost;
# client server port
export CANIS_SERVER_PORT=3307;
# service username
export CANIS_USERNAME=admin
# service password
export CANIS_PASSWORD=123;
# optional comma-separated credentials for additional authenticated clients
export CANIS_SERVICE_CREDENTIALS=serviceA:secret-a,serviceB:secret-b;
# secret file name
export CANIS_SECRET_KEY=/my/secret/location/secret.txt;

# run tests
mvn test

# build executable jar
mvn package

# start server
java server/target/canis-jar-with-dependencies.jar
```
