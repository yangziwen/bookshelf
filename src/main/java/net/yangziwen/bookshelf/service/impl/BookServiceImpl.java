package net.yangziwen.bookshelf.service.impl;

import java.util.Map;

import net.yangziwen.bookshelf.dao.IBookDao;
import net.yangziwen.bookshelf.pojo.Book;
import net.yangziwen.bookshelf.service.IBookService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl implements IBookService {
	
	@Autowired
	private IBookDao bookDao;
	
	@Override
	public void saveOrUpdateBook(Book book) {
		bookDao.saveOrUpdateBook(book);
	}
	
	@Override
	public Map<String, Object> getBookPaginateResult(int start, int limit, Map<String, Object> param) {
		return bookDao.getBookPaginateResult(start, limit, param);
	}

}
