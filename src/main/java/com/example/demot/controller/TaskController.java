package com.example.demot.controller;

import com.example.demot.bean.TestVo;
import com.example.demot.service.TaskService;
import com.example.demot.utils.ResultData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/task")
@Api(tags = "minio上传")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping("/checkUpload")
    @ApiOperation("检查文件是否已上传")
    @CrossOrigin
    boolean checkUpload(String id){
        return taskService.isFinished(id);
    }

    @PostMapping("/getPreUrl")
    @ApiOperation("获得文件预先上传url")
    @CrossOrigin
    ResultData getPreUrl(Integer sliceIndex, Integer totalPieces, String fileId, String fileName) throws Exception {
        // 保存块号文件记录的路径，其中 key 为上传文件的 md5 + 一个固定前缀，value 为块号文件的记录路径
        return taskService.getUploadURL(sliceIndex, totalPieces, fileId, fileName);
    }

    @PostMapping("/merFile")
    @ApiOperation("合并文件")
    @CrossOrigin
    ResultData merFile(String fileId, String folderId, int totalPieces, String fileName){
        return taskService.merFile(fileId,folderId,totalPieces,fileName);
    }

    @PostMapping("/getFileAddr")
    @ApiOperation("获得文件上传地址")
    @CrossOrigin
    ResultData getFileAddr(@RequestBody TestVo testVo){
        return taskService.getFileAddr(testVo.getFileId());
    }

    @PostMapping("/saveToRedis")
    @ApiOperation("上传文件的记录存入redis")
    @CrossOrigin
    ResultData saveToRedis(@RequestBody TestVo testVo){
        return taskService.saveToRedis(testVo.getFileId(),testVo.getSliceIndex());
    }

//    @PostMapping("/queryRedisAndReturn")
//    @ApiOperation("查询redis的文件记录")
//    @CrossOrigin
//    ResultData queryRedisAndReturn(@RequestParam String fileId, @RequestParam(defaultValue = "0")int chuckNum){
//        return taskService.queryRedisAndReturn(fileId,chuckNum);
//    }

    @PostMapping("/queryRedisAndReturn")
    @ApiOperation("查询redis的文件记录")
    @CrossOrigin
    ResultData queryRedisAndReturn(@RequestBody TestVo testVo){
        return taskService.queryRedisAndReturn(testVo.getFileId(),testVo.getChuckNum());
    }
}
