package net.yangziwen.bookshelf.service.impl;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import net.yangziwen.bookshelf.dao.ICronJobDao;
import net.yangziwen.bookshelf.pojo.CronJob;
import net.yangziwen.bookshelf.service.ICronJobService;

import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

@Service
public class CronJobServiceImpl implements ICronJobService {
	
	private static final ConcurrentHashMap<Long, ScheduledFuture<?>> cronJobMap = new ConcurrentHashMap<Long, ScheduledFuture<?>>();
	
	@Autowired
	private TaskScheduler taskScheduler;

	@Autowired
	private ICronJobDao cronJobDao;
	
	@Override
	public void saveOrUpdateCronJob(CronJob job) {
		cronJobDao.saveOrUpdate(job);
		ScheduledFuture<?> scheduledFuture = cronJobMap.remove(job.getId());
		if(scheduledFuture != null) {
			scheduledFuture.cancel(false);
		}
		if(BooleanUtils.isFalse(job.getEnabled())) {
			return;
		}
		scheduledFuture = taskScheduler.schedule(new CrawlBookRunnableJob(), job.createTrigger());
		if(cronJobMap.putIfAbsent(job.getId(), scheduledFuture) != null) {
			scheduledFuture.cancel(true);
			throw new IllegalStateException(String.format("Failed to update cronJob [%d]", job.getId()));
		}
	}

	@Override
	public CronJob getCronJobByType(String type) {
		return cronJobDao.getByType(type);
	}
	
	class CrawlBookRunnableJob implements Runnable {

		@Override
		public void run() {
			System.out.println("crawl books at " + new Date());
		}
		
	}
	
}
