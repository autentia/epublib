package nl.siegmann.epublib.epub;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;

import nl.siegmann.epublib.Constants;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.service.MediatypeService;
import nl.siegmann.epublib.util.ResourceUtil;
import nl.siegmann.epublib.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


/**
 * Reads an epub file.
 * 
 * @author paul
 *
 */
public class EpubReader {

	private static final Logger log = LoggerFactory.getLogger(EpubReader.class);
	private BookProcessor bookProcessor = BookProcessor.IDENTITY_BOOKPROCESSOR;
	
	public Book readEpub(URL url) throws IOException {
		return readEpub(url, Constants.ENCODING);
	}
	
	/**
	 * Read epub from URL
	 * 
	 * @param in the inputstream from which to read the epub
	 * @param encoding the encoding to use for the html files within the epub
	 * @return
	 * @throws IOException
	 */
	public Book readEpub(URL url, String encoding) throws IOException {
		Book result = new Book();
		Map<String, Resource> resources = readResources(url, encoding);
		handleMimeType(result, resources);
		String packageResourceHref = getPackageResourceHref(result, resources);
		Resource packageResource = processPackageResource(packageResourceHref, result, resources);
		result.setOpfResource(packageResource);
		Resource ncxResource = processNcxResource(packageResource, result);
		result.setNcxResource(ncxResource);
		result = postProcessBook(result);
		return result;
	}

	private Book postProcessBook(Book book) {
		if (bookProcessor != null) {
			book = bookProcessor.processBook(book);
		}
		return book;
	}

	private Resource processNcxResource(Resource packageResource, Book book) {
		return NCXDocument.read(book, this);
	}

	private Resource processPackageResource(String packageResourceHref, Book book, Map<String, Resource> resources) {
		Resource packageResource = resources.remove(packageResourceHref);
		try {
			PackageDocumentReader.read(packageResource, this, book, resources);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return packageResource;
	}

	private String getPackageResourceHref(Book book, Map<String, Resource> resources) {
		String defaultResult = "OEBPS/content.opf";
		String result = defaultResult;

		Resource containerResource = resources.remove("META-INF/container.xml");
		if(containerResource == null) {
			return result;
		}
		try {
			Document document = ResourceUtil.getAsDocument(containerResource);
			Element rootFileElement = (Element) ((Element) document.getDocumentElement().getElementsByTagName("rootfiles").item(0)).getElementsByTagName("rootfile").item(0);
			result = rootFileElement.getAttribute("full-path");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		if(StringUtil.isBlank(result)) {
			result = defaultResult;
		}
		return result;
	}

	private void handleMimeType(Book result, Map<String, Resource> resources) {
		resources.remove("mimetype");
	}

	private Map<String, Resource> readResources(URL url, String defaultHtmlEncoding) throws IOException {
		Map<String, Resource> result = new HashMap<String, Resource>();
		ZipInputStream in = new ZipInputStream(url.openStream());

		for (ZipEntry zipEntry = in.getNextEntry(); zipEntry != null; zipEntry = in.getNextEntry()) {
			if (!zipEntry.isDirectory()) {
				Resource resource = new Resource(url, zipEntry.getName());

				if (resource.getMediaType() == MediatypeService.XHTML) {
					resource.setInputEncoding(defaultHtmlEncoding);
				}

				result.put(resource.getHref(), resource);
			}
		}

		return result;
	}
}
