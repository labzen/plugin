package cn.labzen.plugin.api.bean;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Target {

  private String name;
  private Date dob;
  private Boolean dead;
  private Address address;
  private List<String> parentAndChildrenNames;
  private Map<String, ?> information;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Date getDob() {
    return dob;
  }

  public void setDob(Date dob) {
    this.dob = dob;
  }

  public Boolean getDead() {
    return dead;
  }

  public void setDead(Boolean dead) {
    this.dead = dead;
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
