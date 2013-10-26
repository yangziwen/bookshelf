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
	
	private static final int DEFAULT_MAX_FAILED_TIMES = 10;
	
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
		final AtomicInteger atomInt = new AtomicInteger(from);
		Thread[] threads = new Thread[threadNum];
		final Book[] books = new Book[to-from+1];
		for(int i=0; i<threadNum; i++) {
			threads[i] = new Thread(new Runnable(){
				@Override
				public void run() {
					int n = 0;
					while((n = atomInt.getAndIncrement()) <= to) {
						Book book = doCrawlBook(n, DEFAULT_MAX_FAILED_TIMES);
						if(book != null){
							System.out.println(n + ": " + book.getName());
							books[n-from] = book;
						}
					}
				}
			}, "crawler " + i);
			threads[i].start();
		}
		for(int i=0, l=threads.length; i<l; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("crawl task finished");
		int i = 0;
		for(Book book: books) {
			if(book == null) {
				continue;
			}
			bookService.saveOrUpdateBook(book);
			i++;
			if(i%50 == 0) {
				System.out.println(i + " books are saved!");
			}
		}
		if(i%50 > 0) {
			System.out.println(i + " books are saved!");
		}
		System.out.println("finished");
		return "finished";
	}
	
	private Book doCrawlBook(int n, int maxFailedTimes) {
		if(maxFailedTimes <= 0) {
			maxFailedTimes = 1;
		}
		String pageUrl = "http://it-ebooks.info/book/" + n;
		ItEbooksCrawler crawler = new ItEbooksCrawler();
		Book book = null;
		int failedTimes = 0;
		while(book == null) {
			if(failedTimes >= maxFailedTimes) {
				break;
			}
			try {
				book = crawler.crawlPage(pageUrl);
				if(book != null) {
					break;
				} else {
					throw new RuntimeException("failed to get a valid book result!");
				}
			} catch (Exception e) {
				System.err.println("Thread: [" + Thread.currentThread().getName() +  "] failed " + (++failedTimes) + " times to crawl book [" + pageUrl + "]");
				System.err.println(e.getMessage());
			}
		}
		return book;
	}
}
