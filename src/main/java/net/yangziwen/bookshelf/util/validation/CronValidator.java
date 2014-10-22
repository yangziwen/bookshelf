package net.yangziwen.bookshelf.util.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.scheduling.support.CronTrigger;

public class CronValidator implements ConstraintValidator<Cron, String>{

	@Override
	public void initialize(Cron constraintAnnotation) {}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		try {
			new CronTrigger(value);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
