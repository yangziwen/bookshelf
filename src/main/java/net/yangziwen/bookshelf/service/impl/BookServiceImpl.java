package net.yangziwen.bookshelf.service.impl;

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

}
