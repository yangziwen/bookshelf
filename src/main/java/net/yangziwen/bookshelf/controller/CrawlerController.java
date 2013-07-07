package net.yangziwen.bookshelf.controller;

import java.util.concurrent.atomic.AtomicInteger;

import net.yangziwen.bookshelf.crawler.ItEbooksCrawler;
import net.yangziwen.bookshelf.pojo.Book;
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
			@RequestParam(value="from", defaultValue="1") final Integer from,
			@RequestParam(value="to", defaultValue="10") final Integer to) {
		if(to<from) {
			return "'to' should be bigger than 'from'!";
		}
		final AtomicInteger atomInt = new AtomicInteger(from);
		int threadNum = 5;
		Thread[] threads = new Thread[threadNum];
		final Book[] books = new Book[to-from+1];
		for(int i=0; i<threadNum; i++) {
			threads[i] = new Thread(new Runnable(){
				@Override
				public void run() {
					int n = 0;
					while((n = atomInt.getAndIncrement()) <= to) {
						String pageUrl = "http://it-ebooks.info/book/" + n;
						try {
							Book book = new ItEbooksCrawler().crawlPage(pageUrl);
							if(book != null){
								System.out.println(n + ": " + book.getName());
								books[n-from] = book;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			});
			threads[i].start();
		}
		for(int i=0, l=threads.length; i<l; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		for(Book book: books) {
			if(book == null) {
				continue;
			}
			bookService.saveOrUpdateBook(book);
		}
		return "finished";
	}
}
