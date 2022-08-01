package com.thclouds.agent.plugin.sentinel.impl.spring.mvc;

import com.thclouds.agent.plugin.IPlugin;
import com.thclouds.agent.plugin.InterceptPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

public class ControllerPlugin implements IPlugin {

    @Override
    public String name() {
        return "ControllerPlugin";
    }

    @Override
    public InterceptPoint[] buildInterceptPoint() {
        return new InterceptPoint[]{
                new InterceptPoint() {
                    @Override
                    public ElementMatcher<TypeDescription> buildTypesMatcher() {
                        return ElementMatchers.isAnnotatedWith(RestController.class).or(ElementMatchers.isAnnotatedWith(Controller.class));
                    }

                    @Override
                    public ElementMatcher<MethodDescription> buildMethodsMatcher() {
                        return ElementMatchers
                                .isAnnotatedWith(ElementMatchers.named("org.springframework.web.bind.annotation.GetMapping"))
                                .or(ElementMatchers.isAnnotatedWith(ElementMatchers.named("org.springframework.web.bind.annotation.PostMapping")))
                                .or(ElementMatchers.isAnnotatedWith(ElementMatchers.named("org.springframework.web.bind.annotation.PutMapping")))
                                .or(ElementMatchers.isAnnotatedWith(ElementMatchers.named("org.springframework.web.bind.annotation.DeleteMapping")))
                                .or(ElementMatchers.isAnnotatedWith(ElementMatchers.named("org.springframework.web.bind.annotation.PatchMapping")))
                                .or(ElementMatchers.isAnnotatedWith(ElementMatchers.named("org.springframework.web.bind.annotation.RequestMapping")))
                                ;

                    }

                }
        };
    }

    @Override
    public Class adviceClass() {
        return ControllerAdice.class;
    }

}
