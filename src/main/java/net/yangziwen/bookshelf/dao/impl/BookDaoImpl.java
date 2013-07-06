package net.yangziwen.bookshelf.dao.impl;

import java.util.List;
import java.util.Map;

import net.yangziwen.bookshelf.dao.IBasicHibernateDao;
import net.yangziwen.bookshelf.dao.IBookDao;
import net.yangziwen.bookshelf.pojo.Book;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BookDaoImpl implements IBookDao {
	
	@Autowired
	private IBasicHibernateDao basicHibernateDao;
	
	@Override
	public void saveOrUpdateBook(Book book) {
		basicHibernateDao.saveOrUpdate(book);
	}
	
	@Override
	public void deleteBook(Book book) {
		basicHibernateDao.delete(book);
	}
	
	@Override
	public Book getBookById(Long id) {
		return basicHibernateDao.getById(Book.class, id);
	}
	
	@Override
	public List<Book> getBookListResult(int start, int limit, Map<String, Object> param) {
		return basicHibernateDao.<Book>getListResult(start, limit, generateHqlByParam(param), param);
	}
	
	@Override
	public Map<String, Object> getBookPaginateResult(int start, int limit, Map<String, Object> param) {
		return basicHibernateDao.getPaginateResult(start, limit, generateHqlByParam(param), param);
	}
	
	private String generateHqlByParam(Map<String, Object> param) {
		StringBuilder hqlBuff = new StringBuilder("from Book where 1 = 1 ");
		return hqlBuff.toString();
	}

}
