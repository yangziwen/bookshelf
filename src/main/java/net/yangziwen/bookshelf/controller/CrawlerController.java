package net.yangziwen.bookshelf.controller;

import java.util.concurrent.atomic.AtomicInteger;

import net.yangziwen.bookshelf.crawler.ItEbooksCrawler;
import net.yangziwen.bookshelf.pojo.Book;
import net.yangziwen.bookshelf.service.IBookService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/crawler")
public class CrawlerController {
	
	private Logger logger = Logger.getLogger(getClass());

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
					int n = 0, cnt = 0;
					while((n = atomInt.getAndIncrement()) <= to) {
						long startTime = System.currentTimeMillis();
						Book book = doCrawlBook(n, DEFAULT_MAX_FAILED_TIMES);
						if(book != null){
							books[n-from] = book;
							long finishTime = System.currentTimeMillis();
							logger.info(n + ": " + book.getName() +  " [by " + Thread.currentThread().getName() + " using " + (finishTime - startTime) / 1000.0 +  "s]");
						}
						cnt ++;
					}
					logger.info("### " + Thread.currentThread().getName() + " exit, and crawled " + cnt + " books!");
				}
			}, "crawler-" + i);
			threads[i].start();
		}
		for(int i=0, l=threads.length; i<l; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		logger.info("crawl task finished");
		int i = 0;
		for(Book book: books) {
			if(book == null) {
				continue;
			}
			bookService.saveOrUpdateBook(book);
			i++;
			if(i%50 == 0) {
				logger.info(i + " books are saved!");
			}
		}
		if(i%50 > 0) {
			logger.info(i + " books are saved!");
		}
		logger.info("persistence task finished");
		return "finished";
	}
	
	private Book doCrawlBook(int n, int maxFailedTimes) {
		if(maxFailedTimes <= 0) {
			maxFailedTimes = 1;
		}
		final String pageUrl = "http://it-ebooks.info/book/" + n;
		final ItEbooksCrawler crawler = new ItEbooksCrawler();
		final Book[] bookReceiver = new Book[maxFailedTimes];
		Book book = null;
		int failedTimes = 0;
		while(bookReceiver[failedTimes] == null) {
			if(failedTimes >= maxFailedTimes) {
				break;
			}
			final int currentFailedTimes = failedTimes;
			try {
				Thread crawlerThread = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							bookReceiver[currentFailedTimes] = crawler.crawlPage(pageUrl);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				crawlerThread.start();
				crawlerThread.join(60000);	// 先暂时写成这样，以后再继续研究concurrency
				if(bookReceiver[currentFailedTimes] != null) {
					book = bookReceiver[currentFailedTimes];
					break;
				} else {
					throw new RuntimeException("failed to get a valid book result!");
				}
			} catch (Exception e) {
				logger.error("Thread: [" + Thread.currentThread().getName() +  "] failed " + (++failedTimes) + " times to crawl book [" + pageUrl + "]");
				logger.error(e.getMessage());
			}
		}
		return book;
	}
}
