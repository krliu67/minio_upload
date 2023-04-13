package com.example.demot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demot.bean.Task;
import com.example.demot.utils.ResultData;

public interface TaskService extends IService<Task> {
    boolean isFinished(String identifier);
    ResultData getUploadURL(Integer sliceIndex, Integer totalPieces, String fileId, String fileName) throws Exception;
    ResultData merFile(String fileId,String folderId,Integer totalPieces,String fileName);
    ResultData getFileAddr(String fileId);
    ResultData saveToRedis(String fileId,Integer sliceIndex);
    ResultData queryRedisAndReturn(String fileId,Integer chuckNum);
}
