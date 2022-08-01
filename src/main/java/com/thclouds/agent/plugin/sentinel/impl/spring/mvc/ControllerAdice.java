package com.thclouds.agent.plugin.sentinel.impl.spring.mvc;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.thclouds.agent.context.EntryContext;
import com.thclouds.agent.context.EntryHolder;
import net.bytebuddy.asm.Advice;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class ControllerAdice {

    @Advice.OnMethodEnter()
    public static <ParamFlowException> void enter(@Advice.This Object objInst, @Advice.Origin("#t") String className, @Advice.Origin("#m") String methodName, @Advice.AllArguments Object[] allArguments) throws Exception {
        System.out.println("  className："+objInst.getClass() + " methodName: "+methodName );
        String path = "";
        //获取到方法上的路径
        String subPath = getSubPath(objInst, methodName);
        RequestMapping basePathRequestMapping = AnnotationUtils.findAnnotation(objInst.getClass(), RequestMapping.class);
        if (basePathRequestMapping != null) {
            if (basePathRequestMapping.value().length > 0) {
                path = basePathRequestMapping.value()[0] + "/" + subPath;
            } else if (basePathRequestMapping.path().length > 0) {
                path = basePathRequestMapping.path()[0] + "/" +  subPath;
            }
        }
        System.out.println("basePath : "+path);
        Entry entry = null;
        try {
            //TODO 根据资源的来源判断
            // 被保护的逻辑
            entry = SphU.entry(path, EntryType.OUT);
            System.out.println(Thread.currentThread().getId()+"  enter");
        } catch (Exception ex) {
            System.out.println(Thread.currentThread().getId()+"  block : "+ ex.getClass());
            if (ex instanceof FlowException) {
                System.out.println(Thread.currentThread().getId()+"限流: "+ ex);
                throw new RuntimeException("BlockException 系统限流了，请稍后再试!");
            } else if (ex instanceof DegradeException) {
                System.out.println(Thread.currentThread().getId()+"降级"+  ((DegradeException) ex).getRule());
                throw new RuntimeException("BlockException 系统降级了，请稍后再试!");
            } else if (ex instanceof SystemBlockException) {
                System.out.println(Thread.currentThread().getId()+"系统规则(负载/...不满足要求)"+ex);
                throw new RuntimeException("BlockException 系统规则(负载/...不满足要求)");
            } else if (ex instanceof AuthorityException) {
                System.out.println(Thread.currentThread().getId()+"授权规则不通过" + ex);
                throw new RuntimeException("BlockException 授权规则不通过");
            }

            throw new RuntimeException("BlockException");
            // 处理被流控的逻
        }
        EntryContext.putEntryHolder(new EntryHolder(entry,null));
    }

    /**
     * 获取到注解上的路径
     * @param objInst
     * @param methodName
     * @return
     * @throws NoSuchMethodException
     */
    public static String getSubPath(Object objInst, String methodName) throws NoSuchMethodException {
        String subPath = "";
        Class<?> aClass = objInst.getClass();
        Method method = aClass.getDeclaredMethod(methodName);
        RequestMapping methodAnnotation = AnnotationUtils.findAnnotation(method,RequestMapping.class);
        Annotation[] annotations = AnnotationUtils.getAnnotations(method);
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == RequestMapping.class){
                subPath = ((RequestMapping)annotation).value()[0];
            }
            if (annotation.annotationType() == GetMapping.class){
                subPath = ((GetMapping)annotation).value()[0];
            }
            if (annotation.annotationType() == PostMapping.class){
                subPath = ((PostMapping)annotation).value()[0];
            }
            if (annotation.annotationType() == PutMapping.class){
                subPath = ((PutMapping)annotation).value()[0];
            }
            if (annotation.annotationType() == DeleteMapping.class){
                subPath = ((DeleteMapping)annotation).value()[0];
            }
            if (annotation.annotationType() == PatchMapping.class){
                subPath = ((PatchMapping)annotation).value()[0];
            }
        }
        return subPath;
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void exit(@Advice.Origin("#t") String className,
                            @Advice.Origin("#m") String methodName, @Advice.Thrown Throwable e) {
        if (e!=null && !BlockException.isBlockException(e)) {
            Tracer.trace(e);
        }
        EntryHolder entryHolder = EntryContext.getEntryHolder();
        System.out.println(Thread.currentThread().getId()+" entryHolder：" + entryHolder);
        if (null !=  entryHolder){
            entryHolder.getEntry().exit();
            System.out.println(Thread.currentThread().getId()+"  method exit");
        }
    }

}
