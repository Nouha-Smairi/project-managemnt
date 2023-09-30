package com.projectm.task.controller;

import com.alibaba.fastjson.JSONObject;
import com.framework.common.AjaxResult;
import com.framework.common.utils.StringUtils;
import com.projectm.common.CommUtils;
import com.projectm.common.Constant;
import com.projectm.common.DateUtil;
import com.projectm.member.service.MemberAccountService;
import com.projectm.member.service.MemberService;
import com.projectm.project.service.ProjectLogService;
import com.projectm.project.service.ProjectService;
import com.projectm.project.service.SourceLinkService;
import com.projectm.task.domain.TaskStagesTemplete;
import com.projectm.task.service.*;
import com.projectm.web.BaseController;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/project")
public class TaskStageController extends BaseController {

    @Autowired
    private TaskStageService taskStageService;

    @Autowired
    private TaskWorkflowService taskWorkflowService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ProjectLogService projectLogService;

    @Autowired
    private TaskWorkTimeService taskWorkTimeService;

    @Autowired
    private FileService fileService;

    @Autowired
    private SourceLinkService sourceLinkService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TaskTagService taskTagService;

    @Autowired
    private TaskToTagService taskToTagService;

    @Autowired
    private TaskMemberService taskMemberService;
    @Autowired
    private MemberAccountService memberAccountService;
    @Autowired
    private  TaskStagesTempleteService taskStagesTempleteService;

    @PostMapping("/task_stages_template/index")
    @ResponseBody
    public AjaxResult taskStagesTemplate(@RequestParam Map<String, Object> mmap) {
    	String code = MapUtils.getString(mmap, "code");
    	if (StringUtils.isEmpty(code)) {
            return AjaxResult.warn("Please select an item");
        }
        return AjaxResult.success(Constant.createPageResultMap(taskStagesTempleteService.getTaskStagesTemplate(Constant.createPage(mmap),code)));
    }

    @PostMapping("/task_stages_template/save")
    @ResponseBody
    public AjaxResult taskStagesSave(@RequestParam Map<String,Object> mmap){
        String name = MapUtils.getString(mmap,"name");
        String template_code = MapUtils.getString(mmap,"template_code");
        Integer sort = MapUtils.getInteger(mmap,"sort",0);
        if(StringUtils.isEmpty(name)){
            return AjaxResult.warn("Please fill in the task name");
        }
        TaskStagesTemplete tst = TaskStagesTemplete.builder().code(CommUtils.getUUID()).create_time(DateUtil.getCurrentDateTime())
                .name(name).sort(sort).project_template_code(template_code).build();
        boolean result = taskStagesTempleteService.save(tst);
        if(result){
            return AjaxResult.success("Added successfully",tst);
        }else{
            return AjaxResult.error("Operation failed, please try again later!");
        }
    }

    @PostMapping("/task_stages_template/edit")
    @ResponseBody
    public AjaxResult taskStagesEdit(@RequestParam Map<String,Object> mmap){
        String name = MapUtils.getString(mmap,"name");
        String code = MapUtils.getString(mmap,"code");
        Integer sort = MapUtils.getInteger(mmap,"sort",0);
        if(StringUtils.isEmpty(code)){
            return AjaxResult.warn("Please select a task");
        }
        TaskStagesTemplete tst = taskStagesTempleteService.getTaskStageTempleteByCode(code);
        if(ObjectUtils.isEmpty(tst)){
            return AjaxResult.warn("This task has expired");
        }
        tst.setName(name);
        tst.setSort(sort);
        boolean result = taskStagesTempleteService.updateById(tst);
        if(result){
            return AjaxResult.success("Edit task succeeded",tst);
        }else{
            return AjaxResult.error("Operation failed, please try again later!");
        }
    }
    @PostMapping("/task_stages_template/delete")
    @ResponseBody
    public AjaxResult taskStagesDel(@RequestParam Map<String,Object> mmap){
        String code = MapUtils.getString(mmap,"code");
        if(StringUtils.isEmpty(code)){
            return AjaxResult.warn("Please select a task");
        }
        TaskStagesTemplete tst = taskStagesTempleteService.getTaskStageTempleteByCode(code);
        if(ObjectUtils.isEmpty(tst)){
            return AjaxResult.warn("The template does not exist");
        }
        boolean result = taskStagesTempleteService.removeById(tst.getId());
        if(result){
            return AjaxResult.success("Successfully deleted task",tst);
        }else{
            return AjaxResult.error("Operation failed, please try again later!");
        }
    }
    
        /**
         * Save task flow
         *
         * @return
         */
        @PostMapping("/task_workflow/save")
        @ResponseBody
        public AjaxResult save(String projectCode, String taskWorkflowName, String taskWorkflowRules) {
            String orgCode = getOrgCode();
            JSONObject rules = JSONObject.parseObject(taskWorkflowRules);
            taskWorkflowService.save(orgCode, projectCode, taskWorkflowName, rules);
            return AjaxResult.success("Saved successfully");
        }
    
        /**
         * Edit task flow
         *
         * @return
         */
        @PostMapping("/task_workflow/edit")
        @ResponseBody
        public AjaxResult edit(String taskWorkflowCode, String taskWorkflowName, String taskWorkflowRules) {
            JSONObject rules = JSONObject.parseObject(taskWorkflowRules);
            taskWorkflowService.update(taskWorkflowCode, taskWorkflowName, rules);
            return AjaxResult.success("Successfully modified");
        }
    
        /**
         * Delete task flow
         *
         * @return
         */
        @PostMapping("/task_workflow/delete")
        @ResponseBody
        public AjaxResult delete(String taskWorkflowCode) {
            taskWorkflowService.delete(taskWorkflowCode);
            return AjaxResult.success("successfully deleted");
        }
     
}
