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
package com.javacreed.examples.gson.part3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class BookTypeAdapter extends TypeAdapter<Book> {

  @Override
  public Book read(final JsonReader in) throws IOException {
    final Book book = new Book();

    in.beginArray();
    book.setIsbn(in.nextString());
    book.setTitle(in.nextString());
    final List<Author> authors = new ArrayList<>();
    while (in.hasNext()) {
      final int id = in.nextInt();
      final String name = in.nextString();
      authors.add(new Author(id, name));
    }
    book.setAuthors(authors.toArray(new Author[authors.size()]));
    in.endArray();

    return book;
  }

  @Override
  public void write(final JsonWriter out, final Book book) throws IOException {
    out.beginArray();
    out.value(book.getIsbn());
    out.value(book.getTitle());
    for (final Author author : book.getAuthors()) {
      out.value(author.getId());
      out.value(author.getName());
    }
    out.endArray();
  }

}
