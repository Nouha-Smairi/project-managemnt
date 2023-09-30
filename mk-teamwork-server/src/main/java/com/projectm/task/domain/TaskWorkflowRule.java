package com.projectm.task.domain;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.projectm.domain.BaseDomain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@TableName("team_task_workflow_rule")
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TaskWorkflowRule   extends BaseDomain implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String code;
    /**
    * Rule type, 0: task grouping, 1: person, 2: condition
    */    
    private Integer type;
    private String object_code;
    /**
    * Scenes. 0: Add task, 1: Completed, 2: Redone, 3: Set executor, (4: Deadline, 5: Priority)
    */
    private Integer action;    
    private String create_time;
    private String update_time;
    private String workflow_code;
    private Integer sort;
}
