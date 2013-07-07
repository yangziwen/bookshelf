package net.yangziwen.bookshelf.service;

import java.util.Map;

import net.yangziwen.bookshelf.pojo.Book;

public interface IBookService {

	void saveOrUpdateBook(Book book);

	Map<String, Object> getBookPaginateResult(int start, int limit, Map<String, Object> param);

}
