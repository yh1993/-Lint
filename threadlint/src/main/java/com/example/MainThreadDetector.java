package com.example;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.lint.client.api.JavaParser;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

import java.util.Collections;
import java.util.List;

import lombok.ast.AstVisitor;
import lombok.ast.ConstructorInvocation;
import lombok.ast.ForwardingAstVisitor;
import lombok.ast.Node;

import static com.android.SdkConstants.SUPPORT_ANNOTATIONS_PREFIX;
import static com.android.tools.lint.checks.SupportAnnotationDetector.BINDER_THREAD_ANNOTATION;
import static com.android.tools.lint.checks.SupportAnnotationDetector.MAIN_THREAD_ANNOTATION;
import static com.android.tools.lint.checks.SupportAnnotationDetector.UI_THREAD_ANNOTATION;
import static com.android.tools.lint.checks.SupportAnnotationDetector.WORKER_THREAD_ANNOTATION;
import static com.android.tools.lint.detector.api.JavaContext.findSurroundingClass;
import static com.android.tools.lint.detector.api.JavaContext.findSurroundingMethod;


/**
 * Created by yanghao on 17-8-21.
 */

public class MainThreadDetector extends Detector implements Detector.JavaScanner {
    public static final String THREAD_SUFFIX = "Thread";

    public static final Issue ISSUE = Issue.create(
            "I/O操作",
            "避免在主线程使用耗时的I/O操作",
            "使用子线程处理耗时操作",
            Category.SECURITY,5, Severity.ERROR,
            new Implementation(MainThreadDetector.class, Scope.JAVA_FILE_SCOPE)
    );

    @Override
    public List<Class<? extends Node>> getApplicableNodeTypes() {
        return Collections.<Class<? extends Node>>singletonList(ConstructorInvocation.class);
    }

    @Override
    public AstVisitor createJavaVisitor(final JavaContext context) {
        return new ForwardingAstVisitor() {
            @Override
            public boolean visitConstructorInvocation(ConstructorInvocation node) {
                JavaParser.ResolvedNode resolvedNode = context.resolve(node.astTypeReference());
                JavaParser.ResolvedClass resolvedClass = (JavaParser.ResolvedClass) resolvedNode;

                if(resolvedClass != null && (resolvedClass.isSubclassOf("java.io.InputStream",false)
                        ||resolvedClass.isSubclassOf("java.io.OutputStream",false))){
                    JavaParser.ResolvedNode resolved = context.resolve(node);
                    if(resolved instanceof JavaParser.ResolvedMethod){
                        JavaParser.ResolvedMethod method = (JavaParser.ResolvedMethod) resolved;
                        checkThreading(context, node, method);
                    }
//                    ClassDeclaration surroundingClass = JavaContext.findSurroundingClass(node);
//                    JavaParser.ResolvedClass surround = (JavaParser.ResolvedClass) context.resolve(surroundingClass);
//                    if ((surround.isSubclassOf("java.lang.Thread",false)||surround.isInheritingFrom("java.lang.Runnable",false))){
//                        context.report(ISSUE,node,context.getLocation(node),"I/O操作");
//                    }

                    return true;
                }

                return super.visitConstructorInvocation(node);
            }
        };
    }

    private static void checkThreading(
            @NonNull JavaContext context,
            @NonNull Node node,
            @NonNull JavaParser.ResolvedMethod method) {
        getThreadContext(context, node);
//        String threadContext = getThreadContext(context, node);
//        if (threadContext != null && isUIThread(threadContext)) {
//            String message = String.format("Method %1$s must be called from the work thread, currently inferred thread is `%3$s` thread",
//                    method.getName(), describeThread(threadContext));
//            context.report(ISSUE, node, context.getLocation(node), message);
//        }
    }

    @NonNull
    public static String describeThread(@NonNull String annotation) {
        if (UI_THREAD_ANNOTATION.equals(annotation)) {
            return "UI";
        }
        else if (MAIN_THREAD_ANNOTATION.equals(annotation)) {
            return "main";
        }
        else if (BINDER_THREAD_ANNOTATION.equals(annotation)) {
            return "binder";
        }
        else if (WORKER_THREAD_ANNOTATION.equals(annotation)) {
            return "worker";
        } else {
            return "other";
        }
    }

    /** returns true if the thread are UIThread */
    public static boolean isUIThread(@NonNull String thread) {

        // Allow @UiThread and @MainThread to be combined
        if (thread.equals(UI_THREAD_ANNOTATION)||thread.equals(MAIN_THREAD_ANNOTATION)) {
            return true;

        } else if (thread.equals(WORKER_THREAD_ANNOTATION)) {
            return false;
        }

        return false;
    }

    /** Attempts to infer the current thread context at the site of the given method call */
    @Nullable
    private static void getThreadContext(@NonNull JavaContext context,
                                           @NonNull Node methodCall) {
        Node node = findSurroundingMethod(methodCall);
        if (node != null) {
            JavaParser.ResolvedNode resolved = context.resolve(node);
            if (resolved instanceof JavaParser.ResolvedMethod) {
                JavaParser.ResolvedMethod method = (JavaParser.ResolvedMethod) resolved;
                JavaParser.ResolvedClass cls = method.getContainingClass();

//                context.report(ISSUE, node, context.getLocation(node),  method + " " +cls.isInheritingFrom("java.lang.Runnable",false)+" "
//                        + cls.isSubclassOf("java.lang.Thread",false));


//                String scls = "";
//                while(cls != null){
//                    scls += cls.getName();
//                    cls.getSuperClass();
//                }
//
//                String smethod = "";
//                while (method != null){
//                    smethod += method.getName();
//                    method = method.getSuperMethod();
//                }
//
//                context.report(ISSUE, node, context.getLocation(node), scls + " "+smethod);

                if(cls != null){
                    if (cls.isSubclassOf("java.lang.Thread",false) || cls.isInheritingFrom("java.lang.Runnable",false)) {
                        return;
                    }else{
                        context.report(ISSUE, node, context.getLocation(node),  "UIThread I/O"+method + " " +cls);
                    }
                }

//                String scls = "";
//                while(cls != null){
//                    if(cls.isSubclassOf("java.lang.Thread",false) ||
//                            cls.isInheritingFrom("java.lang.Runnable",false)){
//                        context.report(ISSUE, node, context.getLocation(node),"workThread "+cls);
//                    }else{
//                        getThreadContext(context,node);
//                    }
//                    scls += cls.getName();
//                    cls.getSuperClass();
//                }

                if(cls == null){
                    context.report(ISSUE, node, context.getLocation(node),method +" "+ cls +" "+node.getParent().toString());
                }


//                while (method != null) {
//                    for (JavaParser.ResolvedAnnotation annotation : method.getAnnotations()) {
//                        String name = annotation.getSignature();
//                        if (name.startsWith(SUPPORT_ANNOTATIONS_PREFIX)
//                                && name.endsWith(THREAD_SUFFIX)) {
//                            return name;
//                        }
//                    }
//                    method = method.getSuperMethod();
//                }
//
//                // See if we're extending a class with a known threading context
//                while (cls != null) {
//                    for (JavaParser.ResolvedAnnotation annotation : cls.getAnnotations()) {
//                        String name = annotation.getSignature();
//                        if (name.startsWith(SUPPORT_ANNOTATIONS_PREFIX)
//                                && name.endsWith(THREAD_SUFFIX)) {
//                            return name;
//                        }
//                    }
//                    cls = cls.getSuperClass();
//                }
            }
        }

        // In the future, we could also try to infer the threading context using
        // other heuristics. For example, if we're in a method with unknown threading
        // context, but we see that the method is called by another method with a known
        // threading context, we can infer that that threading context is the context for
        // this thread too (assuming the call is direct).

        return;
//        return null;
    }



//    @Override
//    public List<String> getApplicableConstructorTypes() {
//        return Arrays.asList("java.io.InputStream","java.io.OutputStream");
//    }
//
//    @Override
//    public void visitConstructor(JavaContext context, AstVisitor visitor, ConstructorInvocation node, JavaParser.ResolvedMethod constructor) {
//        super.visitConstructor(context, visitor, node, constructor);
//    }
}
