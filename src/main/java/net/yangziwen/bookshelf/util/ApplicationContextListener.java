package net.yangziwen.bookshelf.util;

import net.yangziwen.bookshelf.pojo.CronJob;
import net.yangziwen.bookshelf.service.ICronJobService;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextListener implements ApplicationListener<ContextRefreshedEvent> {

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if(event.getApplicationContext().getParent() != null) {
			return;
		}
		doAfterRootApplicationContextInitialized(event);
	}

	/**
	 * Spring容器启动后，即开启定时任务
	 */
	public void doAfterRootApplicationContextInitialized(ContextRefreshedEvent event) {
		startStartCronJob();
	}
	
	private void startStartCronJob () {
		ICronJobService cronJobService = SpringUtil.getBean(ICronJobService.class);
		CronJob cronJob = cronJobService.getCronJobByType(CronJob.TYPE_ITEBOOKS);
		if(cronJob == null) {
			return;
		}
		cronJobService.saveOrUpdateCronJob(cronJob);
	}

}
