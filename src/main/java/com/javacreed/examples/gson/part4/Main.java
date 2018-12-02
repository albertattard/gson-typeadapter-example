package com.javacreed.examples.gson.part4;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Main {
  public static void main(final String[] args) throws IOException {
    // Configure GSON
    final GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(Book.class, new BookTypeAdapter());
    gsonBuilder.setPrettyPrinting();

    final Gson gson = gsonBuilder.create();

    final Book book = new Book();
    book.setAuthors(new Author[] { new Author(1, "Joshua Bloch"), new Author(2, "Neal Gafter") });
    book.setTitle("Java Puzzlers: Traps, Pitfalls, and Corner Cases");
    book.setIsbn("978-0321336781");

    final String json = gson.toJson(book);
    System.out.println("Serialise");
    System.out.println(json);

    final Book parsedBook = gson.fromJson(json, Book.class);
    System.out.println("\nDeserialised");
    System.out.println(parsedBook);
  }
}
