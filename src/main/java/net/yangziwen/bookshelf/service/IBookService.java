package net.yangziwen.bookshelf.service;

import java.util.List;
import java.util.Map;

import net.yangziwen.bookshelf.pojo.Book;

public interface IBookService {

	void saveOrUpdateBook(Book book);

	Map<String, Object> getBookPaginateResult(int start, int limit, Map<String, Object> param);

	List<String> generateInsertSqlForBookList(int start, int limit, Map<String, Object> param);

	Book getBookById(Long id);

	Book getBookByBookId(Long bookId);

	List<String> getPublisherListResult();

	List<String> getYearListResult();

	void crawlNewBooks(int limit);

}
