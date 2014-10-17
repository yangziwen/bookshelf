package net.yangziwen.bookshelf.action;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.yangziwen.bookshelf.crawler.ItEbooksCrawler;
import net.yangziwen.bookshelf.pojo.Book;
import net.yangziwen.bookshelf.service.IBookService;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

@ParentPackage("struts-default") 
@Namespace("/book")
@SuppressWarnings("serial")
public class BookAction extends ActionSupport {
	
	@Autowired
	private IBookService bookService;
	
	@Action(value = "/list", results = {@Result(name="list", location="book/list.jsp")})
	public String list() {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("publisher", publisher);
		param.put("authorName", authorName);
		param.put("name", name);
		param.put("year", year);
		param.put("orderBy", " id desc ");
		
		Map<String, Object> pageInfo = bookService.getBookPaginateResult(start, limit, param);
		
		HttpServletRequest request = ServletActionContext.getRequest();
		request.setAttribute("list", pageInfo.get("list"));
		request.setAttribute("totalCount", pageInfo.get("totalCount"));
		request.setAttribute("publisherList", bookService.getPublisherListResult());
		request.setAttribute("yearList", bookService.getYearListResult());
		request.setAttribute("success", Boolean.TRUE);
		return "list";
	}
	
	@Action(value = "/download", results = {
		@Result(name="download", type="stream", params = {
			"inputName", "inputStream",
			"contentType", "${contentType}",
			"contentLength", "${contentLength}",
			"contentDisposition", "${contentDisposition}"
		})
	})
	public String downloadBook() throws Exception {
		HttpServletResponse response = ServletActionContext.getResponse();
		
		HttpClient client = new DefaultHttpClient(ItEbooksCrawler.cm);
		Book book = bookId == 0L? null: bookService.getBookByBookId(bookId);
		
		if(book == null || StringUtils.isBlank(book.getDownloadUrl())) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return NONE;
		}
		
		HttpGet downloadBookRequest = new HttpGet(book.getDownloadUrl());
		downloadBookRequest.addHeader("Referer", book.getPageUrl());
		HttpResponse downloadBookResponse = client.execute(downloadBookRequest);
		
		if(downloadBookResponse.getStatusLine().getStatusCode() >= 400 || downloadBookResponse.getEntity() == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return NONE;
		}
		
		contentType = downloadBookResponse.getFirstHeader("Content-Type").getValue();
		contentLength = downloadBookResponse.getEntity().getContentLength();
		contentDisposition = downloadBookResponse.getFirstHeader("Content-Disposition").getValue();
		inputStream = downloadBookResponse.getEntity().getContent();
		return "download";
	}
	
	@Action(value = "/sql", results = {
		@Result(name="sql", type="stream", params = {"contentType", "text/html"})
	})
	public String getBookUpdateSql() throws IOException {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("minId", Long.valueOf(from));
		List<String> sqlList = bookService.generateInsertSqlForBookList(start, limit, param);
		StringBuilder htmlBuff = new StringBuilder()
			.append("<html><body>");
		for(String sql: sqlList) {
			htmlBuff.append("<span>").append(sql).append("</span><br/>");
		}
		htmlBuff.append("</body></html>");
		inputStream = new ByteArrayInputStream(htmlBuff.toString().getBytes());
		return "sql";
	}
	
	//------- parameters from request ---------//

	private int start = 0;
	private int limit = 20;
	private String publisher = "";
	private String name = "";
	private String authorName = "";
	private String year = "";
	private long from = 0L;
	private long bookId = 0L;
	private InputStream inputStream;
	private long contentLength;
	private String contentType;
	private String contentDisposition;

	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getLimit() {
		return limit;
	}
	public void setLimit(int limit) {
		this.limit = limit;
	}
	public String getPublisher() {
		return publisher;
	}
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAuthorName() {
		return authorName;
	}
	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public long getFrom() {
		return from;
	}
	public void setFrom(long from) {
		this.from = from;
	}
	public long getBookId() {
		return bookId;
	}
	public void setBookId(long bookId) {
		this.bookId = bookId;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public long getContentLength() {
		return contentLength;
	}

	public String getContentType() {
		return contentType;
	}

	public String getContentDisposition() {
		return contentDisposition;
	}
	
}
