package net.yangziwen.bookshelf.action;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import net.yangziwen.bookshelf.service.IBookService;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

@ParentPackage("struts-default") 
@Namespace("/crawler")
@SuppressWarnings("serial")
public class CrawlerAction extends ActionSupport implements ServletResponseAware {
	
	private HttpServletResponse response;

	@Autowired
	private IBookService bookService;
	
	@Action(value = "/crawlItEbooks")
	public void crawlItEbooks() throws Exception {
		if(from <= 0 || to <= 0) {
			writeToResponse("Both 'from' and 'to' should be bigger than 0!");
			return;
		}
		if(to < from) {
			writeToResponse("'to' should be bigger than 'from'");
			return;
		}
		bookService.crawlItEbooks(from, to, threadNum);
		writeToResponse("finished");
	}
	
	private void writeToResponse(String msg) throws Exception {
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer = response.getWriter();
		writer.print(msg);
		writer.flush();
	}
	
	@Override
	public void setServletResponse(HttpServletResponse response) {
		this.response = response;
	}
	
	//------- parameters from request ---------//
	private int from = 0;
	private int to = 0;
	private int threadNum = 5;

	public int getFrom() {
		return from;
	}
	public void setFrom(int from) {
		this.from = from;
	}
	public int getTo() {
		return to;
	}
	public void setTo(int to) {
		this.to = to;
	}
	public int getThreadNum() {
		return threadNum;
	}
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}

	
}
