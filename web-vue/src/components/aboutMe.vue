<template>
  <div style="display:block">
    <h2 style="text-align: center">关于Jacoco-Web平台的说明</h2>

    <p>CC-Jacoco-Web-1.0.0<p/>
    <p><p/>
    <p>Android覆盖率报告使用说明：<p/>
    <p>1，下载安装APK：在构建平台下载APK，如：dev_#411671_android_coverage-9.9.9_480271_bdfb318f-debug.apk，前面部分是分支名，bdfb318f是apk构建时对应的提交点。<p/>
    <p>2，运行APP：安装Debug的包，打开APP运行、测试，会生成待上传覆盖率文件，.ec文件在sdcard的/Android/data/com.netease.cc/cache/coverage/目录下。<p/>
    <p>3，上传覆盖率文件：我的-》设置-》关于-》前往调试控制面板-》生成并上传覆盖率文件。<p/>
    <p>4，生成报告：打开本平台，选择步骤1中apk对应的分支名（会自动获取当前最新上传ec文件时的commitId），可以生成报告。<p/>
    <p>5，全量&增量报告：勾选增量，需要输入待对比的CommitId，生成增量报告；也可以选择对比分支，目前只有dev，master（差异可能较大）。<p/>
    <p>6，生成增量报告后，可以在线预览，也可以保存到本地。<p/>
    <p>7，覆盖率报告管理页面，可以看到目前已经生成过的报告。<p/>
    <p><p/>
    <p>上传和下载功能，是用于手动上传class,src,ec等文件，也可以gradle脚本一件操作。<p/>
    <p>平台待不断完善，有疑问请联系：duqian02@corp.netease.com 杜小菜<p/>
  </div>
</template>

<script>
import { getUrlParam,jacocoHost,localHost } from "../utils";
export default {
  data() {
    return {};
  },
  methods: {
    download() {
      window.open(`${jacocoHost}/temp/cc-start-coverage.rar`);
    },
  },
};
</script>