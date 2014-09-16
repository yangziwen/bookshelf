package net.yangziwen.bookshelf.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.yangziwen.bookshelf.crawler.ItEbooksCrawler;
import net.yangziwen.bookshelf.pojo.Book;
import net.yangziwen.bookshelf.service.IBookService;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/book")
public class BookController {

	@Autowired
	private IBookService bookService;
	
	/**
	 * @see net.yangziwen.bookshelf.dao.impl.BookDaoImpl#getBookPaginateResult(int, int, Map)
	 */
	@RequestMapping("/list.do")
	public String list(Model model,
			@RequestParam(value="start", required=false, defaultValue="0")
			Integer start,
			@RequestParam(value="limit", required=false, defaultValue="20")
			Integer limit,
			@RequestParam(value="publisher", required=false)
			String publisher,
			@RequestParam(value="name", required=false)
			String name,
			@RequestParam(value="authorName", required=false)
			String authorName,
			@RequestParam(value="year", required=false)
			String year
		) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("publisher", publisher);
		param.put("authorName", authorName);
		param.put("name", name);
		param.put("year", year);
		param.put("orderBy", " id desc ");
		model.addAllAttributes(param);
		model.addAttribute("start", start);
		model.addAttribute("limit", limit);
		model.addAllAttributes(bookService.getBookPaginateResult(start, limit, param));
		model.addAttribute("success", Boolean.TRUE);
		return "book/list";
	}
	
	/**
	 * 下载电子书
	 */
	@RequestMapping("/download.do")
	public void downloadBook(@RequestParam String pageUrl, HttpServletResponse response) throws ClientProtocolException, IOException {
		HttpClient client = new DefaultHttpClient(ItEbooksCrawler.cm);
		HttpGet pageRequest = new HttpGet(pageUrl);
		Book book = client.execute(pageRequest, new ItEbooksCrawler.ItEbookResponseHandler(pageUrl));
		
		if(StringUtils.isBlank(book.getDownloadUrl())) {
			return;
		}
		
		HttpGet downloadBookRequest = new HttpGet(book.getDownloadUrl());
		downloadBookRequest.addHeader("Referer", pageUrl);
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
	
	/**
	 * 添加一个生成更新数据的sql的接口
	 */
	@ResponseBody
	@RequestMapping("/getBookUpdateSql.do")
	public String getBookUpdateSql(
			@RequestParam(value="from", defaultValue="0")
			Integer from,
			@RequestParam(value="start", defaultValue="0")
			Integer start,
			@RequestParam(value="limit", defaultValue="50")
			Integer limit) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("minId", Long.valueOf(from));
		List<String> sqlList = bookService.generateInsertSqlForBookList(start, limit, param);
		StringBuilder htmlBuff = new StringBuilder()
			.append("<html><body>");
		for(String sql: sqlList) {
			htmlBuff.append("<span>").append(sql).append("</span><br/>");
		}
		htmlBuff.append("</body></html>");
		return htmlBuff.toString();
	}
}
