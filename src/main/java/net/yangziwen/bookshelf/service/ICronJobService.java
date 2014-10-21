package net.yangziwen.bookshelf.service;

import net.yangziwen.bookshelf.pojo.CronJob;

public interface ICronJobService {

	void saveOrUpdateCronJob(CronJob job);

	CronJob getCronJobByType(String type);

}
