<template>
  <div>
    <h2 style="text-align: center">覆盖率报告管理</h2>
    <el-form ref="form" :model="form" label-width="120px" label-position="right">
      <!-- <el-form-item label="应用名称">
        <el-radio-group v-model="form.appName" @change="onSelectApp">
          <el-radio label="coverage-demo">coverage-demo</el-radio>
        </el-radio-group>
      </el-form-item> -->

      <el-form-item label="应用名称">
        <el-radio-group v-model="form.appName" @change="onSelectApp">
          <el-radio
            v-for="item in appList"
            :label="item"
            :key="item.value"
          ></el-radio>
        </el-radio-group>
      </el-form-item>
   </el-form>

    <el-table :data="tableData" border fit style="width: 100%;margin:0px auto">
      <el-table-column fixed prop="date" label="日期" width="180">
      </el-table-column>

      <el-table-column prop="fileName" label="报告名称">
      </el-table-column>

      <el-table-column fixed="right" label="操作" width="180" align="center">
        <template slot-scope="scope">
          <el-button @click="openReport(scope.row)" type="text" size="small">查看报告</el-button>
          <el-button @click="downloadReport(scope.row)" type="text" size="small">下载报告</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { requestGet } from "../utils/fetch";
import { jacocoHost} from "../utils";
export default {
  data() {
    return {
      form: {
        appName: "android"
      },
      reportHostUrl:"",
      ccLivetableData: [],
      tableData: [
        // {
        //   date: "2016-05-02",
        //   basePath: "android/dev_duqian/",
        //   fileName: "dev_duqian_reprot111.zip",
        // },
      ],
      appList: ['coverage-demo'],
    };
  },
  created(){
     console.warn(`host=${jacocoHost}`);
     this.updateAppList();
     this.updateReportList(this.form.appName)
  },
  methods: {
    handleClick(row) {
      console.log(row);
    },
    onSelectApp(val){
      let that = this 
      console.log(val)
      this.updateReportList(val)
    },
    updateReportList(appName){
        requestGet(`${jacocoHost}/coverage/report/manager`, this.form).then(
        (res) => {
          let {data: {fileList=[]}} = res || {data: {}}
          this.tableData = fileList
          this.ccLivetableData = fileList
          this.reportHostUrl = res.data.reportHostUrl
        }
      ).catch((error) => {
          console.error(error);
        });
    },
    updateAppList() {
      requestGet(`${jacocoHost}/api/init`, this.form)
        .then((res) => {
          let { data = {} } = res || {};
          this.appList = data.appList;
          console.warn("/api/init");
          console.warn(this.appList);

          if(this.appList.length>0){
            this.form.appName = this.appList[0]; // 将第一个元素作为默认选项
            //updateReportList(this.appList[0])
          }
        })
        .catch((error) => {
          console.error(error);
        });
    },
    openReport(row) {
        console.log(row);
        let dir = row.fileName.replace(".zip","")
        console.log(dir);
        let reportPreviewUrl = `${this.reportHostUrl}/${row.basePath}${dir}`
        window.open(reportPreviewUrl);
        console.log(`reportPreviewUrl=${reportPreviewUrl}`);
    },
    downloadReport(row) {
        let fileName = row.fileName,
        url = `${jacocoHost}/download?path=${encodeURIComponent(row.basePath + fileName)}`,
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
        console.log(url);
        console.log(fileName);
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