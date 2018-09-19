>**看完本篇文章你将学到什么？**
>
>1.基本了解AOP的编程思想，以及如何在Android中使用AOP的思想  
>2.借助AspectJ用AOP的思想实现埋点逻辑  
>3.借助AspectJ用AOP的思想实现屏蔽快速点击事件的处理
>

### 什么是AOP

AOP，字面翻译为面向切面编程。它是一种编程思想，不是什么新技术。可以这么理解，在Android开发过程中，我们经常会在我们的具体业务代码中加入全局性、系统性的与具体业务无关的代码。比如埋点、动态申请权限等等。AOP的思想就是将这些与业务无关的系统性的功能解耦出来，让代码看起来更清晰一点。使用AOP思想与正常程序流程的对比我们可以通过下面示例图片有个基础的认识：

**插入图片：AOP思想区别.jpg**

上图1我们看到是我们正常的程序流程，程序的执行就像水从管道流出一样从上到下顺畅的**纵向**执行。图2为我们展示了AOP思想的程序执行流程，从图中可以看到我们的管道被从某一点**横向**的切开(AOP中将这个切点定义为**pointCut**)，然后会在切入点植入一段我们在**Aspect**(类似于Java的类的理念，AOP中用来管理切点和执行代码块的一个概念)中定义的代码。示例仅是一种演示，实际情况是植入执行的代码块可以向被插入方法的前后同时植入代码块。目前实现AOP思想的框架有**AspectJ**、Spring、JBoss4.0等等。

### AspectJ框架

为什么这里选择介绍AspectJ呢？主要是我公司的项目使用的是这个框架。那么什么是AspectJ？

它是实现AOP思想的一个框架，拥有自己的编译器和语法，可以在编译期间将需要植入的代码编译成Java代码插入到你的源代码文件当中。更直白点

我们的Android项目引用也非常的简单，2步就可以搞定：

* 在项目的根目录的**Build.gradle**文件中加入下面的代码：

  ```java
    dependencies {
        classpath 'com.hujiang.aspectjx:gradle-android-plugin-aspectjx:2.0.0'
    }
  ```
* 在你的app模块或者其他module中添加依赖：

  ```java
  dependencies {
       implementation 'org.aspectj:aspectjrt:1.8.9'
  }
  ```
  
  
### 应用
我们在项目中经常会遇到一个小问题，就是你对一个View绑定的一个点击事件用户疯狂的快速点击导致点击事件的处理代码被重复执行，常见的处理方式就是在点击事件中加入时间判断的逻辑，在短时间内的重复点击不做响应。但是如果有很多点击事件，这样处理起来显然让代码不好看。

所以，借助AspectJ中的AOP思想，可以在解决快速重复点击的问题的同时，可以让你的代码看的更优雅一点。

##### 第一步：自定义一个注解

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface IgnoreFastClick {
    long value() default 2000;
}
```

##### 第二步：写Aspect植入的类

```java
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
            // 如果两次点击时间差小于或等于value的值，并且两次点击的是同一个View，就不执行onClick()方法
            if (nowTime - lastClickTime <= ignoreFastClick.value()
                    && view.getId() == lastViewId) {
                Log.d("AOP", "you click is too fast!");
            } else {
                // 记住上一次点击的时间戳和View的ID
                lastViewId = view.getId();
                lastClickTime = nowTime;
                执行onCLick()方法
                joinPoint.proceed();
            }
        }
    }
}


```

##### 第三步：给click事件方法加注解

```java
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTestBtn = findViewById(R.id.btn_test);
        mTestBtn.setOnClickListener(new View.OnClickListener() {

            @IgnoreFastClick
            @Override
            public void onClick(View v) {
                Log.d("AOP", "----------------"+number++);
            }
        });
    }
```

以后只要做防重点击的事件，只用加个注解就搞定了。

要想完全掌握AspectJ的语法还是挺多的，这里只是一个简单的抛砖引玉的例子哦。读者可以试试使用AspectJ完成事件统计相关的逻辑哦。讲的不好，仅供参考。

如果觉得本文对你有一点点的帮助，关注一下又能有什么损失呢