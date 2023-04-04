// 检查文件是否已上传
const checkUpload = (id) => {
    return axios({
        url: '/task/checkUpload',
        method: 'post',
        params:{id:id}
    })
}

// 获得文件预先上传url
const getPreUrl = (params) => {
    return axios({
        url: '/task/getPreUrl',
        method: 'post',
        params:params,
        async:false
    })
}

// 合并文件
const merFile = (params) => {
    return axios({
        url: '/task/merFile',
        method: 'post',
        params:params
    })
}

// 上传文件
const uploadFile = (formData,url) => {
    return axios({
        url: url,
        method: 'put',
        headers: {'Content-Type': 'application/octet-stream'},
        data:formData
    })
}

// 得到总共切片数
const getChuckSum = (fileSize) => {
    const size = parseInt(fileSize);
    const chunkSize = 5 * 1024 *1024;
    let chunkSum = 0;
    do {
        chunkSum++
        //console.log('inside ',chunkSum);
    } while (chunkSum * chunkSize < size)
    return chunkSum;
}

// 获得切片文件上传的队列
const uploadQ = async function (file, fileName, fileId, partNumber, chuckSum) {
    const chunkSize = 5 * 1024 * 1024;
    const start = Number(chunkSize) * Number(partNumber);
    const end = start + Number(chunkSize);
    const blob = file.slice(start, end);
    //console.log(blob.size);
    const res = await getPreUrl(
        {fileId: fileId, fileName: fileName, totalPieces: chuckSum, sliceIndex: partNumber}
    );
    if(res.data.code === 200){
        const resUP = await uploadFile(
            {url: res.data.msg, file: blob}
        );
        if(resUP.status === 200){
            console.log("切片文件", partNumber, "上传成功");
            return blob.size;
        }else{
            return 0;
        }
    }else{
        return 0;
    }
}

const upF = async (url,file) =>{
    // 1. 创建 xhr 对象
    var xhr = new XMLHttpRequest();
    // 2. 调用 open 函数，指定请求类型与URL地址。其中，请求类型必须为 POST
    xhr.open('POST', url);
    xhr.setRequestHeader('Content-Type','application/octet-stream');
    // 3. 发起请求
    xhr.send(file);

    if(xhr.status === 200){
        return 'success';
    }
};

// // 断点效果
// function sleep(d){
//     for(var t = Date.now();Date.now() - t <= d;);
// }
//
// // 获取从开始上传到现在的平均速度（byte/s）
// const getSpeed = () => {
//     // 已上传的总大小 - 上次上传的总大小（断点续传）= 本次上传的总大小（byte）
//     const intervalSize = uploadedSize - lastUploadedSize
//     const nowMs = new Date().getTime()
//     // 时间间隔（s）
//     const intervalTime = (nowMs - startMs) / 1000
//     return intervalSize / intervalTime
// }
//
// // 更新上传进度
// const updateProcess = (increment) => {
//     increment = new Number(increment)
//     const { onProgress } = options
//     let factor = 1000; // 每次增加1000 byte
//     let from = 0;
//     // 通过循环一点一点的增加进度
//     while (from <= increment) {
//         from += factor
//         uploadedSize += factor
//         const percent = Math.round(uploadedSize / totalSize * 100).toFixed(2);
//         onProgress({percent: percent})
//     }
//
//     const speed = getSpeed();
//     const remainingTime = speed !== 0 ? Math.ceil((totalSize - uploadedSize) / speed) + 's' : '未知'
//     console.log('剩余大小：', (totalSize - uploadedSize) / 1024 / 1024, 'mb');
//     console.log('当前速度：', (speed / 1024 / 1024).toFixed(2), 'mbps');
//     console.log('预计完成：', remainingTime);
// }

// 控制最大上传数
const limitRequest = (urls,formDatas, maxNum,fileId,fileName,folderId,totalPieces,chuckSizes) => {

    return new Promise((resolve) => {
        if (urls.length === 0) {
            resolve([]);
            return
        }

        // 定义数组记录每次请求的结果（无论成功失败）
        const results = []
        let index = 0;
        let cnt = 0;

        // 发送请求
        async function request() {
            if (index === urls.length) {

                return;
            }

            const i = index;
            const url = urls[i];
            const formData = formDatas[i];
            const chuckSize = chuckSizes[i];
            index++;
            //console.log(url);
            const fileMd5 = fileId;
            const folderId0 = folderId;
            const totalPieces0 = totalPieces;
            const fileName0 = fileName;
            try {
                const response = await uploadFile(formData,url);
                let now = document.getElementById("now");
                let n = now.innerHTML;
                now.innerText = Number(n) + Number(chuckSize);
                change();
                results[i] = response;
            } catch (err) {
                results[i] = err;
            } finally {
                cnt++;
                // 判断是否请求都完成
                if (cnt === urls.length) {
                    console.log('所有请求已完成')
                    console.log("准备合并文件");
                    const startTime = Date.now();
                    console.log('startTime:',startTime);
                    const merge = merFile({fileId:fileMd5, folderId:folderId0, totalPieces:totalPieces0, fileName:fileName0});
                    //console.log(costTime);//两个时间相差的秒数
                    merge.then( (mergeRes) => {
                        if(mergeRes.data.code === 200){
                            const endTime = Date.now();
                            console.log('endTime:',endTime);
                            const costTime = parseInt(endTime - startTime);
                            let loc = document.getElementById("loc");
                            loc.innerHTML = mergeRes.data.data;
                            window.alert("文件合并成功~ 所花时间为："+ costTime + " ms");
                        }
                        else {
                            window.alert("文件合并失败~")
                        }
                    })
                   resolve(results)
                }
                request()
            }
        }

        // maxNum和urls.length取最小进行调用
        // 将异步的并发数控制在，同一时间请求times次
        const times = Math.min(maxNum, urls.length);
        for (let i = 0; i < times; i++) {
            request()
        }
    })
}


