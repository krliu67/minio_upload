package com.example.demot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demot.mapper.TaskMapper;
import com.example.demot.service.TaskService;
import com.example.demot.bean.Task;
import com.example.demot.utils.ResultData;
import com.google.common.collect.Sets;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    private AtomicInteger counter = new AtomicInteger(0);

    //private String minioUrl = "http://localhost:9000";
    private String minioUrl = "http://X0:9000";

    private String accessKey = "admin";
    //private String accessKey = "minioadmin";

    private String secretKey = "minioadmin";

    MinioClient minioClient = MinioClient.builder().endpoint(minioUrl).credentials(accessKey, secretKey).build();

    @Override
    @CrossOrigin
    public boolean isFinished(String identifier) {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("file_identifier",identifier);
        List<Task> tasks = taskMapper.selectByMap(map);
        int count1 = tasks.size();
        if (count1 == 1) {
            // 已上传，设置后端提示
            log.info("文件已上传~");
            return true;
        }
        return false;
    }

    @Override
    @CrossOrigin
    public ResultData getUploadURL(Integer sliceIndex, Integer totalPieces, String fileId, String fileName) throws Exception {
        if (!this.minioClient.bucketExists(BucketExistsArgs.builder().bucket("temp").build())) {
            this.minioClient.makeBucket(MakeBucketArgs.builder().bucket("temp").build());
        }

        // 上传至minio 格式：/temp/fileId/
        String originalFileName = fileName.substring(0, fileName.lastIndexOf("."));
        System.out.println("originalFileName: " + originalFileName);

        // 获取文件后缀
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        System.out.println("fileType: " + fileType);

        String bucketName = "temp";
        String objectName = fileId.concat("/").concat(originalFileName).concat("_").concat(Integer.toString(sliceIndex))
                .concat(fileType);

        // 获取上传路径
        String product = this.minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                // 这里必须是PUT，如果是GET的话就是文件访问地址了。如果是POST上传会报错.
                .method(Method.PUT).bucket(bucketName).object(objectName).expiry(20, TimeUnit.MINUTES).build());
        System.out.println(product);

        return new ResultData(200, null, product, sliceIndex);
    }

    @Override
    @CrossOrigin
    public ResultData merFile(String fileId, String folderId, Integer totalPieces, String fileName) {
        try {
            String objectName = fileId;
            String bucketName = folderId;

            // 根据 folderId 设置 存入的bucket

            // 验证文件是否有缺少
            Iterable<Result<Item>> results = this.minioClient
                    .listObjects(ListObjectsArgs.builder().bucket("temp").prefix(objectName.concat("/")).build());
            Set<String> objectNames = Sets.newHashSet();
            for (Result<Item> item : results) {
                objectNames.add(item.get().objectName());
            }

            // 获取文件原名
            String originalFileName = fileName.substring(0, fileName.lastIndexOf("."));
            System.out.println("originalFileName: " + originalFileName);

            // 获取文件后缀
            String fileType = fileName.substring(fileName.lastIndexOf("."), fileName.length());
            System.out.println("fileType: " + fileType);

            // 查看未上传文件
            List<Integer> indexs = Stream
                    .iterate(0, i -> ++i).limit(totalPieces).filter(i -> !objectNames.contains(objectName.concat("/")
                            .concat(originalFileName).concat("_").concat(Integer.toString(i).concat(fileType))))
                    .sorted().collect(Collectors.toList());
            if (indexs.size() != 0) {
                return new ResultData(500, null, "文件不完整，无法合并~~", indexs.size());
            }

            // 创建 folderId 的 bucket name
            if (!this.minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                this.minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
            System.out.println("创建" + bucketName + "成功");

            // 收集文件
            List<ComposeSource> sourceObjectList = Stream.iterate(0, i -> ++i).limit(totalPieces)
                    .map(i -> ComposeSource.builder().bucket("temp")
                            .object(objectName.concat("/").concat(originalFileName).concat("_")
                                    .concat(Integer.toString(i).concat(fileType)))
                            .build())
                    .collect(Collectors.toList());

            // 设置文件名
            String targetBucket = bucketName;

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String targetObjectName = format.format(new Date()) + "_"
                    + (System.currentTimeMillis() * 100 + counter.incrementAndGet()) + "_" + fileName;

            // 合并文件
            ObjectWriteResponse response = this.minioClient.composeObject(ComposeObjectArgs.builder()
                    .bucket(targetBucket).object(targetObjectName).sources(sourceObjectList).build());
            System.out.println("合并成功！");

            // 查询合并文件
            Iterable<Result<Item>> result = this.minioClient
                    .listObjects(ListObjectsArgs.builder().bucket(bucketName).prefix(targetObjectName).build());
            long fileSize = result.iterator().next().get().size();

            // 删除缓存中的所有的分片文件
            for (Result<Item> item : results) {
                this.minioClient.removeObject(
                        RemoveObjectArgs.builder().bucket("temp").object(item.get().objectName()).build());
            }
            System.out.println("删除完毕：" + "temp/" + objectName + "；合并文件完成上传，保存文件信息到数据库");

            // 删除redis缓存
            boolean delRedis = redisTemplate.delete("uploaddemo:"+fileId);
            System.out.println("删除文件" + fileId + "，结果为：" + delRedis);

            // 测试user
            // System.out.println(user.toString());
            String product = minioUrl + "/" + bucketName + "/" + targetObjectName;
            System.out.println("文件访问地址:" + product);

            // 存入数据库中
            Task task = new Task();
            task.setBucketName(bucketName);
            task.setChunkNum(0);
            task.setChunkSize((long) 0);
            task.setFileIdentifier(fileId);
            task.setFileName(fileName);
            task.setTotalSize(fileSize);
            task.setFileAddr(product);


            if (taskMapper.insert(task) == 1) {
                return new ResultData(200, product, "文件合并成功，上传成功", 1);
            }

            // 设置失败返回值
            return new ResultData(500, targetObjectName, "文件合并失败", 1);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // 设置失败返回值
        return new ResultData(500, null, "合并失败", 0);
    }

    @Override
    @CrossOrigin
    public ResultData getFileAddr(String fileId) {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("file_identifier",fileId);
        List<Task> tasks = taskMapper.selectByMap(map);
        System.out.println("getFileAddr: " + fileId);
        try{
            String path = tasks.get(0).getFileAddr();
            System.out.println("getFileAddr: " + fileId + " 成功");
            return new ResultData(200, path, "找到了", 0);
        }catch (Exception e){
            System.out.println("getFileAddr: " + fileId + " 失败");
            return new ResultData(500, null, e.toString(), 0);
        }
    }

    @Override
    @CrossOrigin
    public ResultData saveToRedis(String fileId, Integer sliceIndex) {
        try{
            String num = sliceIndex.toString();
            Long temp = redisTemplate.opsForList().rightPush("uploaddemo:"+fileId,num);
            System.out.println("saveToRedis: " + fileId + ", "+ sliceIndex  + " 成功");
            return new ResultData(200, null, "设置成功", 0);
        }catch (Exception e){
            System.out.println("saveToRedis: " + fileId + ", "+ sliceIndex  + " 失败");
            return new ResultData(500, null, e.toString(), 0);
        }
    }

    @Override
    @CrossOrigin
    public ResultData queryRedisAndReturn(String fileId,Integer chuckNum) {
        List<Integer> lists = new ArrayList<>();
        for (int i = 0; i < chuckNum; i++) {
            lists.add(i);
        }

        try{
            Long num = redisTemplate.opsForList().size("uploaddemo:"+fileId);
            List<Object> res = redisTemplate.opsForList().range("uploaddemo:"+fileId,0,num);

            for (int i = 0; i < res.size(); i++) {
                Integer tempNum1 = Integer.parseInt(res.get(i).toString());
                if(lists.contains(tempNum1)){
                    // 如果有，就去掉
                    boolean t = lists.remove(tempNum1);
                    System.out.println(fileId + "已经上传 " + res.get(i) + "号文件" );
                }
            }
            Collections.sort(lists);
            System.out.println("queryRedisAndReturn: " + fileId + " 成功");
            return new ResultData(200, lists, "查询成功", 0);
        }catch (Exception e){
            System.out.println("queryRedisAndReturn: " + fileId + " 不存在");

            return new ResultData(200, lists, e.toString(), 0);
        }
    }
}

