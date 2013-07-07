package net.yangziwen.bookshelf.controller;

import java.util.HashMap;
import java.util.Map;

import net.yangziwen.bookshelf.service.IBookService;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.Link;
import org.apache.tika.sax.LinkContentHandler;
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
		model.addAllAttributes(param);
		model.addAttribute("start", start);
		model.addAttribute("limit", limit);
		model.addAllAttributes(bookService.getBookPaginateResult(start, limit, param));
		model.addAttribute("success", Boolean.TRUE);
		return "book/list";
	}
	
	@ResponseBody
	@RequestMapping("/getBookDownloadLink.do")
	public String getBookDownloadLink(@RequestParam(value="pageUrl", required=true)String pageUrl) {
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(pageUrl);
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			if(entity == null) {
				return "";
			}
			Parser parser = new HtmlParser();
			LinkContentHandler linkHandler = new LinkContentHandler();
			Metadata meta = new Metadata();
			meta.set(Metadata.CONTENT_TYPE, "text/html");
			parser.parse(entity.getContent(), linkHandler, meta, new ParseContext());
			String url = "";
			for(Link link: linkHandler.getLinks()) {
				if("Free".equals(link.getText())) {
					url = "http://it-ebooks.info/" + link.getUri();
				}
			}
			return url;
		} catch (Exception e) {
			return "";
		}
	}
}
