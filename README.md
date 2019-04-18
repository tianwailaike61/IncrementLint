# IncrementLint
该项目主要针对Android项目编写lint gradle 插件，实现lint增量检查。参考：[自定义lint增量检查](https://www.jianshu.com/p/98b8b7d6fed3)

### 使用方式
1、修改lib_lint_plugin模块下的build.gradle
```
uploadArchives {
    repositories.mavenDeployer {
        repository(url: uri('/Users/jianghongkui/IntellijProject/repo')) //修改此处URL
        pom.groupId = "com.jianghongkui.lint"
        pom.artifactId = "increment"
        pom.version = "1.0.0"
    }
}

```
2、执行uploadArchives任务，将lib_lint_plugin模块编译为jar包并上传到服务器
3、将步骤1中的URL添加到build.gradle的repositories中,并添加步骤1中的groupId、artifactId到依赖
```
buildscript {
    repositories {
        maven { url '/Users/jianghongkui/IntellijProject/repo' } //添加maven库URL
        ....
    }
    dependencies {
        ....
        classpath 'com.jianghongkui.lint:increment:1.0.0' //添加步骤2中编译传的包
    }
}

allprojects {
    repositories {
        maven { url '/Users/jianghongkui/IntellijProject/repo' } //添加maven库URL
        ....
    }
}

```
4、在app的build.gradle中添加插件和自定义lint规则包

```
apply plugin: 'com.android.application'
apply plugin: 'IncrementLint' //添加gradle插件

dependencies {
    ....
    
    debugImplementation "{group}:{name}:{version}" //添加自定义的lint规则包
}
```


###参考
[Android Lint增量扫描实战纪要](https://www.jianshu.com/p/4833a79e9396)
[Android Lint工作原理剖析](http://www.androidchina.net/5106.html)

