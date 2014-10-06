package com.outbrain.ob1k.server.entities;

import java.util.List;
import java.util.Set;

/**
 * Created by aronen on 10/6/14.
 */
public class TestEntity {
  private Set<Long> ids;
  private String name;
  private TestEnum[] options;
  private List<OtherEntity> others;

  public TestEntity() {}

  public TestEntity(Set<Long> ids, String name, TestEnum[] options, List<OtherEntity> others) {
    this.ids = ids;
    this.name = name;
    this.options = options;
    this.others = others;
  }

  public Set<Long> getIds() {
    return ids;
  }

  public void setIds(Set<Long> ids) {
    this.ids = ids;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public TestEnum[] getOptions() {
    return options;
  }

  public void setOptions(TestEnum[] options) {
    this.options = options;
  }

  public List<OtherEntity> getOthers() {
    return others;
  }

  public void setOthers(List<OtherEntity> others) {
    this.others = others;
  }
}
