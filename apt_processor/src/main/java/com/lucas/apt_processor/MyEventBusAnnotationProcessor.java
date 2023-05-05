package com.lucas.apt_processor;

import com.lucas.annotations.MethodHandle;
import com.lucas.annotations.Subscribe;
import com.lucas.annotations.SubscribedMethod;
import com.lucas.annotations.Subscription;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("com.lucas.annotations.Subscribe")
public class MyEventBusAnnotationProcessor extends AbstractProcessor {

    private Map<String, CreateMethod> mCachedCreateMethod = new HashMap<>();
    //用于log打印
    private Messager messager;
    // 用于处理类中的元素
    private Elements elementUtils;
    // 用来创建java文件
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(List.class, SubscribedMethod.class);
        ClassName map = ClassName.get("java.util", "Map");
        ClassName object = ClassName.get("java.lang", "Object");
        FieldSpec aptMap = FieldSpec.builder(ParameterizedTypeName.get(map, object, parameterizedTypeName), "aptMap")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("new $T<>()", HashMap.class)
                .build();

        MethodSpec getSubscribMethod = MethodSpec.methodBuilder("getAllSubscribedMethods")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(List.class, SubscribedMethod.class))
                .addParameter(Object.class, "subscriber")
                .addCode("return aptMap.get(subscriber);")
                .build();

        MethodSpec invokeMethod = MethodSpec.methodBuilder("invokeMethod")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(Subscription.class, "subscription")
                .addParameter(Object.class, "event")
                .build();

        //类建造者
        TypeSpec.Builder aptMethodFinder = TypeSpec.classBuilder("AptMethodFinder")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(MethodHandle.class)
                .addField(aptMap)
                .addMethod(getSubscribMethod)
                .addMethod(invokeMethod);

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Subscribe.class);
        // 寻找工程中所有注解了Subscribe的方法
        for (Element element : elements) {
            // 注解了Subscribe的方法元素  强转为可执行方法元素
            ExecutableElement executableElement = (ExecutableElement) element;
            // 获取这个方法所在类元素,强转为类元素
            TypeElement typeElement = (TypeElement) executableElement.getEnclosingElement();
            // 获取全路径类名
            String qualifiedName = typeElement.getQualifiedName().toString();
            // 将这个循环里的这个元素分类，分别放到对应<类，类中所有订阅者方法>集合中，先从集合中获取这个类中的所有需要创建的方法。没有则新建
            CreateMethod createMethod = mCachedCreateMethod.get(qualifiedName);
            if (createMethod == null) {
                createMethod = new CreateMethod(typeElement);
                mCachedCreateMethod.put(qualifiedName, createMethod);
            }

            //简单方法名，非全路径
            String methodName = executableElement.getSimpleName().toString();
            //放入方法名和方法的map
            createMethod.putElement(methodName, executableElement);

        }

        CodeBlock.Builder codeBlock = CodeBlock.builder();
        //遍历所有类中所有的注解方法
        for (String key : mCachedCreateMethod.keySet()) {
            //获取一个类  key 中的所有要创建的方法。
            CreateMethod createMethod = mCachedCreateMethod.get(key);
            //创建方法并添加到类中,比如 MainActivity中，这个方法
//        private static List<SubscribedMethod> findMethodsInMainActivity(){
//            List<SubscribedMethod> subscribedMethods = new ArrayList<>();
//            subscribedMethods.add(new SubscribedMethod(com.example.zephyrbus.MainActivity.class, com.example.zephyrbus.Event.WorkEvent.class, ThreadMode.POSTING, 0, "onEvent"));
//            subscribedMethods.add(new SubscribedMethod(com.example.zephyrbus.MainActivity.class, com.example.zephyrbus.Event.ViewEvent.class, ThreadMode.MAIN, 0, "handleView"));
//            return subscribedMethods;
//        }
            aptMethodFinder.addMethod(createMethod.generateMethod());

            codeBlock.add("aptMap.put($L.class, $L());\n", key, createMethod.getMethodName());
        }
        //将静态代码块加入到类文件中。     类建造者build后变为类
        TypeSpec typeSpec = aptMethodFinder.addStaticBlock(codeBlock.build()).build();

        JavaFile javaFile = JavaFile.builder("com.lucas.xbus", typeSpec)
                .build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return false;
    }
}
