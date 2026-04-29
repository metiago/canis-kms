package io.canis.jpaw.client;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * The {@code Canis} interface defines a contract for managing health status,
 * setting, retrieving, deleting, and listing key-value pairs in a kms system.
 *
 */
public interface Jpaw {

  /**
   * Retrieves the health status of the server.
   *
   * @return a {@code String} representing the health status.
   * @throws IOException if an I/O error occurs while retrieving the health status.
   */
  String health() throws IOException;

  /**
   * Sets a value associated with the specified key.
   *
   * @param key the key to associate with the value.
   * @return a {@code String} indicating the result of the operation.
   * @throws IOException if an I/O error occurs while setting the value.
   */
  String set(String key) throws IOException;

  /**
   * Retrieves the value associated with the specified key.
   *
   * @param key the key whose associated value is to be retrieved.
   * @return a {@code Map<String, Object>} containing the value associated with the key,
   *         or {@code null} if the key does not exist.
   * @throws IOException if an I/O error occurs while retrieving the value.
   */
  Map<String, Object> get(String key) throws IOException;

  /**
   * Lists all registered entries returned by the server.
   *
   * @return all visible entries.
   * @throws IOException if an I/O error occurs while listing entries.
   */
  List<Map<String, Object>> list() throws IOException;

  /**
   * Decrypts encrypted data using the private key stored for the specified key.
   * The private key remains on the server.
   *
   * @param key the registered key whose private key should be used.
   * @param encryptedData encrypted bytes to decrypt.
   * @return decrypted bytes returned by the server.
   * @throws IOException if an I/O error occurs while decrypting the data.
   */
  byte[] decrypt(String key, byte[] encryptedData) throws IOException;

  /**
   * Deletes the value associated with the specified key.
   *
   * @param key the key to be deleted.
   * @return {@code true} if the key was successfully deleted, {@code false} otherwise.
   * @throws IOException if an I/O error occurs while deleting the key.
   */
  boolean delete(String key) throws IOException;
}


