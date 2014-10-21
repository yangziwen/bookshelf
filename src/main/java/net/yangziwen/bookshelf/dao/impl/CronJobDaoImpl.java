package net.yangziwen.bookshelf.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.yangziwen.bookshelf.dao.IBasicHibernateDao;
import net.yangziwen.bookshelf.dao.ICronJobDao;
import net.yangziwen.bookshelf.pojo.CronJob;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CronJobDaoImpl implements ICronJobDao {
	
	@Autowired
	private IBasicHibernateDao basicHibernateDao;
	
	@Override
	public void saveOrUpdate(CronJob job) {
		basicHibernateDao.saveOrUpdate(job);
	}
	
	@Override
	public CronJob getById(Long id) {
		return basicHibernateDao.getById(CronJob.class, id);
	}
	
	@Override
	public CronJob getByType(String type) {
		String hql = "from CronJob where type = :type";
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("type", type);
		List<CronJob> list = basicHibernateDao.getListResult(0, 0, hql, param);
		return CollectionUtils.isNotEmpty(list)? list.get(0): null;
	}

}
