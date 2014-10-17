package net.yangziwen.bookshelf.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.yangziwen.bookshelf.service.IBookService;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

@ParentPackage("json") 
@Namespace("/ajax")
@SuppressWarnings("serial")
public class AjaxAction extends ActionSupport {
	
	@Autowired
	private IBookService bookService;
	
	@Action(value = "books", results = @Result(name="books", type="json", params={"root", "bookInfo"}))
	public String getBookList() {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("publisher", publisher);
		param.put("authorName", authorName);
		param.put("name", name);
		param.put("year", year);
		param.put("orderBy", " id desc ");
		bookInfo = bookService.getBookPaginateResult(start, limit, param);
		return "books";
	}
	
	@Action(value = "sqls", results = @Result(name="sqls", type="json", params={"root", "sqls"}))
	public String getSqlList() {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("minId", Long.valueOf(from));
		sqls = bookService.generateInsertSqlForBookList(start, limit, param);
		return "sqls";
	}

	//------ params ------//
	private Map<String, Object> bookInfo;
	private List<String> sqls;
	private int start = 0;
	private int limit = 20;
	private String publisher = "";
	private String name = "";
	private String authorName = "";
	private String year = "";
	private long from = 0L;

	public Map<String, Object> getBookInfo() {
		return bookInfo;
	}

	public List<String> getSqls() {
		return sqls;
	}

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
	

}
