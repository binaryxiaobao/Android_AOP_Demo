package com.bob.www.testdemo.aop;

import android.util.Log;
import android.view.View;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

@Aspect
public class IgnoreFastClickAspect {

    int lastViewId=-1;
    long lastClickTime=0;

    @Pointcut("execution(@com.bob.www.testdemo.aop.IgnoreFastClick * *(..))")
    public void hhh(){
    }

    @Around("hhh()")
    public void insertCodeBlock(ProceedingJoinPoint joinPoint) throws Throwable {
        View view;
        //取click方法中的参数view
        if (joinPoint.getArgs() != null
                && joinPoint.getArgs().length > 0
                && joinPoint.getArgs()[0] instanceof View) {
            view = ((View) joinPoint.getArgs()[0]);
            if (view == null)
                return;
        } else {
            return;
        }

        MethodSignature methodSignature = ((MethodSignature) joinPoint.getSignature());
        Method method = methodSignature.getMethod();
        if (method.isAnnotationPresent(IgnoreFastClick.class)) {
            IgnoreFastClick ignoreFastClick = method.getAnnotation(IgnoreFastClick.class);
            long nowTime = System.currentTimeMillis();
            if (nowTime - lastClickTime <= ignoreFastClick.value()
                    && view.getId() == lastViewId) {
                Log.d("AOP", "you click is too fast!");
            } else {
                lastViewId = view.getId();
                lastClickTime = nowTime;
                joinPoint.proceed();
            }
        }
    }
}
