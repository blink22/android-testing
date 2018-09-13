package com.example.android.testing.notes.processor;

import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * Used to list all the instrumentation test cases that are supposed to run on
 * cloud-based app-testing infrastructure, e.g Firebase test-lab.
 */
public final class InstrumentationTestsProcessor extends AbstractProcessor {

    private static final String GENERATED_FILE_PATH = "ui-tests";
    private static final String TEST_METHOD_NAME_DELIMITER = "#";
    private static final String SUCCESSFUL_PROCESS_MESSAGE = "Instrumentation tests file has been generated successfully.";
    private static final String FAILED_PROCESS_MESSAGE = "Failed to generate instrumentation tests file.";

    private PrintWriter mPrintWriter;
    private Set<Element> mLoadedClasses;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new LinkedHashSet<String>() {{
            add(Test.class.getCanonicalName());
        }};
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mLoadedClasses = new HashSet<>();
        initPrintWriter(false); // create new file
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> annotatedMethods = roundEnv.getElementsAnnotatedWith(Test.class);
        loadClasses(annotatedMethods);
        Set<String> testMethods = loadTestMethodsFullNames(annotatedMethods);
        printSortedMethodsNamesFile(testMethods);
        printProcessSuccessMessage();
        return false; // allows subsequent annotation processors to process @Test annotation
    }

    private void initPrintWriter(boolean append) {
        try {
            FileWriter fileWriter = new FileWriter(GENERATED_FILE_PATH, append);
            mPrintWriter = new PrintWriter(fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Only detects test classes whose siblings, children of same package, have methods annotated
     * with @Test
     *
     * @param testMethods
     */
    private void loadClasses(Set<? extends Element> testMethods) {
        Element testClass;
        Element testPackage;
        for (Element testMethod : testMethods) {
            testClass = testMethod.getEnclosingElement();
            testPackage = testClass.getEnclosingElement();
            mLoadedClasses.addAll(testPackage.getEnclosedElements());
        }
    }

    private Set<String> loadTestMethodsFullNames(Set<? extends Element> testMethods) {
        Set<String> fullNameTestMethods = new HashSet<>();

        for (Element method : testMethods) {
            List<Element> testClasses = findTestClassesContaining(method);
            for (Element testClass : testClasses) {
                fullNameTestMethods.add(getFullMethodName(testClass, method));
            }
        }

        return fullNameTestMethods;
    }

    private List<Element> findTestClassesContaining(Element method) {
        String enclosingClass = method.getEnclosingElement().toString();
        List<Element> testClasses = new LinkedList<>();

        for (Element testClass : mLoadedClasses) {
            if (isAbstract(testClass)) continue;
            if (!isSubtypeOfType(testClass.asType(), enclosingClass)) continue;
            testClasses.add(testClass);
        }

        return testClasses;
    }

    private boolean isAbstract(Element element) {
        return element.getModifiers().contains(Modifier.ABSTRACT);
    }

    private boolean isSubtypeOfType(TypeMirror type, String otherType) {
        if (type.getKind() != TypeKind.DECLARED) return false;
        if (isTypeEqual(type.toString(), otherType)) return true;

        TypeMirror superType = findDirectSuperType(type);
        return isSubtypeOfType(superType, otherType);
    }

    private boolean isTypeEqual(String type, String otherType) {
        type = extractClassName(type);
        otherType = extractClassName(otherType);
        return type.equals(otherType);
    }

    private String extractClassName(String type) {
        int lastValidCharIndex = type.indexOf('<') != -1 ? type.indexOf('<') : type.length();
        return type.substring(0, lastValidCharIndex);
    }

    private TypeMirror findDirectSuperType(TypeMirror type) {
        DeclaredType declaredType = (DeclaredType) type;
        TypeElement typeElement = (TypeElement) declaredType.asElement();
        return typeElement.getSuperclass();
    }

    private String getFullMethodName(Element testClass, Element method) {
        return testClass.toString()
                + TEST_METHOD_NAME_DELIMITER
                + method.toString().substring(0, method.toString().length() - 2); // remove "()" from name
    }

    private void printSortedMethodsNamesFile(Set<String> methods) {
        initPrintWriter(true); // append to created file

        List<String> sortedMethods = new ArrayList<>(methods);
        Collections.sort(sortedMethods);

        for (String method : sortedMethods) {
            mPrintWriter.println(method);
        }

        mPrintWriter.close();
    }

    private void printProcessSuccessMessage() {
        File file = new File(GENERATED_FILE_PATH);
        String message = file.length() > 0 ? SUCCESSFUL_PROCESS_MESSAGE : FAILED_PROCESS_MESSAGE;
        System.out.println(message);
    }

}
