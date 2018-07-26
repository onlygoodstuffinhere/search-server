package chop.sanic.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.jboss.logging.Logger;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LogStateChangesAspect {

	private final static Logger LOGGER =Logger.getLogger(LogStateChangesAspect.class); 
	
	@AfterReturning("@annotation(StateImpacting)")
	public void logExecutionTime(JoinPoint joinPoint) throws Throwable {
	    //return joinPoint.proceed();
		LOGGER.trace("Executed "+joinPoint.getSignature().toShortString() 
				+ " with args "
				+ joinPoint.getArgs().toString());
	}

}
