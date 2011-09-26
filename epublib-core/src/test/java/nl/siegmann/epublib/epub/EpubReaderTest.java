package nl.siegmann.epublib.epub;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.epub.EpubWriter;
import nl.siegmann.epublib.service.MediatypeService;

import junit.framework.TestCase;


public class EpubReaderTest extends TestCase {
	
	public void testCover_only_cover() {
		try {
			Book book = new Book();
			
			book.setCoverImage(new Resource(this.getClass().getResourceAsStream("/book1/cover.png"), "cover.png"));

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			(new EpubWriter()).write(book, out);
			byte[] epubData = out.toByteArray();
			Book readBook = new EpubReader().readEpub(getURLFromTempFile(epubData));
			assertNotNull(readBook.getCoverImage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		}

	}

	public void testCover_cover_one_section() {
		try {
			Book book = new Book();
			
			book.setCoverImage(new Resource(this.getClass().getResourceAsStream("/book1/cover.png"), "cover.png"));
			book.addSection("Introduction", new Resource(this.getClass().getResourceAsStream("/book1/chapter1.html"), "chapter1.html"));
			book.generateSpineFromTableOfContents();
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			(new EpubWriter()).write(book, out);
			byte[] epubData = out.toByteArray();
			Book readBook = new EpubReader().readEpub(getURLFromTempFile(epubData));
			assertNotNull(readBook.getCoverPage());
			assertEquals(1, readBook.getSpine().size());
			assertEquals(1, readBook.getTableOfContents().size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testReadEpub_opf_ncx_docs() {
		try {
			Book book = new Book();
			
			book.setCoverImage(new Resource(this.getClass().getResourceAsStream("/book1/cover.png"), "cover.png"));
			book.addSection("Introduction", new Resource(this.getClass().getResourceAsStream("/book1/chapter1.html"), "chapter1.html"));
			book.generateSpineFromTableOfContents();
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			(new EpubWriter()).write(book, out);
			byte[] epubData = out.toByteArray();
			Book readBook = new EpubReader().readEpub(getURLFromTempFile(epubData));
			assertNotNull(readBook.getCoverPage());
			assertEquals(1, readBook.getSpine().size());
			assertEquals(1, readBook.getTableOfContents().size());
			assertNotNull(readBook.getOpfResource());
			assertNotNull(readBook.getNcxResource());
			assertEquals(MediatypeService.NCX, readBook.getNcxResource().getMediaType());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	private URL getURLFromTempFile(byte[] epubData) throws IOException {
		final File tempFile = File.createTempFile("test", ".txt");
		final OutputStream fileOutputStream = new FileOutputStream(tempFile);

		fileOutputStream.write(epubData);
		fileOutputStream.close();
		
		return tempFile.toURI().toURL();
	}
}
