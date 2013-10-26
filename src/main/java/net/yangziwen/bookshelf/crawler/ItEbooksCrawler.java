package net.yangziwen.bookshelf.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import net.yangziwen.bookshelf.pojo.Book;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.Link;
import org.apache.tika.sax.LinkContentHandler;
import org.apache.tika.sax.TeeContentHandler;
import org.springframework.stereotype.Component;
import org.xml.sax.ContentHandler;

@Component
public class ItEbooksCrawler {
	
	private static ClientConnectionManager cm = buildClientConnectionManager();
	
	private LinkContentHandler linkHandler;

	@SuppressWarnings("static-access")
	public Book crawlPage(String pageUrl) throws Exception {
		if(StringUtils.isEmpty(pageUrl)){
			return null;
		}
		HttpEntity entity = getResponseEntity(pageUrl);
		if(entity == null) {
			return null;
		}
		InputStream in = entity.getContent();
		PipedInputStream pipedIn = new PipedInputStream();
		
		Thread parserThread = new ParserThread(in, pipedIn);
		parserThread.start();

		BufferedReader reader = new BufferedReader(new InputStreamReader(pipedIn));
		
		int sleepTime = 0, sleepInterval = 200;
		boolean connAvailable = true;
		while(pipedIn.available() <= 0) {
			// 有没有个可阻塞的方法，或者事件什么的
			Thread.currentThread().sleep(sleepInterval);
			sleepTime += sleepInterval;
			if(sleepTime >= 20000) {
				connAvailable = false;
				break;
			}
		}
		if(!connAvailable) {
			close(in);
			close(pipedIn);
			throw new RuntimeException("connection timeout in crawlPage process!");
		}
		String line = "";
		Map<String, String> resultMap = new HashMap<String, String>();
		while((line = reader.readLine()) != null) {
			line = line.trim();
			if(line.length() == 0) {
				continue;
			}
			int colonPos = line.indexOf(":");
			if(colonPos == -1) {
				continue;
			}
			resultMap.put(line.substring(0, colonPos).trim(), line.substring(colonPos + 1).trim());
		}
		resultMap.put("PageUrl", pageUrl);
		close(reader);
		close(in);
		parserThread.join();
		
		return buildNewBook(resultMap, this.linkHandler);
	}
	
	private Book buildNewBook(Map<String, String> parameter, LinkContentHandler linkHandler) {
		Map<String, Map<String, String>> linkMap = new HashMap<String, Map<String, String>>();
		for(Link link: linkHandler.getLinks()) {
			Map<String, String> m = linkMap.get(link.getText());
			if(m == null) {
				m = new HashMap<String, String>();
				linkMap.put(link.getText(), m);
			}
			m.put(link.getType(), link.getUri());
		}
		Book book = new Book();
		book.setName(parameter.get("Buy"));
		String authorName = parameter.get("By");
		if(authorName != null){
			int len = authorName.length() / 2;
			book.setAuthorName(authorName.substring(0, len));
		}
		book.setPublisher(parameter.get("Publisher"));
		book.setIsbn(parameter.get("ISBN"));
		book.setYear(parameter.get("Year"));
		book.setLanguage(parameter.get("Language"));
		book.setSize(parameter.get("File size"));
		book.setFormat(parameter.get("File format"));
		book.setPageUrl(parameter.get("PageUrl"));
		try{
			book.setPages(Integer.valueOf(parameter.get("Pages")));
		} catch (NumberFormatException e) {
			System.err.println("parse number error!");
		}
		if(linkMap.get(book.getName()) != null) {
			book.setCoverImgUrl(linkMap.get(book.getName()).get("img"));
		}
		if(linkMap.get("Free") != null) {
			book.setDownloadUrl(linkMap.get("Free").get("a"));
		}
		if(book.getAuthorName() != null && book.getAuthorName().length() > 300) {
			book.setAuthorName(book.getAuthorName().substring(0, 290));
		}
		return book;
	}
	
	private static ClientConnectionManager buildClientConnectionManager() {
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
		PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
		cm.setMaxTotal(50);
		cm.setDefaultMaxPerRoute(20);
		return cm;
	}
	
	private HttpEntity getResponseEntity(String uri) {
		HttpClient client = new DefaultHttpClient(ItEbooksCrawler.cm);
		HttpGet get = new HttpGet(uri);
		HttpResponse response;
		try {
			response = client.execute(get);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return response.getEntity();
	}
	
	private void close(Reader reader) {
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	private void close(Writer writer) {
//		try {
//			writer.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	public void close(InputStream in) {
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void close(OutputStream out) {
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private ContentHandler buildContentHandler(PipedInputStream pipedIn, PipedOutputStream pipedOut) {
		this.linkHandler = new LinkContentHandler();
		BodyContentHandler bodyHandler = new BodyContentHandler(pipedOut);
		TeeContentHandler handler =  new TeeContentHandler(bodyHandler, this.linkHandler);
		return handler;
	}
	
	private class ParserThread extends Thread {
		
		public ParserThread(final InputStream sourceIn, final PipedInputStream targetIn) {
			super(new Runnable(){
				@Override
				public void run() {
					Metadata meta = new Metadata();
					meta.set(Metadata.CONTENT_TYPE, "text/html");
					HtmlParser parser = new HtmlParser();
					PipedOutputStream pipedOut = null;
					try {
						pipedOut = new PipedOutputStream(targetIn);
						parser.parse(sourceIn, ItEbooksCrawler.this.buildContentHandler(targetIn, pipedOut), meta, new ParseContext());
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						close(sourceIn);
						close(pipedOut);
					}
				}
			});
			
		}
		
	}
}
