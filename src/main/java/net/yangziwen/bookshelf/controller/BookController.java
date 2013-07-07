package net.yangziwen.bookshelf.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.yangziwen.bookshelf.pojo.Book;
import net.yangziwen.bookshelf.service.IBookService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/book")
public class BookController {

	@Autowired
	private IBookService bookService;
	
	@RequestMapping("/list.do")
	public String list(Model model,
			@RequestParam(value="start", required=false, defaultValue="0")
			Integer start,
			@RequestParam(value="limit", required=false, defaultValue="10")
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
}
