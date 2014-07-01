package net.yangziwen.bookshelf.dao.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.yangziwen.bookshelf.dao.IBasicHibernateDao;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BasicHibernateDaoImpl implements IBasicHibernateDao {

	@Autowired
	public SessionFactory sessionFactory;

	public static final String ORDER_ASC = "asc";
	public static final String ORDER_DESC = "desc";
	
	public static final String PAGE_LIST = "list";
	public static final String PAGE_TOTAL_COUNT = "totalCount";
	
	@Override
	public Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}
	
	@Override
	public <T> void saveOrUpdate(T entity){
		getCurrentSession().saveOrUpdate(entity);
	}
	
	@Override
	public <T> void delete(T entity) {
		getCurrentSession().delete(entity);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getById(Class<T> clazz, Long id) {
		return (T)getCurrentSession().get(clazz, id);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> getListResult(int start, int limit, String hql, Map<String, Object> param) {
		Session session = getCurrentSession();
		Query query = session.createQuery(hql);
		for(String key: getPlaceHolderList(hql)) {
			query.setParameter(key, param.get(key));
		}
		if(limit != 0) {
			query.setFirstResult(start);
			query.setMaxResults(limit);
		}
		return query.list();
	}
	
	@Override
	public int getTotalCount(String hql, Map<String, Object> param) {
		return getTotalCount(hql, param, true);
	}
	
	/**
	 * 
	 * @param hql
	 * @param param
	 * @param needConvert: true表明hql本身不是一个取count的hql，不需要做select count(*)的替换
	 * @return
	 */
	@Override
	public int getTotalCount(String hql, Map<String, Object> param, boolean needConvert) {
		if(param == null) {
			param = Collections.<String,Object>emptyMap();
		}
		Session session = getCurrentSession();
		if(needConvert) {
			int beginPos = hql.indexOf("from");
			int endPos = hql.indexOf("order by");
			if(endPos == -1) {
				endPos = hql.length();
			}
			hql = "select count(*) " + hql.substring(beginPos, endPos);
		}
		Query query = session.createQuery(hql);
		for (String key : getPlaceHolderList(hql)) {
			query.setParameter(key, param.get(key));
		}
		List<?> list = query.list();
		if(list.isEmpty()) {
			return 0;
		}
		if(hql.indexOf("group by") != -1) {
			return list.size();
		} else {
			return new Integer(list.get(0).toString());
		}
	}
	
	@Override
	public Map<String, Object> getPaginateResult(int start, int limit, String hql, Map<String, Object> param) {
		Map<String, Object> resultMap = new HashMap<String, Object>(5);
		resultMap.put(PAGE_TOTAL_COUNT, getTotalCount(hql, param));
		resultMap.put(PAGE_LIST, getListResult(start, limit, hql, param));
		return resultMap;
	}
	
	/**
	 * 获取sql/hql中的所有占位符(形如":XXX")
	 * @author zyang
	 */
	@Override
	public List<String> getPlaceHolderList(String sql){
		if(sql.indexOf(":") == -1){
			return Collections.<String>emptyList();
		}
		// 剔除sql中的字符串
		StringBuilder clearedSqlBuff = new StringBuilder();
		int idx1 = 0, idx2 = 0, idx = 0, minIdx = 0;
		int sqlLen = sql.length();
		while(idx < sqlLen){
			idx1 = sql.indexOf("'", idx);
			idx2 = sql.indexOf("\"", idx);
			if(idx1 == -1){
				idx1 = sql.length();
			}
			if(idx2 == -1){
				idx2 = sql.length();
			}
			minIdx = Math.min(idx1, idx2);
			clearedSqlBuff.append(sql.substring(idx, minIdx));
			if(minIdx == sqlLen){
				break;
			}
			idx = (idx1 < idx2? sql.indexOf("'", idx1 + 1): sql.indexOf("\"", idx2 + 1)) + 1;
		}
		String clearedSql = clearedSqlBuff.toString();
		// 按前缀":"查找sql中的占位符
		List<String> placeHolderList = new LinkedList<String>();
		String prefix = ":";
		int clearedSqlLen = clearedSql.length();
		int prefixLen = prefix.length();
		int beginIdx = clearedSql.indexOf(prefix), 
			endIdx;
		if(beginIdx == -1) {
			return Collections.<String>emptyList();
		}
		while(beginIdx != -1){
			beginIdx += prefixLen;	// 占位符的第一个字母
			endIdx = beginIdx;
			char c;
			while(endIdx < clearedSqlLen){
				c = clearedSql.charAt(endIdx);
				if(!Character.isLetter(c) && c != '_' && (c < '0' || c > '9')){
					break;
				}
				endIdx ++;
			}
			if(endIdx <= beginIdx + 1){
				continue;
			}
			placeHolderList.add(clearedSql.substring(beginIdx, endIdx));
			beginIdx = clearedSql.indexOf(prefix, endIdx);
		}
		return placeHolderList;
	}
	
	/**
	 * param中的value转Long的工具方法，默认会覆盖param中的value
	 */
	@Override
	public Long convertToLong(String key, Map<String, Object> param){
		return convertToLong(key, param, true);
	}
	
	/**
	 * param中的value转Long的工具方法
	 */
	@Override
	public Long convertToLong(String key, Map<String, Object> param, boolean override){
		Object obj = null;
		if(StringUtils.isEmpty(key) || (obj = param.get(key)) == null){
			return null;
		}
		Long num = null;
		if(obj instanceof Long){
			num = (Long) obj;
		} else {
			try {
				num = Long.valueOf(obj.toString());
				if(override){
					param.put(key, num);
				}
			} catch (NumberFormatException e) {
			}
		}
		return num;
	}
}
