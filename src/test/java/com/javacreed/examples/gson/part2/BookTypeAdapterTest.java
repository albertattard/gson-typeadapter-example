/*
 * #%L
 * Gson TypeAdapter Example
 * %%
 * Copyright (C) 2012 - 2015 Java Creed
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.javacreed.examples.gson.part2;

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
    book.setAuthors(new String[] { "Joshua Bloch" });
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
    book.setAuthors(new String[] { "Joshua Bloch", "Neal Gafter" });
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
