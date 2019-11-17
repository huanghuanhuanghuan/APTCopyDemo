package com.example.compiler;

import com.example.annotation.ViewHolderBinder;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

//@SupportedAnnotationTypes({"com.example.annotation.ViewHolderBinder"})
@AutoService(Processor.class)
public class AnnotationProcessor extends AbstractProcessor {
    private Messager mMessager = null;

    private static final ClassName inflaterClass = ClassName.get("android.view", "LayoutInflater");
    private static final ClassName viewClass = ClassName.get("android.view", "View");
    private static final ClassName viewGroupClass = ClassName.get("android.view", "ViewGroup");
    private static final ClassName multiTypeAdapter = ClassName.get("me.drakeet.multitype", "MultiTypeAdapter");
    private static final ClassName itemViewBinder = ClassName.get("me.drakeet.multitype", "ItemViewBinder");


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mMessager = processingEnvironment.getMessager();
        mMessager.printMessage(Diagnostic.Kind.NOTE, "init");
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(ViewHolderBinder.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, "process");
        try {
            createFactoryAndHelper(roundEnvironment);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 创建全部工厂类
     * @param roundEnvironment
     * @throws IOException
     */
    private void createFactoryAndHelper(RoundEnvironment roundEnvironment) throws IOException {
        Set<? extends Element> set = roundEnvironment.getElementsAnnotatedWith(ViewHolderBinder.class);
        if (set != null) {
            ArrayList<BinderInfo> binderList = new ArrayList<>();
            for (Element element : set) {
                if (element.getKind() == ElementKind.CLASS) {
                    BinderInfo binderInfo = createFactory((TypeElement)element);
                    binderList.add(binderInfo);
                }
            }

            crateMyMultiTypeHelper(binderList);
        }
    }

    /**
     * 创建具体工厂类
     * @param element
     */
    private BinderInfo createFactory(TypeElement element) throws IOException {
        // 获取到data ViewHolder 以及xml信息
        TypeName contentDataClassName = getContentClassName(element);
        TypeName viewHolderClassName = TypeName.get(element.asType());
        int xmlType = element.getAnnotation(ViewHolderBinder.class).xml();

        // 创建onCreateViewHolder方法
        MethodSpec onCreateViewHolderMethod = MethodSpec.methodBuilder("onCreateViewHolder")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(inflaterClass, "inflater")
                .addParameter(viewGroupClass, "parent")
                .returns(TypeName.get(element.asType()))
                .addCode("final $T view = inflater.inflate($L, parent, false);\n", viewClass, xmlType)
                .addCode("return new $T(view);\n", viewHolderClassName)
                .addModifiers()
                .build();

        // 创建onBindViewHolder方法
        MethodSpec onBindViewHolderMethod = MethodSpec.methodBuilder("onBindViewHolder")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(viewHolderClassName,"vh", Modifier.FINAL)
                .addParameter(contentDataClassName, "data", Modifier.FINAL)
                .addCode("vh.onBindData(data);\n")
                .build();

        // 实现的接口对象
        TypeName parentClassType = ParameterizedTypeName.get(itemViewBinder, contentDataClassName, viewHolderClassName);

        // 创建XXFactory类对象
        TypeSpec typeSpec = TypeSpec.classBuilder(element.getSimpleName() + "Factory")
                .addModifiers(Modifier.PUBLIC)
                .superclass(parentClassType)
                .addMethod(onCreateViewHolderMethod)
                .addMethod(onBindViewHolderMethod)
                .build();

        // 写入文件，生成.java文件
        JavaFile javaFile = JavaFile.builder("com.hh.example", typeSpec).build();
        javaFile.writeTo(processingEnv.getFiler());

        // 返回获取到的信息
        BinderInfo binderInfo = new BinderInfo();
        binderInfo.viewHolderClass = viewHolderClassName;
        binderInfo.dataClass = contentDataClassName;
        return binderInfo;
    }

    private TypeName getContentClassName(TypeElement element) {
        TypeName typeName = null;
        TypeMirror typeMirror = element.getInterfaces().get(0);
        if (typeMirror instanceof DeclaredType) {
            List<? extends TypeMirror> typeArguments = ((DeclaredType) typeMirror).getTypeArguments();
            if (typeArguments != null && typeArguments.size() == 1) {
                typeName = TypeName.get(typeArguments.get(0));
                mMessager.printMessage(Diagnostic.Kind.NOTE, "interface[0] = " + typeName);
            }
        }
        return typeName;
    }

    private void crateMyMultiTypeHelper(ArrayList<BinderInfo> binderList) throws IOException {

        // 方法体内容
        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
        for (BinderInfo info : binderList) {
            codeBlockBuilder.add("adapter.register($T.class, new $TFactory());\n", info.dataClass, info.viewHolderClass);
        }

        // 创建bindVHFactory方法
        MethodSpec getBindersMethod = MethodSpec.methodBuilder("bindVHFactory")
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .addParameter(multiTypeAdapter, "adapter")
                .returns(void.class)
                .addCode(codeBlockBuilder.build())
                .build();

        // 创建MyMultiTypeHelper 对象
        TypeSpec typeSpec = TypeSpec.classBuilder("MyMultiTypeHelper")
                .addModifiers(Modifier.PUBLIC)
                .addMethod(getBindersMethod)
                .build();

        // 写入文件
        JavaFile javaFile = JavaFile.builder("com.hh.example", typeSpec).build();
        javaFile.writeTo(processingEnv.getFiler());
    }

    class BinderInfo {
        TypeName dataClass;
        TypeName viewHolderClass;
    }
}


