package net.yangziwen.bookshelf.pojo;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import net.yangziwen.bookshelf.util.validation.Cron;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;

@Entity
@Table(name="cron_job")
public class CronJob {
	
	public static final String TYPE_ITEBOOKS = "itebooks";
	
	private static final String NAME_ERROR_MESSAGE = "请输入定时任务的名称!";
	private static final String CRON_ERROR_MESSAGE = "请输入有效的cron表达式!";
	
	@Id
	@GeneratedValue
	@Column
	private Long id;
	@Column
	@NotBlank(message = NAME_ERROR_MESSAGE)
	private String name;
	@Column
	private String type;
	@Column
	
	@NotBlank(message = CRON_ERROR_MESSAGE)
	@Cron(message = CRON_ERROR_MESSAGE)
	private String cron;
	@Column(name="update_time")
	private Timestamp updateTime;
	@Column
	@NotNull
	private Boolean enabled;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getCron() {
		return cron;
	}
	public void setCron(String cron) {
		this.cron = cron;
	}
	public Timestamp getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}
	public Boolean getEnabled() {
		return enabled;
	}
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	
	public Trigger createTrigger() {
		return new CronTrigger(cron);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
	
}
