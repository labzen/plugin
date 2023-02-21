package cn.labzen.plugin.api.bean;

import java.util.List;
import java.util.Map;

public class Source {

  private String name;
  private Integer age;
  private boolean dead = false;
  private String mistressName;
  private Address address;
  private List<String> parentAndChildrenNames;
  private Map<String, ?> information;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getAge() {
    return age;
  }

  public void setAge(Integer age) {
    this.age = age;
  }

  public boolean isDead() {
    return dead;
  }

  public void setDead(boolean dead) {
    this.dead = dead;
  }

  public String getMistressName() {
    return mistressName;
  }

  public void setMistressName(String mistressName) {
    this.mistressName = mistressName;
  }

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public List<String> getParentAndChildrenNames() {
    return parentAndChildrenNames;
  }

  public void setParentAndChildrenNames(List<String> parentAndChildrenNames) {
    this.parentAndChildrenNames = parentAndChildrenNames;
  }

  public Map<String, ?> getInformation() {
    return information;
  }

  public void setInformation(Map<String, ?> information) {
    this.information = information;
  }
}
