package com.example.demot.controller;

import com.example.demot.service.TaskService;
import com.example.demot.utils.ResultData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/task")
@Api(tags = "minio上传")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping("/checkUpload")
    @ApiOperation("检查文件是否已上传")
    boolean checkUpload(String id){
        return taskService.isFinished(id);
    }

    @PostMapping("/getPreUrl")
    @ApiOperation("获得文件预先上传url")
    ResultData getPreUrl(Integer sliceIndex, Integer totalPieces, String fileId, String fileName) throws Exception {
        return taskService.getUploadURL(sliceIndex, totalPieces, fileId, fileName);
    }

    @PostMapping("/merFile")
    @ApiOperation("合并文件")
    ResultData merFile(String fileId, String folderId, int totalPieces, String fileName){
        return taskService.merFile(fileId,folderId,totalPieces,fileName);
    }
}
