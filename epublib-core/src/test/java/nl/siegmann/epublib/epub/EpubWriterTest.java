package nl.siegmann.epublib.epub;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.GuideReference;
import nl.siegmann.epublib.domain.Identifier;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.epub.EpubWriter;
import nl.siegmann.epublib.util.CollectionUtil;


import junit.framework.TestCase;

public class EpubWriterTest extends TestCase {

	public void testBook1() {
		try {
			// create test book
			Book book = createTestBook();
			
			// write book to byte[]
			byte[] bookData = writeBookToByteArray(book);
//			FileOutputStream fileOutputStream = new FileOutputStream("foo.zip");
//			fileOutputStream.write(bookData);
//			fileOutputStream.flush();
//			fileOutputStream.close();
			assertNotNull(bookData);
			assertTrue(bookData.length > 0);
			
			// read book from byte[]
			Book readBook = new EpubReader().readEpub(getURLFromTempFile(bookData));
			
			// assert book values are correct
			assertEquals(book.getMetadata().getTitles(), readBook.getMetadata().getTitles());
			assertEquals(Identifier.Scheme.ISBN, CollectionUtil.first(readBook.getMetadata().getIdentifiers()).getScheme());
			assertEquals(CollectionUtil.first(book.getMetadata().getIdentifiers()).getValue(), CollectionUtil.first(readBook.getMetadata().getIdentifiers()).getValue());
			assertEquals(CollectionUtil.first(book.getMetadata().getAuthors()), CollectionUtil.first(readBook.getMetadata().getAuthors()));
			assertEquals(1, readBook.getGuide().getGuideReferencesByType(GuideReference.COVER).size());
			assertNotNull(book.getCoverPage());
			assertNotNull(book.getCoverImage());
			assertEquals(4, readBook.getTableOfContents().size());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private URL getURLFromTempFile(byte[] epubData) throws IOException {
		final File tempFile = File.createTempFile("test", ".txt");
		final OutputStream fileOutputStream = new FileOutputStream(tempFile);

		fileOutputStream.write(epubData);
		fileOutputStream.close();
		
		return tempFile.toURI().toURL();
	}

	private Book createTestBook() throws IOException {
		Book book = new Book();
		
		book.getMetadata().addTitle("Epublib test book 1");
		book.getMetadata().addTitle("test2");
		
		book.getMetadata().addIdentifier(new Identifier(Identifier.Scheme.ISBN, "987654321"));
		book.getMetadata().addAuthor(new Author("Joe", "Tester"));
		book.setCoverPage(new Resource(this.getClass().getResourceAsStream("/book1/cover.html"), "cover.html"));
		book.setCoverImage(new Resource(this.getClass().getResourceAsStream("/book1/cover.png"), "cover.png"));
		book.addSection("Chapter 1", new Resource(this.getClass().getResourceAsStream("/book1/chapter1.html"), "chapter1.html"));
		book.addResource(new Resource(this.getClass().getResourceAsStream("/book1/book1.css"), "book1.css"));
		TOCReference chapter2 = book.addSection("Second chapter", new Resource(this.getClass().getResourceAsStream("/book1/chapter2.html"), "chapter2.html"));
		book.addResource(new Resource(this.getClass().getResourceAsStream("/book1/flowers_320x240.jpg"), "flowers.jpg"));
		book.addSection(chapter2, "Chapter 2 section 1", new Resource(this.getClass().getResourceAsStream("/book1/chapter2_1.html"), "chapter2_1.html"));
		book.addSection("Chapter 3", new Resource(this.getClass().getResourceAsStream("/book1/chapter3.html"), "chapter3.html"));
		return book;
	}
	

	private byte[] writeBookToByteArray(Book book) throws IOException, XMLStreamException, FactoryConfigurationError {
		EpubWriter epubWriter = new EpubWriter();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		epubWriter.write(book, out);
		return out.toByteArray();
	}
}
