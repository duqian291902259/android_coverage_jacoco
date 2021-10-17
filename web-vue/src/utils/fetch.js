var qs = require('qs');
const axios = require('axios');

function requestPost(url, params) {
    return new Promise((resolve, reject)=>{
        // 配置post的请求头
        //axios.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded'
        var str = qs.stringify(params);
        console.warn(`requestPost=${str}`)
        // var urlSearchParams = new URLSearchParams();
        // urlSearchParams.append('appName', 'cc-android');
        // urlSearchParams.append('branch', 'dev');
        axios.post(url, str).then(function (response) {
            resolve(response);
        })
        .catch(function (error) {
            reject(error);
        });
    })
}

function requestGet(url, params) {
    return new Promise((resolve, reject)=>{
        axios.get(url, {
            params:{...params}
        }).then(function (response) {
            resolve(response);
        })
        .catch(function (error) {
            reject(error);
        });
    })
}
export {requestPost, requestGet}

