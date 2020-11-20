package com.practice.common.aspect;

import com.practice.common.ResponseObject;
import com.practice.common.Constant;
import com.practice.common.redis.RedisService;
import com.practice.util.DateParseUtil;
import com.practice.util.RegxUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.Date;
import java.util.List;

@Aspect
@Component
public class ParamAspect {
    private static final Logger logger = LoggerFactory.getLogger(ParamAspect.class);

    @Pointcut("@annotation(com.practice.common.annotation.ApiCheck)")
    public void log() {
    }

    //@Before("log()")
    public ResponseObject doBefore(JoinPoint joinPoint) {
        return null;
    }

    @Around("log()")
    public Object doAround(ProceedingJoinPoint proceeJoinPoint) throws Throwable {
        long time_1 = System.currentTimeMillis();
        String api = proceeJoinPoint.getSignature().getName();
        logger.info("=============== before, target className: {},  method: {} ======================== ", proceeJoinPoint.getSignature().getDeclaringType()
                .getSimpleName(), proceeJoinPoint.getSignature().getName());

        Object[] args = proceeJoinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof BindingResult) {
                BindingResult validResult = (BindingResult) arg;
                if (validResult.hasErrors()) {
                    return ResponseObject.newErrorResponseObject(Constant.REQ_ILLEGAL_CODE, validErrorMsg(validResult));
                }
            }
        }

        Object result = proceeJoinPoint.proceed();
        long t = (System.currentTimeMillis() - time_1) / 1000;
        logger.info("--------------------- request api: {}, exec end time at {}, take time in total: {}小时{}分{}秒", api,
                DateParseUtil.dateTimeToString(new Date()), t / 3600, t % 3600 / 60, t % 60);
        return result;
    }

    //@After("")
    public void doAfter(JoinPoint joinPoint) {
        logger.info("******拦截后的逻辑******");
    }

    public String validErrorMsg(BindingResult validResult) {
        List<ObjectError> list = validResult.getAllErrors();
        for (ObjectError objectError : list) {
            logger.info(objectError.toString());
        }
        return RegxUtil.extractTargetBetweenSymbolRange(validResult.toString(), RegxUtil.REG_ZH, RegxUtil.REG_TX);
    }

}
