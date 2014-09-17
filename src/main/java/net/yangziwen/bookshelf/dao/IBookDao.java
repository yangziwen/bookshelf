package net.yangziwen.bookshelf.dao;

import java.util.List;
import java.util.Map;

import net.yangziwen.bookshelf.pojo.Book;

public interface IBookDao {

	Map<String, Object> getBookPaginateResult(int start, int limit, Map<String, Object> param);

	List<Book> getBookListResult(int start, int limit, Map<String, Object> param);

	Book getBookById(Long id);

	void deleteBook(Book book);

	void saveOrUpdateBook(Book book);

	List<String> getPublisherListResult();

	List<String> getYearListResult();

}
