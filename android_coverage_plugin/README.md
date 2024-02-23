# Coverage

覆盖率库，包含Demo App、收集覆盖率信息的library和gradle插件。

### app
示例工程：包含DUMP覆盖率信息，上传ec文件，清空覆盖率文件等功能。

### coverage-library
主要实现JacocoHelper。
1，用于获取app名称、当前分支名、提交点、设备sn、是否开启覆盖率统计，覆盖率平台地址等。
2，dump覆盖率信息，并保存到本地"/sdcard/Android/data/applicationId/cache/connected/"下面。
3，上传覆盖率信息。

### coverage-no-op
当buildType为release时，空实现，没有任何逻辑，因为release包没有开启覆盖率统计（比较耗费性能）。


### coverage-plugin
基于jacoco实现自定义的gradle插件，用于在app工程注册task，收集各种信息，聚合所需的src、class文件，压缩成zip并上传到后台。

### 集成coverage-plugin
在项目根build.gradle中添加插件依赖，配置插件属性，使用封装好的JacocoHelper类dump数据，即可完成覆盖率统计功能的集成：
```
classpath "com.duqian.coverage:coverage-plugin:1.0.0"

apply plugin: "com.duqian.coverage"

jacocoReportConfig {
    //报告生成位置
    destination "$buildDir/jacoco/report/"
    branchName 'dev'//差异覆盖率,默认：dev
    packageName = "com.duqian.coverage" //用于过滤class,不能通过applicationId来获取，因为千差万别，还是要看源码实际的包名
    includes = [packageName] //后续支持多个，只处理这个包名的类
}
```

### gradle.properties配置

```
#本地开发，如果要debug包开启覆盖率插件，设置true
coverageEnabled = true
coverageEnabledCi = true
android.forceJacocoOutOfProcess=true
#定义的名字将用于后端显示、创建目录
COV_APP_NAME=coverage-demo2
#入口模块，用于更新目录模块的BuildConfig
COV_ENTRY_MODULE=app
# 本地url和服务器url，用于切换上传文件的服务器
COV_JACOCO_HOST=http://10.255.217.193:8090
```

### app工程集成coverage-library
```
/**
 *覆盖率库
 */
dependencies {
    debugImplementation("com.duqian.coverage:coverage-library:" +coverageVersion) {
        exclude group: "com.google.code.gson", module: "gson"
        exclude group: "com.squareup.okhttp3", module: "okhttp"
    }
    releaseImplementation("com.duqian.coverage:coverage-no-op:" +coverageVersion)
}
```
### init
/**
 * 配置jacoco基本信息,自动获取gradle脚本生成的值，如果要指定某个模块初始化这些参数的值，可以在gradle.properties里面设置入口
 * 如果要测试本地地址，host写本地地址，参考JacocoHelper.LOCAL_HOST
 */
```
private void initJacocoCoverage() {
    if (BuildConfig.DEBUG) {
        JacocoHelper.initAppData(
                    BuildConfig.IS_JACOCO_ENABLE,
                    BuildConfig.CURRENT_BRANCH_NAME,
                    BuildConfig.CURRENT_COMMIT_ID,
                    BuildConfig.COV_APP_NAME,
                    BuildConfig.JACOCO_HOST
                )
    }
}
```

### 在子线程执行dump覆盖率信息并上传
```
ThreadUtil.runOnThread(() -> {
    JacocoHelper.INSTANCE.generateEcFileAndUpload(ApplicationWrapper.getContext(), devicesId,new JacocoCallback() {
        @Override
        public void onLog(@Nullable String TAG, @Nullable String msg) {
            CLog.d(TAG, "onLog=" + msg);
        }

        @Override
        public void onEcDumped(@Nullable String ecPath) {
            CLog.d(TAG, "onEcDumped=" + ecPath);
        }

        @Override
        public void onEcUploaded(boolean isSingleFile, File ecFile) {
            if (isSingleFile && ecFile != null) {
                ApplicationWrapper.getHandler().post(() -> ToastUtil.makeToast(getApplicationContext(), "覆盖率信息已上传:" + ecFile.getName(), ToastUtil.LENGTH_SHORT));
            }
        }

        @Override
        public void onIgnored(@Nullable String failedMsg) {
            ApplicationWrapper.getHandler().post(() -> ToastUtil.makeToast(getApplicationContext(), "覆盖率信息获取失败：" + failedMsg, ToastUtil.LENGTH_SHORT));
        }
    });
});
```
### baseActivity中的处理
```kotlin
JacocoHelper.generateEcFile(this)
```


### KM-Wiki
参照：[Android覆盖率系统-Gradle插件篇](http://www.duqian.site/) 


### contact me
duqian2010@gmail.com