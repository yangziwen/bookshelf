package net.yangziwen.bookshelf.dao.impl;

import java.util.List;
import java.util.Map;

import net.yangziwen.bookshelf.dao.IBasicHibernateDao;
import net.yangziwen.bookshelf.dao.IBookDao;
import net.yangziwen.bookshelf.pojo.Book;

import org.apache.commons.lang.StringUtils;
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
		StringBuilder hqlBuff = new StringBuilder("from Book where name is not null ");
		String publisher = (String) param.get("publisher");
		if(!StringUtils.isEmpty(publisher)) {
			hqlBuff.append(" and publisher = :publisher ");
		}
		String authorName = (String) param.get("authorName");
		if(!StringUtils.isEmpty(authorName)) {
			hqlBuff.append(" and authorName like :authorName ");
			param.put("authorName", "%" + authorName + "%");
		}
		String name = (String) param.get("name");
		if(!StringUtils.isEmpty(name)) {
			hqlBuff.append(" and name like :name ");
			param.put("name", "%" + name + "%");
		}
		String year = (String) param.get("year");
		if(!StringUtils.isEmpty(year)) {
			hqlBuff.append(" and year = :year ");
		}
		Long minId = (Long) param.get("minId");
		if(minId != null && minId > 0) {
			hqlBuff.append(" and id >= :minId");
		}
		String orderBy = (String) param.get("orderBy");
		if(StringUtils.isNotBlank(orderBy)) {
			hqlBuff.append(" order by " + orderBy);
		}
		return hqlBuff.toString();
	}

}
 