package net.yangziwen.bookshelf.dao;

import java.util.List;
import java.util.Map;

import org.hibernate.Session;

public interface IBasicHibernateDao {

	public abstract Long convertToLong(String key, Map<String, Object> param, boolean override);

	public abstract Long convertToLong(String key, Map<String, Object> param);

	public abstract List<String> getPlaceHolderList(String sql);

	public abstract Map<String, Object> getPaginateResult(int start, int limit, String hql, Map<String, Object> param);

	public abstract int getTotalCount(String hql, Map<String, Object> param, boolean needConvert);

	public abstract int getTotalCount(String hql, Map<String, Object> param);

	public abstract <T> List<T> getListResult(int start, int limit, String hql, Map<String, Object> param);

	public abstract <T> T getById(Class<T> clazz, Long id);

	public abstract <T> void delete(T entity);

	public abstract <T> void saveOrUpdate(T entity);

	public abstract Session getCurrentSession();

}
