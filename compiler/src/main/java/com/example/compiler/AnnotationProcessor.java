package com.example.compiler;

import com.example.annotation.MyAnnotation;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

//@SupportedAnnotationTypes({"com.example.annotation.MyAnnotation"})
//@AutoService(Processor.class)
public class AnnotationProcessor extends AbstractProcessor {
    private Messager mMessager = null;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mMessager = processingEnvironment.getMessager();
        mMessager.printMessage(Diagnostic.Kind.NOTE, "init");
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> result = Collections.singleton(MyAnnotation.class.getCanonicalName());
        mMessager.printMessage(Diagnostic.Kind.NOTE, "getSupportedAnnotationTypes result = " + result);
        return result;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, "process");
        try {
            createClass();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void createClass() throws IOException {
        MethodSpec main = MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args")
                .addStatement("$T.out.println($S)", System.class, "Hello, Java poet!")
                .build();

        TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
                .addModifiers(Modifier.PUBLIC)
                .addMethod(main)
                .build();

        JavaFile javaFile = JavaFile.builder("com.hh.example", helloWorld)
                .build();

        File file = new File(".");
        if (!file.exists()) {
            file.createNewFile();
        }
        javaFile.writeTo(file);
    }
}


