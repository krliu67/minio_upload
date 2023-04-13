package com.example.demot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demot.bean.Task;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Mapper
@Component
@Repository
public interface TaskMapper extends BaseMapper<Task> {
    Task findByFileId(String id);

}
