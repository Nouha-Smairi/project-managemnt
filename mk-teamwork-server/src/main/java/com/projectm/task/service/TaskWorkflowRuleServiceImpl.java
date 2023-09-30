package com.projectm.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.projectm.task.domain.TaskWorkflowRule;
import com.projectm.task.mapper.TaskWorkflowRuleMapper;
import org.springframework.stereotype.Service;

/**
 * @version V1.0
 * @program: teamwork
 * @package: com.projectm.task.service
 * @description: Flow rule business processing class
 * @create: 2020-07-20 23:12
 **/
@Service
public class TaskWorkflowRuleServiceImpl extends ServiceImpl<TaskWorkflowRuleMapper, TaskWorkflowRule> implements IService<TaskWorkflowRule> {
}