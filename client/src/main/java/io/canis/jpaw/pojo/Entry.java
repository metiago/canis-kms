package io.canis.jpaw.pojo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Entry implements Serializable {

  private String name;

  private String publicKey;

  private String privateKey;

  public Entry() {
  }

  public Entry(String name, String publicKey, String privateKey) {
    this.name = name;
    this.publicKey = publicKey;
    this.privateKey = privateKey;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPublicKey() {
    return publicKey;
  }

  public void setPublicKey(String publicKey) {
    this.publicKey = publicKey;
  }

  public String getPrivateKey() {
    return privateKey;
  }

  public void setPrivateKey(String privateKey) {
    this.privateKey = privateKey;
  }

  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("name", this.name);
    map.put("publicKey", this.publicKey);
    map.put("privateKey", this.privateKey);
    return map;
  }

  @Override
  public String toString() {
    return "Entry{" +
        "name='" + name + '\'' +
        '}';
  }
}
