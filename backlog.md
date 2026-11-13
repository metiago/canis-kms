# Backlog

## High

## Medium

### Make missing-key behavior consistent for `get`

`get` currently returns an empty record when the key is missing. The server creates `new Entry()`,
serialization drops null fields, and the client ends up with an empty `Map`. That is inconsistent
with the client interface documentation, which says `get` should return `null` when the key does
not exist, and it is also inconsistent with `get-public` and `decrypt`, which return explicit
`Key not found` errors.

Relevant code:
- `server/src/main/java/io/canis/handlers/ClientHandler.java`
- `server/src/main/java/io/canis/store/Entry.java`
- `server/src/main/java/io/canis/protocol/Converter.java`
- `client/src/main/java/io/canis/jpaw/utils/Parser.java`
- `client/src/main/java/io/canis/jpaw/client/JpawClient.java`
- `client/src/main/java/io/canis/jpaw/client/Jpaw.java`

Follow-up:
- Decide whether missing `get` should return `null` or an explicit protocol error.
- Align server behavior, client parsing, and client interface docs.
