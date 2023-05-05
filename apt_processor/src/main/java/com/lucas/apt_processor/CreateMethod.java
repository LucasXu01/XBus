package com.lucas.apt_processor;

import com.lucas.annotations.Subscribe;
import com.lucas.annotations.ThreadMode;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * 每个有注册Subscribe类中要创建的方法
 */
public class CreateMethod {
    private TypeElement typeElement;
    private Map<String, ExecutableElement> methodMap = new HashMap<>();

    public CreateMethod(TypeElement typeElement) {
        this.typeElement = typeElement;
    }

    /**
     * 放入方法名strin和方法的map，这些都是需要创建的方法索引
     *
     * @param methodName        方法名
     * @param executableElement 方法
     */
    public void putElement(String methodName, ExecutableElement executableElement) {
        methodMap.put(methodName, executableElement);
    }

    /**
     * 创建方法
     *
     * @return
     */
    public MethodSpec generateMethod() {

        ClassName subscribedMethod = ClassName.get("com.lucas.annotations", "SubscribedMethod");
        ClassName list = ClassName.get("java.util", "List");
        ClassName arrayList = ClassName.get("java.util", "ArrayList");
        ParameterizedTypeName listSubscribeMethods = ParameterizedTypeName.get(list, subscribedMethod);
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(getMethodName().toString());
        methodBuilder.returns(ParameterizedTypeName.get(list, subscribedMethod));
        methodBuilder.addModifiers(Modifier.PRIVATE, Modifier.STATIC);
        methodBuilder.addStatement("$T subscribedMethods = new $T<>()", listSubscribeMethods, arrayList);
        for (Map.Entry<String, ExecutableElement> elementEntry : methodMap.entrySet()) {
            String methodName = elementEntry.getKey();
            ExecutableElement executableElement = elementEntry.getValue();
            List<? extends VariableElement> parameters = executableElement.getParameters();
            Subscribe annotation = executableElement.getAnnotation(Subscribe.class);
            ThreadMode threadMode = annotation.threadMode();
            int priority = annotation.priority();
            methodBuilder.addStatement("subscribedMethods.add(new SubscribedMethod($T.class, $T.class, $T.$L, $L, $S))",
                this.typeElement.asType(), parameters.get(0).asType(), threadMode.getClass(), threadMode.toString(), priority, methodName);
        }
        methodBuilder.addStatement("return subscribedMethods");
        return methodBuilder.build();
    }

    public Object getMethodName() {
        return "findMethodsIn" + this.typeElement.getSimpleName();
    }
}
