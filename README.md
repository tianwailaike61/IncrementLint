# IncrementLint
该项目主要针对Android项目编写lint gradle 插件，实现lint增量检查。参考：[自定义lint增量检查](https://www.jianshu.com/p/98b8b7d6fed3)

## 功能描述

- 1、自定义Android常用规则（参见lib-rules)  
- 2、利用gradle插件和版本管理（git或svn）实现对修改代码进行检查，避免大型项目代码量过大导致检查时间长的问题
- 3、该项目自定义规则保证仅检查java代码的正确性

## 使用方式
- 1、将[output](https://github.com/tianwailaike61/IncrementLint/output)中的文件拷贝到某文件夹
- 2、在项目根目录的build.gradle添加如下信息：
```
buildscript {
	// 添加公共信息
    ext {
        REMOTE_URL = new File(rootDir.absolutePath, "output").toURI() //todo 修改为output中文件存放路径
        GROUP = 'com.jianghongkui.lint'
        LIB_VERSION = '1.0.0'
    }
    repositories {
        maven { url REMOTE_URL } //添加URL
        google()
        jcenter()

    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.2.1'
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.30"
        classpath "$GROUP:increment:$LIB_VERSION" //添加插件包
    }
}

```
- 3、在app的build.gradle中添加插件和自定义lint规则包

```
apply plugin: 'com.android.application'
apply plugin: 'IncrementLint' //添加gradle插件

dependencies {
    ....
    //添加自定义的lint规则包
    lintChecks "{group}:{name}:{version}"
}
```
### 日志
- 2021.6.23  
 兼容4.2插件版本  
- 2021.5.3  
  兼容3.2~4.1插件版本  
- 2020.11.5  
  解决升级AndroidX包中自定义规则导致lib-rules中的规则无效

### 参考  
[Android Lint增量扫描实战纪要](https://www.jianshu.com/p/4833a79e9396)  
[Android Lint工作原理剖析](http://www.androidchina.net/5106.html)

