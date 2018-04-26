package clonegod.mongodb.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

/**
 * Aspect that implements automatic logging on performance of the data access queries with Spring Data MongoDB.
 * 
 */
@Aspect
@Component("PerformanceProfilerAspect")
public class PerformanceProfilerAspect {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Pointcut("execution(* clonegod.mongodb.dao.*.*(..))")
    public void clientMethodPointcut() {
    }

   /**
    * Log on the performance of the interactions/queries on MongoDB.
    *
    * @param joinPoint the join point
    * @throws Throwable the throwable
    */
   @Around("clientMethodPointcut()")
   public Object retryOnConnectionException(ProceedingJoinPoint joinPoint) throws Throwable {
        Object ret = null;

         System.out.println("PerformanceProfilerAspect: Advised with logic to calculate the Time Taken "
         		+ "for the execution of the method ["+joinPoint.getSignature()+"]");
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String throwableName = null;
            try {        
               ret = joinPoint.proceed();

            } catch (Throwable t) {
                throwableName = t.getClass().getName();
                throw t;
            } finally {
                stopWatch.stop();
                if (throwableName != null) {
                	logger.warn("Timed ["+joinPoint.getSignature().toString()+"]: " +stopWatch
                            .getTotalTimeMillis()+" milliseconds , with exception ["+throwableName+"]");
                } else {
                    logger.info("Timed ["+joinPoint.getSignature().toString()+"]: " +stopWatch
                            .getTotalTimeMillis()+" milliseconds");
                }
            }

        return ret;
   }
}