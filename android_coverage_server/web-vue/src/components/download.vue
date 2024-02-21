<template>
  <div>
    <h2 style="text-align: center">覆盖率报告文件列表(待开发)</h2>

    <el-button style="text-align: center" @click="downFile" >测试下载</el-button>
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
    downFile() {
      let filePath = "android/dev_#411671_android_coverage/2c9f2fe4/ec/0bd2920814e8def68e984be4be59d147.ec"
      let path = encodeURIComponent(filePath)
      let url = `${jacocoHost}/download?path=${path}`,
        fileName = "0bd2920814e8def68e984be4be59d147.ec",//getUrlParam(url, "fileName"),
        _this = this,
        url2 = url.replace(/\\/g, "/"),
        xhr = new XMLHttpRequest();
        xhr.open("GET", url2, true);
        xhr.responseType = "blob";
        xhr.onload = () => {
          if (xhr.status === 200) {
            // 获取文件blob数据并保存
            _this.saveAs(xhr.response, fileName);
          }
        };
      xhr.send();
    },
    saveAs(data, fileName) {
      const urlObject = window.URL || window.webkitURL || window;
      const export_blob = new Blob([data]);
      //createElementNS() 方法可创建带有指定命名空间的元素节点。
      //此方法可返回一个 Element 对象。
      const save_link = document.createElementNS(
        "http://www.w3.org/1999/xhtml",
        "a"
      );
      save_link.href = urlObject.createObjectURL(export_blob);
      save_link.download = fileName;
      save_link.click();
    },
  },
};
</script>