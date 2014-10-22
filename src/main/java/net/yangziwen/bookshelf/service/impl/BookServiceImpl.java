package net.yangziwen.bookshelf.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	private Logger logger = Logger.getLogger(this.getClass());
	
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
	
	@Override	// 理论上这种耗时的操作不应该放在事务里，不过反正只是随便写写，无所谓了
	public void crawlNewBooks(int limit) {
		if(limit <= 0) {
			return;
		}
		Book latestBook = getLatestBook();
		Long latestBookId = latestBook != null? latestBook.getBookId(): 0L;
		if(!hasNewBookToCrawl(latestBookId, 3, 2)) {
			logger.info("There is no new book!");
			return;
		}
		Long newBookId = latestBookId + 1;
		ItEbooksCrawler crawler = new ItEbooksCrawler();
		for(int i=0; i<limit; i++) {
			try {
				Book book = crawler.crawlPage(newBookId + i);
				if(book == null) {
					break;
				}
				saveOrUpdateBook(book);
				logger.info(String.format("crawled book {bookId: '%d', name: '%s'}", book.getBookId(), book.getName()));
			} catch (Exception e) {
				logger.warn(e.getCause());
			}
		}
	}
	
	public boolean hasNewBookToCrawl(Long latestBookId, int tryTimes, int step) {
		ItEbooksCrawler crawler = new ItEbooksCrawler();
		try {
			Book newBook = crawler.crawlPage(latestBookId + 1);
			boolean result = newBook != null;
			if(!result && tryTimes > 1) {
				return hasNewBookToCrawl(latestBookId + step, tryTimes - 1, step);
			}
			return result;
		} catch (Exception e) {
			logger.warn(e.getCause());
			if(tryTimes > 1) {
				return hasNewBookToCrawl(latestBookId + step, tryTimes - 1, step);
			}
			return false;
		}
	}
	
	private Book getLatestBook() {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("orderBy", " bookId desc ");
		List<Book> list = bookDao.getBookListResult(0, 1, param);
		return list.size() > 0? list.get(0): null;
	}

}
