package com.javacreed.examples.gson.part3;

public class Author {

  private int id;
  private String name;

  public Author(final int id, final String name) {
    this.id = id;
    this.name = name;
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof Author) {
      final Author other = (Author) object;
      return id == other.id && name.equals(other.name);
    }

    return false;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + id;
    result = prime * result + (name == null ? 0 : name.hashCode());
    return result;
  }

  public void setId(final int id) {
    this.id = id;
  }

  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return String.format("[%d] %s", id, name);
  }
}
