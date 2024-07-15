<template>
  <div style="display:block">
    <h2 style="text-align: center">Code Coverage System</h2>
    <p style="text-align:center">Code Coverage 1.0.0<p/>

    <p>Android覆盖率报告生成步骤<p/>
    <p>1，打包编译APK，安装APK。确定apk构建app对应的分支和代码Commit hash值<p/>
    <p>2，运行APP：安装Debug的包，打开APP运行、测试，会生成待上传覆盖率文件，.ec文件在sdcard的/Android/data/pkg/cache/coverage/目录下。<p/>
    <p>3，上传覆盖率文件：各个App集成Coverage插件，app端彩蛋页面提供入口：生成并上传覆盖率文件；清除所有覆盖率文件。<p/>
    <p>4，生成报告：打开本平台，选择步骤1中apk对应的分支名生成报告，系统会自动获取所选分支最新上传ec文件时的commitId<p/>
    <p>5，全量&增量报告：勾选增量，需要输入待对比的CommitId，生成增量报告；如果gitlab没有获取到diff文件，默认取本地上传的diff文件，目前基准分支建议gradle配置dev或者master（增量差异可能较大）。<p/>
    <p>6，报告查看：生成增量报告后，可以在线预览，也可以保存到本地。<p/>
    <p>7，报告管理：覆盖率报告管理页面，可以看到目前已经生成过的报告。<p/>
    <p>8，其他功能：上传和下载功能，用于手动上传class,src,ec等文件，也可以gradle脚本一件操作，以及一些接口测试功能。<p/>
    <p style="color:blue">欢迎提供建议或意见，平台将不断完善，有疑问请联系:杜小菜：<a href="mailto:duqian2010@gmail.com">duqian2010@gmail.com</a><p/>
  </div>
</template>

<script>
export default {
  data() {
    return {};
  },
  methods: {
   
  },
};
</script>