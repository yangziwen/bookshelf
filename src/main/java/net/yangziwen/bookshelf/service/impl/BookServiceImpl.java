package net.yangziwen.bookshelf.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.yangziwen.bookshelf.crawler.ItEbooksCrawler;
import net.yangziwen.bookshelf.dao.IBookDao;
import net.yangziwen.bookshelf.pojo.Book;
import net.yangziwen.bookshelf.service.IBookService;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class BookServiceImpl implements IBookService {
	
	private Logger logger = Logger.getLogger(getClass());
	
	@Autowired
	private IBookDao bookDao;
	
	@Override
	public void saveOrUpdateBook(Book book) {
		bookDao.saveOrUpdateBook(book);
	}
	
	@Override
	public Book getBookById(Long id) {
		return bookDao.getBookById(id);
	}
	
	@Override
	public Book getBookByBookId(Long bookId) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("bookId", bookId);
		List<Book> list = bookDao.getBookListResult(0, 1, param);
		return CollectionUtils.isEmpty(list)? null: list.get(0);
	}
	
	@Override
	public List<String> getPublisherListResult() {
		return bookDao.getPublisherListResult();
	}
	
	@Override
	public List<String> getYearListResult() {
		return bookDao.getYearListResult();
	}
	
	@Override
	public Map<String, Object> getBookPaginateResult(int start, int limit, Map<String, Object> param) {
		return bookDao.getBookPaginateResult(start, limit, param);
	}
	
	@Override
	public List<String> generateInsertSqlForBookList(int start, int limit, Map<String, Object> param) {
		List<Book> bookList = bookDao.getBookListResult(start, limit, param);
		if(bookList == null || bookList.size() == 0) {
			return Collections.emptyList();
		}
		List<String> sqlList = new ArrayList<String>(bookList.size());
		StringBuilder sqlBuff = null;
		for(Book book: bookList) {
			sqlBuff = new StringBuilder()
				.append("insert into `book` (")
				.append("id, name, publisher, isbn, year, pages, language, size, format, downloadUrl, coverImgUrl, storagePath, authorName, pageUrl ")
				.append(") values (") 
				.append(book.getId()).append(",")
				.append(escapeSqlString(book.getName())).append(",")
				.append(escapeSqlString(book.getPublisher())).append(",")
				.append(escapeSqlString(book.getIsbn())).append(",")
				.append(escapeSqlString(book.getYear())).append(",")
				.append(book.getPages()).append(",")
				.append(escapeSqlString(book.getLanguage())).append(",")
				.append(escapeSqlString(book.getSize())).append(",")
				.append(escapeSqlString(book.getFormat())).append(",")
				.append(escapeSqlString(book.getDownloadUrl())).append(",")
				.append(escapeSqlString(book.getCoverImgUrl())).append(",")
				.append(escapeSqlString(book.getStoragePath())).append(",")
				.append(escapeSqlString(book.getAuthorName())).append(",")
				.append(escapeSqlString(book.getPageUrl()))
				.append(");");
			;
			sqlList.add(sqlBuff.toString());
		}
		return sqlList;
	}
	
	private String escapeSqlString(String value) {
		if(value == null) {
			return "null";
		}
		return "'" + StringEscapeUtils.escapeSql(value) + "'";
	}
	
	@Override
	public void crawlItEbooks(final int from, final int to, int threadNum) {
		final int DEFAULT_MAX_FAILED_TIMES = 5;
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
			saveOrUpdateBook(book);
			i++;
			if(i%50 == 0) {
				logger.info(i + " books are saved!");
			}
		}
		if(i%50 > 0) {
			logger.info(i + " books are saved!");
		}
		logger.info("persistence task finished");
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
