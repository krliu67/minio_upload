package com.example.demot.bean;

import lombok.Data;

@Data
public class TestVo {

    private String fileId;
    private String fileName;
    private Integer sliceIndex;
    private Integer chuckNum;
    private Integer totalPieces;
}
