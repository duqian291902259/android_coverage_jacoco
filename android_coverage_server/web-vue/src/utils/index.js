export const localHost = "http://127.0.0.1:8090"
//export const remoteHost = "http://jacoco.dev.duqian.cn"
export const remoteHost = "http://127.0.0.1:18090"
export const jacocoHost = getHostUrl()//remoteHost

function getHostUrl() {
    let origin = window.location.origin
    console.log(origin)
    if (origin.indexOf("18080")>-1) {
        console.log("origin remoteHost")
        return remoteHost;
    }
     console.log("origin localHost")
    return localHost
}

// 获取url中的参数值
export function getUrlParam(url, paramName) {
    let arrObj = url.split("?");
    if (arrObj.length > 1) {
        let arrPara = arrObj[1].split("&");
        let arr;

        for (let i = 0; i < arrPara.length; i++) {
            arr = arrPara[i].split("=");

            if (arr != null && arr[0] == paramName) {
                return arr[1];
            }
        }
        return "";
    }
    else {
        return "";
    }
}