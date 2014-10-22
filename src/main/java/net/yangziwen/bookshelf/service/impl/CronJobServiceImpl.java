package net.yangziwen.bookshelf.service.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import net.yangziwen.bookshelf.dao.ICronJobDao;
import net.yangziwen.bookshelf.pojo.CronJob;
import net.yangziwen.bookshelf.service.IBookService;
import net.yangziwen.bookshelf.service.ICronJobService;
import net.yangziwen.bookshelf.util.SpringUtil;

import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

@Service
public class CronJobServiceImpl implements ICronJobService {
	
	private static final ConcurrentHashMap<Long, ScheduledFuture<?>> cronJobScheduledFutureMap = new ConcurrentHashMap<Long, ScheduledFuture<?>>();
	
	@Autowired
	private TaskScheduler taskScheduler;

	@Autowired
	private ICronJobDao cronJobDao;
	
	@Override
	public void saveOrUpdateCronJob(CronJob job) {
		cronJobDao.saveOrUpdate(job);
		ScheduledFuture<?> scheduledFuture = cronJobScheduledFutureMap.remove(job.getId());
		if(scheduledFuture != null) {
			scheduledFuture.cancel(false);
		}
		if(BooleanUtils.isFalse(job.getEnabled())) {
			return;
		}
		scheduledFuture = taskScheduler.schedule(new CrawlBookRunnableJob(), job.createTrigger());
		if(cronJobScheduledFutureMap.putIfAbsent(job.getId(), scheduledFuture) != null) {
			scheduledFuture.cancel(true);
			throw new IllegalStateException(String.format("Failed to update cronJob [%d]", job.getId()));
		}
	}
	
	@Override
	public CronJob getCronJobByType(String type) {
		return cronJobDao.getByType(type);
	}
	
	class CrawlBookRunnableJob implements Runnable {
		private static final int LIMIT = 20;
		@Override
		public void run() {
			SpringUtil.getBean(IBookService.class).crawlNewBooks(LIMIT);
		}
		
	}
	
}
