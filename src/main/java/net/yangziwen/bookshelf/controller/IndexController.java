package net.yangziwen.bookshelf.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class IndexController {

	@RequestMapping({"/", "/index.do"})
	public String index(){
		return "redirect:/book/list.do";
	}
}
