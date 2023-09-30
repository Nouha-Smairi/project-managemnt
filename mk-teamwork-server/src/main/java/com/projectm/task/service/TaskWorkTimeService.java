package com.projectm.task.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.projectm.task.domain.TaskWorkTime;
import com.projectm.task.mapper.TaskWorkTimeMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TaskWorkTimeService  extends ServiceImpl<TaskWorkTimeMapper, TaskWorkTime> {

    //Obtain taskworktime according to taskCode
    public List<Map> getTaskWorkTimeByTaskCode(String taskCode){
        return baseMapper.selectTaskWorkTimeByTaskCode(taskCode);
    }
    //Obtain taskworktime according to code
    public Map getTaskWorkTimeByCode(String code){
        return baseMapper.selectTaskWorkTimeByCode(code);
    }
    //Delete taskworktime according to code
    public Integer delTaskWorkTimeByCode(String code){
        return baseMapper.deleteTaskWorkTimeByCode(code);
    }
}
