package net.yangziwen.bookshelf.crawler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.yangziwen.bookshelf.pojo.Book;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class ItEbooksCrawler {
	
	private static ClientConnectionManager cm = buildClientConnectionManager();
	
	public Book crawlPage(final String pageUrl) throws Exception {
		if(StringUtils.isBlank(pageUrl)) {
			return null;
		}
		HttpClient client = new DefaultHttpClient(ItEbooksCrawler.cm);
		return client.execute(new HttpGet(pageUrl), new ResponseHandler<Book>() {
			@Override
			public Book handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
				if(response.getStatusLine().getStatusCode() >= 400) {
					return null;
				}
				String pageContent = EntityUtils.toString(response.getEntity(), "UTF-8");
				Document doc = Jsoup.parse(pageContent);
				Element viewTbl = doc.select("table.ebook_view").first();
				Element coverImg = viewTbl.select("img").first();
				Element detailTbl = viewTbl.select("table").first();
				Map<String, String> params = new HashMap<String, String>();
				for(Element tr: detailTbl.select("tr")) {
					Elements tds = tr.children();
					if(tds.size() < 2) {
						continue;
					}
					String key = tds.get(0).text().replace(":", "");
					String value = "By".equals(key)? tds.get(1).select("a").first().text() :tds.get(1).text();
					params.put(key, value);
				}
				params.put("Cover", coverImg.attr("src"));
				params.put("PageUrl", pageUrl);
				return createNewBook(params);
			}
		});
	}
	
	private static Book createNewBook(Map<String, String> params) {
		Book book = new Book();
		book.setName(params.get("Buy"));
		book.setAuthorName(params.get("By"));
		book.setPublisher(params.get("Publisher"));
		book.setIsbn(params.get("ISBN"));
		book.setYear(params.get("Year"));
		book.setLanguage(params.get("Language"));
		book.setSize(params.get("File size"));
		book.setFormat(params.get("File format"));
		book.setPageUrl(params.get("PageUrl"));
		book.setPages(NumberUtils.toInt(params.get("Pages")));
		book.setCoverImgUrl(params.get("Cover"));
		book.setPageUrl(params.get("PageUrl"));
		if(book.getAuthorName() != null && book.getAuthorName().length() > 300) {
			book.setAuthorName(book.getAuthorName().substring(0, 300));
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
	
}
