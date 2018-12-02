package com.javacreed.examples.gson.part3;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BookTypeAdapterTest {

  @Test
  public void testWithOneAuthor() {
    final GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(Book.class, new BookTypeAdapter());
    final Gson gson = gsonBuilder.create();

    final Book book = new Book();
    book.setAuthors(new Author[] { new Author(1, "Joshua Bloch") });
    book.setTitle("Effective Java (2nd Edition)");
    book.setIsbn("978-0321356680");

    final String json = gson.toJson(book);
    Assert.assertNotNull(json);

    final Book parsedBook = gson.fromJson(json, Book.class);
    Assert.assertNotNull(parsedBook);
    Assert.assertEquals(book.getIsbn(), parsedBook.getIsbn());
    Assert.assertEquals(book.getTitle(), parsedBook.getTitle());
    Assert.assertArrayEquals(book.getAuthors(), parsedBook.getAuthors());
  }

  @Test
  public void testWithTwoAuthors() {
    final GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(Book.class, new BookTypeAdapter());
    final Gson gson = gsonBuilder.create();

    final Book book = new Book();
    book.setAuthors(new Author[] { new Author(1, "Joshua Bloch"), new Author(2, "Neal Gafter") });
    book.setTitle("Java Puzzlers: Traps, Pitfalls, and Corner Cases");
    book.setIsbn("978-0321336781");

    final String json = gson.toJson(book);
    Assert.assertNotNull(json);

    final Book parsedBook = gson.fromJson(json, Book.class);
    Assert.assertNotNull(parsedBook);
    Assert.assertEquals(book.getIsbn(), parsedBook.getIsbn());
    Assert.assertEquals(book.getTitle(), parsedBook.getTitle());
    Assert.assertArrayEquals(book.getAuthors(), parsedBook.getAuthors());
  }
}
