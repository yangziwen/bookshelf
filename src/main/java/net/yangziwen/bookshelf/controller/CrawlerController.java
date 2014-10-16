package net.yangziwen.bookshelf.controller;

import net.yangziwen.bookshelf.service.IBookService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/crawler")
public class CrawlerController {
	
	@Autowired
	private IBookService bookService;

	@ResponseBody
	@RequestMapping("/crawlItEbooks.do")
	public String crawlItEbooks(Model model,
			@RequestParam(value="from", required=true) final Integer from,
			@RequestParam(value="to", required=true) final Integer to,
			@RequestParam(value="threadNum", defaultValue="5", required=false) Integer threadNum) {
		if(to<from) {
			return "'to' should be bigger than 'from'!";
		}
		bookService.crawlItEbooks(from, to, threadNum);
		return "finished";
	}
	
}
