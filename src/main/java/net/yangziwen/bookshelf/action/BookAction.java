package net.yangziwen.bookshelf.action;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.yangziwen.bookshelf.crawler.ItEbooksCrawler;
import net.yangziwen.bookshelf.pojo.Book;
import net.yangziwen.bookshelf.service.IBookService;

import org.apache.commons.io.IOUtils;
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
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

@ParentPackage("struts-default") 
@Namespace("/book")
@Results({
	@Result(name="list", location="book/list.jsp")
})
@SuppressWarnings("serial")
public class BookAction extends ActionSupport {
	
	@Autowired
	private IBookService bookService;
	
	@Action("/list")
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
	
	@Action("/download")
	public void downloadBook() throws Exception {
		HttpServletResponse response = ServletActionContext.getResponse();
		
		HttpClient client = new DefaultHttpClient(ItEbooksCrawler.cm);
		Book book = bookId == 0L? null: bookService.getBookByBookId(bookId);
		
		if(book == null || StringUtils.isBlank(book.getDownloadUrl())) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		HttpGet downloadBookRequest = new HttpGet(book.getDownloadUrl());
		downloadBookRequest.addHeader("Referer", book.getPageUrl());
		HttpResponse downloadBookResponse = client.execute(downloadBookRequest);
		
		response.setContentLength(Long.valueOf(downloadBookResponse.getEntity().getContentLength()).intValue());
		response.setContentType(downloadBookResponse.getFirstHeader("Content-Type").getValue());
		response.setHeader("Content-Disposition", downloadBookResponse.getFirstHeader("Content-Disposition").getValue());
		response.flushBuffer();
		
		InputStream in = null;
		OutputStream out = null;
		try {
			in = downloadBookResponse.getEntity().getContent();
			out = response.getOutputStream();
			IOUtils.copy(in, out);
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
	}
	
	@Action("/sql")
	public void getBookUpdateSql() throws IOException {
		HttpServletResponse response = ServletActionContext.getResponse();
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("minId", Long.valueOf(from));
		List<String> sqlList = bookService.generateInsertSqlForBookList(start, limit, param);
		StringBuilder htmlBuff = new StringBuilder()
			.append("<html><body>");
		for(String sql: sqlList) {
			htmlBuff.append("<span>").append(sql).append("</span><br/>");
		}
		htmlBuff.append("</body></html>");
		PrintWriter writer = response.getWriter();
		writer.print(htmlBuff.toString());
		writer.flush();
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
	
}
