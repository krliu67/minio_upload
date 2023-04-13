package com.example.demot.bean;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_upload_task")
public class Task {
    //序列号
    private static final long serialVersionUID = 1L;
    /**
     * id.
     * 数据类型:Long
     */
    @TableId("id")
    private Long  id;


    /**
     * 分片上传的uploadId.
     * 数据类型:String
     */
    private String  uploadId;


    /**
     * 文件唯一标识（md5）.
     * 数据类型:String
     */
    private String  fileIdentifier;


    /**
     * 文件名.
     * 数据类型:String
    */
    private String  fileName;


    /**
     * 所属桶名.
     * 数据类型:String
     */
    private String  bucketName;


    /**
     * 文件的key.
     * 数据类型:String
     */
    private String  objectKey;


    /**
     * 文件大小（byte）.
     * 数据类型:Long
     */
    private Long  totalSize;


    /**
     * 每个分片大小（byte）.
     * 数据类型:Long
     */
    private Long  chunkSize;


    /**
     * 分片数量.
     * 数据类型:Integer
     */
    private Integer chunkNum;

    /**
     * 文件地址，
     *  String
     */
    private String fileAddr;
}
