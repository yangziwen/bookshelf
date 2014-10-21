package net.yangziwen.bookshelf.dao;

import net.yangziwen.bookshelf.pojo.CronJob;

public interface ICronJobDao {

	void saveOrUpdate(CronJob job);

	CronJob getById(Long id);

	CronJob getByType(String type);

}
