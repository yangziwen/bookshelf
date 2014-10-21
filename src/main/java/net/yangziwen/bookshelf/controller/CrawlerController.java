package net.yangziwen.bookshelf.controller;

import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.yangziwen.bookshelf.crawler.ItEbooksCrawler;
import net.yangziwen.bookshelf.pojo.Book;
import net.yangziwen.bookshelf.pojo.CronJob;
import net.yangziwen.bookshelf.service.IBookService;
import net.yangziwen.bookshelf.service.ICronJobService;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/crawler")
public class CrawlerController {
	
	private Logger logger = Logger.getLogger(getClass());

	private static final int DEFAULT_MAX_FAILED_TIMES = 5;
	
	@Autowired
	private IBookService bookService;
	@Autowired
	private ICronJobService cronJobService;
	
	@RequestMapping(value = "/editJob.do", method = RequestMethod.GET)
	public String editJob(Model model) {
		model.addAttribute("cronJob", cronJobService.getCronJobByType(CronJob.TYPE_ITEBOOKS));
		return "cronJob/edit";
	}
	
	@ResponseBody
	@RequestMapping(value="/editJob.do", method = RequestMethod.POST)
	public Map<String, Object> editJob(@ModelAttribute("cronJob") CronJob crontJob) {
		ModelMap resultMap = new ModelMap();
		if(!validateCron(crontJob.getCron())) {
			resultMap.addAttribute("success", false).addAttribute("message", "请输入有效的cron表达式!");
			return resultMap;
		}
		crontJob.setType(CronJob.TYPE_ITEBOOKS);
		crontJob.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		cronJobService.saveOrUpdateCronJob(crontJob);
		return resultMap.addAttribute("success", true).addAttribute("message", "保存成功!");
	}
	
	private boolean validateCron(String cronExpression) {
		if(StringUtils.isBlank(cronExpression)) {
			return false;
		}
		try {
			new CronTrigger(cronExpression);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

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
		final Long bookId = Long.valueOf(n);
		final ItEbooksCrawler crawler = new ItEbooksCrawler();
		final Book[] bookReceiver = new Book[maxFailedTimes];
		Book book = null;
		int failedTimes = 0;
		while(bookReceiver[failedTimes] == null) {
			final int currentFailedTimes = failedTimes;
			try {
				Thread crawlerThread = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							bookReceiver[currentFailedTimes] = crawler.crawlPage(bookId);
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
			if(failedTimes >= maxFailedTimes) {
				break;
			}
		}
		return book;
	}
	
	
}
