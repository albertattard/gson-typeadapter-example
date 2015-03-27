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
package com.javacreed.examples.gson.part1;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class BookTypeAdapter extends TypeAdapter<Book> {

  @Override
  public Book read(final JsonReader in) throws IOException {
    final Book book = new Book();

    in.beginObject();
    while (in.hasNext()) {
      switch (in.nextName()) {
      case "isbn":
        book.setIsbn(in.nextString());
        break;
      case "title":
        book.setTitle(in.nextString());
        break;
      case "authors":
        book.setAuthors(in.nextString().split(";"));
        break;
      }
    }
    in.endObject();

    return book;
  }

  @Override
  public void write(final JsonWriter out, final Book book) throws IOException {
    out.beginObject();
    out.name("isbn").value(book.getIsbn());
    out.name("title").value(book.getTitle());
    out.name("authors").value(StringUtils.join(book.getAuthors(), ";"));
    out.endObject();
  }

}
