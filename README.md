Java objects can be serialised to JSON strings and deserialised back using <code>JsonSerializer</code> (<a href="http://www.javacreed.com/gson-serialiser-example/" target="_blank">Article</a> and <a href="https://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/JsonSerializer.html" target="_blank">Java Doc</a>) and the <code>JsonDeserializer</code> (<a href="http://www.javacreed.com/gson-deserialiser-example/" target="_blank">Article</a> and <a href="https://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/JsonDeserializer.html" target="_blank">Java Doc</a>) respectively.   These two classes simplify the translation between these two realms but add an extra layer which can be avoided.  Instead of the <code>JsonSerializer</code> or <code>JsonDeserializer</code> we can use an instance of <code>TypeAdapter</code> (<a href="http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/TypeAdapter.html" target="_blank">Java Doc</a>) which can serialise and deserialise JSON objects efficiently as we will see in this article.  


This article assumes that the reader is already familiar with Gson and encourages the reader to read the following articles before proceeding:

<ol>
<li><a href="/simple-gson-example/">Simple Gson Example</a></li>
<li><a href="/gson-deserialiser-example/">Gson Deserialiser Example</a></li>
<li><a href="/gson-serialiser-example/">Gson Serialiser Example</a></li>
</ol>


Most of the examples will not contain the whole code and may omit fragments which are not relevant to the example being discussed. The readers can download or view all code from the above link.


<h2>Introduction</h2>


The <code>JsonSerializer</code> and <code>JsonDeserializer</code> classes makes use of  an intermediate layer of objects.  The Java or JSON objects are first converted to <code>JsonElement</code> (the intermediate layer) and then converted to Java or JSON string as shown in the following image.


<a href="http://www.javacreed.com/wp-content/uploads/2014/03/Intermediate-Layer.png" class="preload" rel="prettyphoto" title="" ><img src="http://www.javacreed.com/wp-content/uploads/2014/03/Intermediate-Layer.png" alt="Intermediate Layer" width="703" height="517" class="size-full wp-image-5087" /></a>


This intermediate layer can be avoided by using the <code>TypeAdapter</code> instead of <code>JsonSerializer</code> or <code>JsonDeserializer</code>.  The <code>TypeAdapter</code> is more efficient than the  <code>JsonSerializer</code> and <code>JsonDeserializer</code> as it skips the intermediate layer.  This fact is also documented in the class Java Doc.


<blockquote cite="https://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/JsonSerializer.html">
New applications should prefer TypeAdapter, whose streaming API is more efficient than this interface's tree API.
</blockquote>


With that said, the <code>JsonSerializer</code> and <code>JsonDeserializer</code> provide a safety cushion which is very convenient as it mitigates the risk of producing invalid JSON strings.  The image shown above shows how objects are serialised using the <code>JsonSerializer</code>.   The Java objects are converted to <code>JsonElement</code>s first, and then converted to JSON string.  This process creates a set of temporary objects which are then converted to JSON string.   These objects are converted to JSON string using an internal implementation of the <code>TypeAdapter</code>.   The <code>TypeAdapter</code> can take any Java object (including objects of type <code>JsonElement</code>) and converts it to JSON string as shown in the following image.


<a href="http://www.javacreed.com/wp-content/uploads/2014/03/Skipping-Intermediate-Later.png" class="preload" rel="prettyphoto" title="" ><img src="http://www.javacreed.com/wp-content/uploads/2014/03/Skipping-Intermediate-Later.png" alt="Skipping Intermediate Later" width="565" height="372" class="size-full wp-image-5088" /></a>

 
The <code>TypeAdapter</code> is an abstract class at has two abstract methods.   The <code>write()</code> (<a href="http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/TypeAdapter.html#write(com.google.gson.stream.JsonWriter, T)" target="_blank">Java Doc</a>) method takes an instance of the <code>JsonWriter</code> (<a href="http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/stream/JsonWriter.html" target="_blank">Java Doc</a>) and the object to be serialised.  The object is written to the <code>JsonWriter</code> in a similar manner an object is printed to a <code>PrintStream</code> (<a href="http://docs.oracle.com/javase/7/docs/api/java/io/PrintStream.html" target="_blank">Java Doc</a>).  The <code>read()</code> (<a href="http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/TypeAdapter.html#read(com.google.gson.stream.JsonReader)" target="_blank">Java Doc</a>) method takes an instance of the <code>JsonReader</code> (<a href="http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/stream/JsonReader.html" target="_blank">Java Doc</a>) and returns an instance of the deserialised object.


<a href="http://www.javacreed.com/wp-content/uploads/2014/03/TypeAdapter-write-and-read-methods.png" class="preload" rel="prettyphoto" title="TypeAdapter write and read methods" ><img src="http://www.javacreed.com/wp-content/uploads/2014/03/TypeAdapter-write-and-read-methods.png" alt="TypeAdapter write and read methods" width="565" height="465" class="size-full wp-image-5101" /></a>


Similar to the <code>JsonSerializer</code> and <code>JsonDeserializer</code>, the <code>TypeAdapter</code> needs to be registered, as shown in the following code fragment, before it can be used.

<pre>
    final GsonBuilder gsonBuilder = new GsonBuilder();
    <span class="highlight">gsonBuilder.registerTypeAdapter(Book.class, new BookTypeAdapter());</span>
    final Gson gson = gsonBuilder.create();
</pre>


In the following sections we will see how to use the <code>TypeAdapter</code> to serialise Java objects into JSON strings and deserialise them back in more detail.


<h2>Simple TypeAdapter Example</h2>


Consider the following class.


<pre>
package com.javacreed.examples.gson.part1;

public class Book {

  private String[] authors;
  private String isbn;
  private String title;

  <span class="comments">// Methods removed for brevity</span>
}
</pre>


This class has three fields one of which is an array of <code>String</code>.  The following class shows an example of a <code>TypeAdapter</code>, named <code>BookTypeAdapter</code>, that can serialise and deserialise instances of the <code>Book</code> class shown above.


<pre>
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
</pre>


This class may look cryptic.  Let us split it in smaller parts and describe each part individually.


<h3>The write() Method</h3>


The <code>write()</code> method takes two parameters.  The first parameter is the <code>JsonWriter</code> instance, where the book will be written.  This can be thought of as a <code>PrintStream</code> that has special methods that will allow us to construct a valid JSON string.  In the event we make a mistake and attempt to produce an invalid JSON string, the <code>JsonWriter</code> will throw an <code>IllegalStateException</code> (<a href="http://docs.oracle.com/javase/7/docs/api/java/lang/IllegalStateException.html" target="_blank">Java Doc</a>) indicating that the last action was not valid.  The second parameter represents the object to be serialised, which can be <code>null</code>. 


<pre>
  @Override
  public void write(final JsonWriter out, final Book book) throws IOException {
    out.beginObject();
    out.name("isbn").value(book.getIsbn());
    out.name("title").value(book.getTitle());
    out.name("authors").value(StringUtils.join(book.getAuthors(), ";"));
    out.endObject();
  }
</pre>


The <code>JsonWriter</code> method provides specific methods that can be used to construct a JSON objects.  Here we are using some.


<ol>
<li>The book will be represented as a JSON object thus we start with the <code>beginObject()</code> (<a href="http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/stream/JsonWriter.html#beginObject()" target="_blank">Java Doc</a>) method call.
 
<pre>
    out.beginObject();
</pre>

The above call will produce the following JSON output

<pre>
<span class="highlight">{</span>
</pre>


If instead we want to create a JSON array, then we need to use the <code>beginArray()</code> (<a href="http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/stream/JsonWriter.html#beginArray()" target="_blank">Java Doc</a>) instead, which indicates that beginning of an array.  


Please note that we need to start with either a JSON object or a JSON array.  JSON data must be contained in one of these.
</li>

<li>The <code>beginObject()</code> allows us to add the required fields.  Note that we cannot add the fields without first calling the <code>beginObject()</code>.  A field must have a <em>name</em> and a <em>value</em>, which value can be <code>null</code>.  This can be achieved by invoking the <code>name()</code> (<a href="http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/stream/JsonWriter.html#name(java.lang.String)" target="_blank">Java Doc</a>) method followed by the <code>value()</code> (<a href="http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/stream/JsonWriter.html#value(java.lang.String)" target="_blank">Java Doc</a>) method. 


<pre>
    out.name("isbn").value(book.getIsbn());
</pre>


The above code fragment will produce the following highlighted JSON output.  Please note that the curly bracket was produced by the <code>beginObject()</code> described before.

<pre>
{
<span class="highlight">  "isbn": "978-0321336781"</span>
</pre>

Here we used, what is referred to as, method chaining (<a href="http://en.wikipedia.org/wiki/Method_chaining" target="_blank">Wiki</a>) which allows us to have the name and value on one line.  Please note that we could have written the above as shown next.

<pre>
    out.name("isbn").
    out.value(book.getIsbn());
</pre>

Both examples will produce the same result.
</li>

<li>The book title and the authors are added in a similar fashion.
<pre>
    out.name("title").value(book.getTitle());
    out.name("authors").value(StringUtils.join(book.getAuthors(), ";"));
</pre>

The above code fragment will produce the following highlighted JSON output, which is appended to the JSON produced so far.

<pre>
{
  "isbn": "978-0321336781",
<span class="highlight">  "title": "Java Puzzlers: Traps, Pitfalls, and Corner Cases",
  "authors": "Joshua Bloch;Neal Gafter"</span>
</pre>

Please note that the authors are added as a single string joined by a semi-colon using the Apache Commons Lang library (<a href="http://commons.apache.org/proper/commons-lang/" target="_blank">homepage</a>) class <code>StringUtils</code> (<a href="http://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/StringUtils.html" target="_blank">Java Doc</a>) to keep this first example as simple as possible.
</li>

<li>Finally, we need to close the JSON object by invoking the following method.

<pre>
    out.endObject();
</pre>

The above call will produce the following highlighted JSON output

<pre>
{
  "isbn": "978-0321336781",
  "title": "Java Puzzlers: Traps, Pitfalls, and Corner Cases",
  "authors": "Joshua Bloch;Neal Gafter"
<span class="highlight">}</span>
</pre>

It is very important to close the JSON object as otherwise we will produce an invalid JSON string.  Unfortunately, no exceptions will be thrown at this stage, but it will fail during reading with a <code>JsonSyntaxException</code> (<a href="https://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/JsonSyntaxException.html" target="_blank">Java Doc</a>) similar to the one shown below.

<pre>
Exception in thread "main" com.google.gson.JsonSyntaxException: java.io.EOFException: End of input at line 4 column 40
	at com.google.gson.Gson.fromJson(Gson.java:813)
	at com.google.gson.Gson.fromJson(Gson.java:768)
	at com.google.gson.Gson.fromJson(Gson.java:717)
	at com.google.gson.Gson.fromJson(Gson.java:689)
	at com.javacreed.examples.gson.part1.Main.main(Main.java:41)
Caused by: java.io.EOFException: End of input at line 4 column 40
	at com.google.gson.stream.JsonReader.nextNonWhitespace(JsonReader.java:1377)
	at com.google.gson.stream.JsonReader.doPeek(JsonReader.java:471)
	at com.google.gson.stream.JsonReader.hasNext(JsonReader.java:403)
	at com.javacreed.examples.gson.part1.BookTypeAdapter.read(BookTypeAdapter.java:33)
	at com.javacreed.examples.gson.part1.BookTypeAdapter.read(BookTypeAdapter.java:1)
	at com.google.gson.Gson.fromJson(Gson.java:803)
	... 4 more
</pre>
</li>
</ol>


Consider the following <code>Book</code> instance.


<pre>
    final Book book = new Book();
    book.setAuthors(new String[] { "Joshua Bloch", "Neal Gafter" });
    book.setTitle("Java Puzzlers: Traps, Pitfalls, and Corner Cases");
    book.setIsbn("978-0321336781");
</pre>


When serialised with the above <code>BookTypeAdapter</code> we will produce the following JSON.


<pre>
{
  "isbn": "978-0321336781",
  "title": "Java Puzzlers: Traps, Pitfalls, and Corner Cases",
  "authors": "Joshua Bloch;Neal Gafter"
}
</pre>


This concludes our description of the <code>write()</code> method.  In the following section we will see how the <code>read()</code> works.


<h3>The read() Method</h3>


The <code>TypeAdapter</code> has another abstract method, which purpose is to convert JSON string into Java Objects.  The <code>read()</code> method takes an instance of <code>JsonReader</code> and creates the Java Object from it as shown in the following fragment.


<pre>
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
</pre>


This method will take the parsed JSON string as an instance of <code>JsonReader</code> and converts it back to an instance of <code>Book</code>.  The <code>JsonReader</code> is very similar to an <code>InputStream</code> (a href="http://docs.oracle.com/javase/7/docs/api/java/io/InputStream.html" target="_blank">Java Doc</a>).  It reads JSON parts, so to call them, sequentially.  The <code>JsonReader</code> can peek but it cannot skip to a given part without first reading all preceding JSON parts.


The <code>read()</code> method can appear more complex than its counterpart because it involves more control logic.  Let us break this down further and understand each individual part.


<ol>
<li>We have a JSON object that includes three fields as shown below.
<pre>
{
  "isbn": "978-0321336781",
  "title": "Java Puzzlers: Traps, Pitfalls, and Corner Cases",
  "authors": "Joshua Bloch;Neal Gafter"
}
</pre>

The first thing we need to do is to read the object as shown next.

<pre>
    in.beginObject();
</pre>

This will read the curly brackets, highlighted below, and will allow us to read the name/values within the JSON object.

<pre>
<span class="highlight">{</span>
  "isbn": "978-0321336781",
  "title": "Java Puzzlers: Traps, Pitfalls, and Corner Cases",
  "authors": "Joshua Bloch;Neal Gafter"
}
</pre>

</li>
<li>A JSON object contains a list of name/value pairs.  Using the <code>JsonReader</code> we cannot read a particular field by its name, as we can do with the <code>JsonDeserializer</code>.  We cannot, for example, read the book title before we first reading its ISBN.


We need to invoke the <code>nextName()</code> method first to read the next field name.  Then we can read the field value by invoking the <code>nextString()</code> method as shown in the following example.


<pre>
  String name = in.nextName();
  String value = in.nextString();
</pre>


We cannot skip the name, even though we know that the book ISBN is the first field.  If we do so (that is, skip the invocation of the <code>nextName()</code> method), an error similar to the following will be thrown

<pre>
Caused by: java.lang.IllegalStateException: Expected a string but was NAME at line 2 column 4
	at com.google.gson.stream.JsonReader.nextString(JsonReader.java:821)
	at com.javacreed.examples.gson.part1.BookTypeAdapter.read(BookTypeAdapter.java:32)
	at com.javacreed.examples.gson.part1.BookTypeAdapter.read(BookTypeAdapter.java:1)
	at com.google.gson.Gson.fromJson(Gson.java:803)
	... 4 more
</pre>
</li>

<li>The <code>JsonReader</code> has another method called <code>hasNext()</code> (<a href="http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/stream/JsonReader.html#hasNext()" target="_blank">Java Doc</a>), which returns <code>true</code> if more name/value pairs are available, <code>false</code> otherwise.  Combining this within a loop and a switch control statement we can have a generic method for deserialising objects.

<pre>
    while (in.hasNext()) {
      switch (in.nextName()) {
      }
    }
</pre>


In a nutshell, the above code fragment iterates through all name/value pairs and then using the switch statement is filters the fields but their names.  The method <code>nextName()</code> returns the name of the next field.  We need to create a case statement for every possible field and assign the correct logic.  The following code fragment shows the complete parsing process.

<pre>
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
</pre>

This approach is quite generic and it does not depend on the order of the fields.   Unfortunately it is quite verbose and can get messy especially when dealing with large objects.
</li>

<li>Once all fields are parsed, we need to close the JSON object by invoking the <code>endObject()</code> method.

<pre>
    in.endObject();
</pre>

Failing to do so will cause an <code>JsonIOException</code> (<a href="https://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/JsonIOException.html" target="_blank">Java Doc</a>) similar to the one shown next.

<pre>
Exception in thread "main" com.google.gson.JsonIOException: JSON document was not fully consumed.
	at com.google.gson.Gson.assertFullConsumption(Gson.java:776)
	at com.google.gson.Gson.fromJson(Gson.java:769)
	at com.google.gson.Gson.fromJson(Gson.java:717)
	at com.google.gson.Gson.fromJson(Gson.java:689)
	at com.javacreed.examples.gson.part1.Main.main(Main.java:41)
</pre>
</li>
</ol>

This concludes our description about the <code>read()</code> method.  In the next section we will see how to configure and use our <code>TypeAdapter</code>.


<h3>Configuration</h3>


Before we can use the <code>BookTypeAdapter</code> we need to register it with the <code>GsonBuilder</code> instance as highlighted below.

<pre>
    final GsonBuilder gsonBuilder = new GsonBuilder();
    <span class="highlight">gsonBuilder.registerTypeAdapter(Book.class, new BookTypeAdapter());</span>
    gsonBuilder.setPrettyPrinting();

    final Gson gson = gsonBuilder.create();
</pre>


Once registered, Gson will use our instance of  <code>TypeAdapter</code> when serialising and deserialising objects of type <code>Book</code>.  The following class shows a complete example of how to use the <code>BookTypeAdapter</code> to serialise and deserialise instances of type <code>Book</code>.


<pre>
package com.javacreed.examples.gson.part1;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Main {
  public static void main(final String[] args) throws IOException {
    final GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(Book.class, new BookTypeAdapter());
    gsonBuilder.setPrettyPrinting();

    final Gson gson = gsonBuilder.create();

    final Book book = new Book();
    book.setAuthors(new String[] { "Joshua Bloch", "Neal Gafter" });
    book.setTitle("Java Puzzlers: Traps, Pitfalls, and Corner Cases");
    book.setIsbn("978-0321336781");

    final String json = gson.toJson(book);
    System.out.println("Serialised");
    System.out.println(json);

    final Book parsedBook = gson.fromJson(json, Book.class);
    System.out.println("\nDeserialised");
    System.out.println(parsedBook);
  }
}
</pre>


The above will produce the following output.


<pre>
Serialised
{
  "isbn": "978-0321336781",
  "title": "Java Puzzlers: Traps, Pitfalls, and Corner Cases",
  "authors": "Joshua Bloch;Neal Gafter"
}

Deserialised
Java Puzzlers: Traps, Pitfalls, and Corner Cases [978-0321336781]
Written by:
  >> Joshua Bloch
  >> Neal Gafter
</pre>


This concludes our first example of the <code>TypeAdapter</code>.  In this example we saw how to use <code>TypeAdapter</code> to serialise and deserialise Java Object.  In the next section we will see how to create compact JSON strings using the <code>TypeAdapter</code>.


<h2>Compact JSON with TypeAdapter</h2>


The JSON string can be compacted and any unnecessary text removed as shown in the following JSON example.


<pre>
["978-0321336781","Java Puzzlers: Traps, Pitfalls, and Corner Cases","Joshua Bloch","Neal Gafter"]
</pre>

The above JSON example contains the information of our book as a JSON array instead of a JSON object.  This makes the JSON string smaller, but also very brittle.  The first JSON element in this array is the book ISBN, followed by the book title and finally its authors.  The order of the array elements is very important and thus we cannot skip any of the first two elements.  In the event where the book ISBN or title are missing, a JSON <code>null</code> value needs to be used to indicate that this value is missing.


In order to obtain this result, we need to modify the <code>TypeAdapter</code>'s <code>write()</code> method.  Instead of creating a JSON object, we need to create an array as shown next.


<pre>
  @Override
  public void write(final JsonWriter out, final Book book) throws IOException {
    out.beginArray();
    out.value(book.getIsbn());
    out.value(book.getTitle());
    for (final String author : book.getAuthors()) {
      out.value(author);
    }
    out.endArray();
  }
</pre>


This version of the <code>write()</code> method is very similar to the previous version, with minor differences.  For completeness, the <code>write()</code> is described in more detail in the following points.


<ol>
<li>
The <code>write()</code> method starts with opening an array.

<pre>
    out.beginArray();
</pre>

The above call will produce the following JSON output

<pre>
<span class="highlight">[</span>
</pre>
</li>

<li>
The book ISBN is added first followed by the book title. 

<pre>
    out.value(book.getIsbn());
    out.value(book.getTitle());
</pre>

Different from JSON objects, arrays only contain values.  Here we did not use the <code>name()</code> method, as the array elements do not have a name.  This reduces the JSON size drastically, especially when you have large number of objects.  The above code fragment will produce the following highlighted JSON output.  Please note that the square bracket (<code>[</code>) was produced by the <code>beginArray()</code> described before.

<pre>
["978-0321336781"<span class="highlight">,"Java Puzzlers: Traps, Pitfalls, and Corner Cases"</span>
</pre>
</li>

<li>
The authors are too added as array elements.  Each author is added to a new element.  This is different from what we did before, where all authors' names were added as a single string.

<pre>
    for (final String author : book.getAuthors()) {
      out.value(author);
    }
</pre>

The above code fragment will produce the following highlighted JSON output.

<pre>
["978-0321336781","Java Puzzlers: Traps, Pitfalls, and Corner Cases"<span class="highlighted">,"Joshua Bloch","Neal Gafter"</span>]
</pre>
</li>

<li>Finally, when all items are added to the array, this is closed, indicating that no more elements will be added to the JSON array.

<pre>
    out.endArray();
</pre>
</li>
</ol>


The <code>read()</code> method expects the JSON array as produced by the <code>write()</code> method.  Following is the updated <code>read()</code> method.

<pre>
  @Override
  public Book read(final JsonReader in) throws IOException {
    final Book book = new Book();

    in.beginArray();
    book.setIsbn(in.nextString());
    book.setTitle(in.nextString());
    final List<String> authors = new ArrayList<>();
    while (in.hasNext()) {
      authors.add(in.nextString());
    }
    book.setAuthors(authors.toArray(new String[authors.size()]));
    in.endArray();

    return book;
  }
</pre>


The <code>read()</code> method is simpler when compared with the previous version as this time it does not deal with names.  As pointed earlier in this article, while this approach looks simpler, it is more brittle as the position of the array elements is very important.  The following points describe the <code>read()</code> in more detail.


<ol>
<li>The JSON object is a JSON array, therefore we have to open an array
<pre>
    in.beginArray();
</pre>

This will read the open square bracket as highlighted below.

<pre>
<span class="highlighted">[</span>"978-0321336781","Java Puzzlers: Traps, Pitfalls, and Corner Cases","Joshua Bloch","Neal Gafter"]
</pre>
</li> 

<li>Then we read the book ISBN and title respectively.
<pre>
    book.setIsbn(in.nextString());
    book.setTitle(in.nextString());
</pre>

Note that arrays only contain values.  Therefore we cannot invoke the <code>name()</code> method.  This will read the first two JSON array elements highlighted below.

<pre>
[<span class="highlighted">"978-0321336781","Java Puzzlers: Traps, Pitfalls, and Corner Cases"</span>,"Joshua Bloch","Neal Gafter"]
</pre>
</li> 

<li>All elements following the title are considered as the book authors.  Therefore we can iterate through the remaining elements as shown in the following fragment.

<pre>
    final List<String> authors = new ArrayList<>();
    while (in.hasNext()) {
      authors.add(in.nextString());
    }
    book.setAuthors(authors.toArray(new String[authors.size()]));
</pre>

Once all authors are read, we convert the list to an array and set the book's authors.
</li> 
<li>Once the book is read, we need to close the array.
<pre>
    in.endArray();
</pre>

The above call will read the closing square bracket highlighted below.

<pre>
["978-0321336781","Java Puzzlers: Traps, Pitfalls, and Corner Cases","Joshua Bloch","Neal Gafter"<span class="highlighted">]</span>
</pre>
</li> 
</ol>


This version of the <code>BookTypeAdapter</code> will produce the following response when used to serialise and deserialise the same instance of <code>Book</code>


<pre>
Serialise
["978-0321336781","Java Puzzlers: Traps, Pitfalls, and Corner Cases","Joshua Bloch","Neal Gafter"]

Deserialised
Java Puzzlers: Traps, Pitfalls, and Corner Cases [978-0321336781]
Written by:
  >> Joshua Bloch
  >> Neal Gafter
</pre>


In this section we saw how to take advantage of the <code>TypeAdapter</code> to create smaller JSON objects, by removing unnecessary parts.  In the following section we will see how to handle nested objects using the <code>TypeAdapter</code>.


<h2>Nested Objects</h2>


A book is written by one or more authors, where the latter can be a more complex Java object than a simple <code>String</code>.  So far the book authors were represented as an array of <code>String</code>.  Consider the following class.


<pre>
package com.javacreed.examples.gson.part3;

public class Author {

  private int id;
  private String name;

  <span class="comments">// Methods removed for brevity</span>
}
</pre>


The <code>Author</code> class shown above comprise the author id and his/her name.  The <code>Book</code> class is modified to save authors as an array of <code>Author</code> instead of an array of <code>String</code>.


<pre>
package com.javacreed.examples.gson.part3;

public class Book {

  <span class="highlight">private Author[] authors;</span>
  private String isbn;
  private String title;

  <span class="comments">// Methods removed for brevity</span>
}
</pre>


Before we discuss the <code>TypeAdapter</code> we need to first agree on the JSON format to represent these classes.  Do we need to create nested objects using the <code>TypeAdapter</code> (as we did in the first section), or shall we create more compact JSON (similar to our second section)?  Both are valid formats and the answer depends a lot on the current situation.  For this example we will opt for the compact version, as shown below.  For completeness we will discuss the other approach later on.


<pre>
["978-0321336781","Java Puzzlers: Traps, Pitfalls, and Corner Cases",1,"Joshua Bloch",2,"Neal Gafter"]
</pre>


The first two elements in the array are still occupied by the book ISBN and title.  Each author now takes two elements, one for its id, followed by the author's name.  I would like to stress out that while this approach reduces the size of the JSON string, it also makes it more brittle.


The <code>BookTypeAdapter</code> needs to be updated to cater for the new changes in the <code>Book</code> and <code>Author</code> classes.  Let start with the <code>write()</code> method.  Instead of a <code>String</code> entry for every author, we first add the author's id followed by his/her name.


<pre>
  @Override
  public void write(final JsonWriter out, final Book book) throws IOException {
    out.beginArray();
    out.value(book.getIsbn());
    out.value(book.getTitle());
    <span class="highlight">for (final Author author : book.getAuthors()) {
      out.value(author.getId());
    out.endArray();
  }

      out.value(author.getName());
    }</span></pre>


The <code>read()</code> method  needs to create <code>Author</code>s instances instead of <code>String</code>.  It first reads the author's id and then his/her name as highlighted below.


<pre>
  @Override
  public Book read(final JsonReader in) throws IOException {
    final Book book = new Book();

    in.beginArray();
    book.setIsbn(in.nextString());
    book.setTitle(in.nextString());
    <span class="highlight">final List&lt;Author&gt; authors = new ArrayList&lt;&gt;();
    while (in.hasNext()) {
      final int id = in.nextInt();
      final String name = in.nextString();
      authors.add(new Author(id, name));
    }
    book.setAuthors(authors.toArray(new Author[authors.size()]));</span>
    in.endArray();

    return book;
  }
</pre>


The changes required were quite minimal as we saw in the above example.


Instead of the compact JSON format we can use JSON objects as we will see in the following examples.  The following JSON shows the same book but in different format.  This example uses JSON objects and the authors are represented as a JSON array of JSON objects.


<pre>
{
  "isbn": "978-0321336781",
  "title": "Java Puzzlers: Traps, Pitfalls, and Corner Cases",
  "authors": [
    {
      "id": 1,
      "name": "Joshua Bloch"
    },
    {
      "id": 2,
      "name": "Neal Gafter"
    }
  ]
}
</pre>


The above JSON is more structured and less brittle when compared with the JSON array compact version.  On the other hand this version takes more space and this can impact performance when working with large number of objects. 


The <code>write()</code> method needs to produce JSON objects instead.  Furthermore, the authors are represented as JSON array of JSON objects.


<pre>
  @Override
  public void write(final JsonWriter out, final Book book) throws IOException {
    out.beginObject();
    out.name("isbn").value(book.getIsbn());
    out.name("title").value(book.getTitle());
    out.name("authors").beginArray();
    for (final Author author : book.getAuthors()) {
      out.beginObject();
      out.name("id").value(author.getId());
      out.name("name").value(author.getName());
      out.endObject();
    }
    out.endArray();
    out.endObject();
  }
</pre>


The <code>write()</code> shown above produces the required JSON.  It is more complex than the compact counterpart and requires more code.  Same applies to the <code>read()</code> method show next.


<pre>
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
        in.beginArray();
        final List<Author> authors = new ArrayList<>();
        while (in.hasNext()) {
          in.beginObject();
          final Author author = new Author();
          while (in.hasNext()) {
            switch (in.nextName()) {
            case "id":
              author.setId(in.nextInt());
              break;
            case "name":
              author.setName(in.nextString());
              break;
            }
          }
          authors.add(author);
          in.endObject();
        }
        book.setAuthors(authors.toArray(new Author[authors.size()]));
        in.endArray();
        break;
      }
    }
    in.endObject();

    return book;
  }
</pre>

Nesting added to this method complexity as we saw in the above example.  This method size can be reduced using reflection.  Using reflection we can remove most of the boilerplate code but will make our code somewhat harder to understand.


<h2>Conclusion</h2>


The <code>TypeAdapter</code> has two abstract methods, the <code>write()</code> and <code>read()</code> methods and these are used to serialise and deserialise Java objects to JSON objects and vise-versa.  The <code>TypeAdapter</code> is more efficient <code>JsonSerializer</code> and <code>JsonDeserializer</code> and this fact is also documented in the same class Java Docs.


Working with nested objects is tricky as these methods do not have access to some sort of context (such as <code>JsonSerializationContext</code> (<a href="https://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/JsonSerializationContext.html" target="_target">Java Doc</a>)).  You cannot delegate the serialisation, or deserialisation, to the context as we used to do with the <code>JsonSerializer</code> or <code>JsonDeserializer</code> as shown next.


<pre>
@Override
  public JsonElement serialize(final Book book, final Type typeOfSrc, final JsonSerializationContext context) {
    final JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("isbn", book.getIsbn());
    jsonObject.addProperty("title", book.getTitle());

    <span class="highlight" target="_blank">final JsonElement jsonAuthros = context.serialize(book.getAuthors());
    jsonObject.add("authors", jsonAuthros);</span>

    return jsonObject;
  }
</pre>


This is very convenient as it makes the <code>JsonSerializer</code> more coherent (<a href="http://en.wikipedia.org/wiki/Cohesion_(computer_science)" target="_target">Wiki</a>).  The same thing can achieve using a <code>TypeAdapterFactory</code> (<a href="http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/TypeAdapterFactory.html" target="_target">Java Doc</a> and <a href="" target="_target">Article</a>).

