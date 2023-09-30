package com.projectm.project.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.framework.common.AjaxResult;
import com.framework.common.utils.StringUtils;
import com.projectm.common.BeanMapUtils;
import com.projectm.common.CommUtils;
import com.projectm.common.DateUtil;
import com.projectm.project.domain.ProjectVersion;
import com.projectm.project.domain.ProjectVersionLog;
import com.projectm.project.mapper.ProjectVersionLogMapper;
import com.projectm.project.mapper.ProjectVersionMapper;
import com.projectm.task.domain.Task;
import com.projectm.task.mapper.TaskMapper;
import com.projectm.task.service.TaskService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import oshi.util.MapUtil;

import java.util.*;

@Service
public class ProjectVersionService  extends ServiceImpl<ProjectVersionMapper, ProjectVersion> {

    @Autowired
    private ProjectVersionLogMapper projectVersionLogMapper;
    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskService taskService;

    @Transactional
    public Integer addVersionTask(List<Task> tasks, List<ProjectVersion> projectVersions){
        Integer i1= 0,i2=0;
        for(Task t:tasks){
            taskMapper.insert(t);
            i1++;
        }
        for(ProjectVersion pv:projectVersions){
            baseMapper.insert(pv);
            i2++;
        }
        return i1;
    }

    //According to the completion of the task, obtain the progress of the task
    public Integer getScheduleByVersion(String versionCode){
        Map projectVersion = baseMapper.selectProjectVersionByCode(versionCode);
        List<Map> taskList = taskMapper.selectTaskListByVersionAndDelete(new HashMap(){{
            put("versionCode",versionCode);
            put("deleted",0);
        }});
        Integer doneTotal = 0;
        if(CollectionUtils.isNotEmpty(taskList)){
            for(Map taskMap:taskList){
                if(MapUtils.getInteger(taskMap,"done",0) > 0){
                    doneTotal ++;
                }
            }
            return  Math.round(doneTotal/taskList.size() * 100);
        }
        return 0;
    }

    public Map gettProjectVersionByNameAndFeaturesCode(String name,String featuresCode){
        return baseMapper.selectProjectVersionByNameAndFeaturesCode(name,featuresCode);
    }

    public List<Map> getProjectVersion(String featuresCode){
        return baseMapper.selectProjectVersionByFeaturesCode(featuresCode);
    }

    public Map getProjectVersionByCode(String code){
        return baseMapper.selectProjectVersionByCode(code);
    }

    @Transactional
    public Integer addProjectVersionAndVersionLog(ProjectVersion pv, ProjectVersionLog pvl){
        Integer i = baseMapper.insert(pv);
        Integer j = projectVersionLogMapper.insert(pvl);
        return i+j;
    }

    @Transactional
    public Integer delProjectVersion(String versionCode){
        Integer i1 = baseMapper.deleteProjectVersionByCode(versionCode);
        Integer i2 = taskMapper.updateTaskFeaAndVerByVerCode(versionCode);
        return  i1+i2;
    }
    public ProjectVersion getPVByCode(String versionCode){
        LambdaQueryWrapper<ProjectVersion> pvQw = new LambdaQueryWrapper<>();
        pvQw.eq(ProjectVersion::getCode,versionCode);
        return baseMapper.selectOne(pvQw);
    }

    @Transactional
    public Task addVersionTask(Map taskMap, Map versionMap) throws Exception {
        String versionCode = MapUtils.getString(versionMap,"code","0");
        Task task = BeanMapUtils.mapToBean(taskMap,Task.class);
        task.setVersion_code(versionCode);
        task.setFeatures_code(MapUtils.getString(versionMap,"features_code"));
        taskService.updateById(task);
        updateSchedule(versionCode);
        return task;
    }

    @Transactional
    public Task removeVersionTask(Task task,String memberCode,String versionCode){
        if(StringUtils.isNotEmpty(versionCode)){
            task.setVersion_code("");
            task.setFeatures_code("");
            taskService.updateById(task);
            updateSchedule(versionCode);
            run(new HashMap(){{
                put("memberCode",memberCode);
                put("versionCode",versionCode);
                put("type","removeVersionTask");
                put("data",task.getName());
            }});
        }
        return task;
    }


    public void  updateSchedule(String versionCode){
        //Map projectVersionMap = baseMapper.selectProjectVersionByCode(versionCode);
        ProjectVersion pv = getPVByCode(versionCode);
        List<Map> listTaskMap = taskService.getTaskListByVersionAndDelete(new HashMap(){{
            put("versionCode",versionCode);
            put("deleted",0);
        }});
        Integer doneTotal = 0;
        if(CollectionUtils.isNotEmpty(listTaskMap)){
            for(Map map:listTaskMap){
                if(MapUtils.getInteger(map,"done",0)>0){
                    doneTotal++;
                }
            }
            int size = listTaskMap.size();
            size = size>0?size:1;
            int schedule = (int)Math.floor(doneTotal/size * 100);
            float f1 = (float)((float)doneTotal/(float)size)*100;
            pv.setSchedule((int)f1);
            updateById(pv);
        }
    }

    @Autowired
    ProjectVersionLogService projectVersionLogService;

    /**
     * memberCode
     * versionCode
     * remark
     * type
     * content
     * @param map
     */
    public void run(Map map){
        ProjectVersionLog pvl = ProjectVersionLog.builder().code(CommUtils.getUUID())
                .member_code(MapUtils.getString(map,"memberCode"))
                .source_code(MapUtils.getString(map,"versionCode"))
                .remark(MapUtils.getString(map,"remark"))
                .type(MapUtils.getString(map,"type"))
                .content(MapUtils.getString(map,"content"))
                .create_time(DateUtil.formatDateTime(new Date()))
                .build();
        Map versionMap = getProjectVersionByCode(MapUtils.getString(map,"versionCode"));
        pvl.setFeatures_code(MapUtils.getString(versionMap,"features_code"));
        String remark="",content="",icon = "";
        String type = MapUtils.getString(map,"type");
        if("create".equals(type)){
            icon = "plus";
            remark="Created version";
            content = MapUtils.getString(versionMap,"name");
        }else if("status".equals(type)){
            icon = "check-square";
            remark="Updated status to"+getStatusTextAttr(MapUtils.getString(versionMap,"status"));
        }else if("publish".equals(type)){
            icon = "check-square";
            remark="The completed version is "+MapUtils.getString(versionMap,"publish_time");
        }else if("name".equals(type)){
            icon = "edit";
            remark="updated version name";
            content = MapUtils.getString(versionMap,"name");
        }else if("content".equals(type)){
            icon = "file-text";
            remark="更新了备注";
            content = MapUtils.getString(versionMap,"description");
        }else if("clearContent".equals(type)){
            icon = "file-text";
            remark="Notes cleared ";
        }else if("setStartTime".equals(type)){
            icon = "calendar";
            remark="The update starts at " + MapUtils.getString(versionMap,"start_time");
        }else if("clearStartTime".equals(type)){
            icon = "calendar";
            remark="start time cleared ";
        }else if("setPlanPublishTime".equals(type)){
            icon = "calendar";
            remark="Updates are scheduled for release at " + MapUtils.getString(versionMap,"plan_publish_time");
        }else if("clearPlanPublishTime".equals(type)){
            icon = "calendar";
            remark="Scheduled release time cleared ";
        }else if("delete".equals(type)){
            icon = "delete";
            remark="deleted version ";
        }else if("addVersionTask".equals(type)){
            List<String> list = (ArrayList)map.get("data");
            content = StringUtils.join(list,",");
            icon="link";
            remark = "Added"+list.size()+"Item posting";
        }else if("removeVersionTask".equals(type)){
            icon = "disconnect";
            remark="Post removed";
            content = MapUtils.getString(map,"data");
        }else{
            icon = "plus";
            remark="Created version";
        }
        pvl.setIcon(icon);
        pvl.setRemark(remark);
        pvl.setContent(content);
        if(!CommUtils.isEmpty(MapUtils.getString(map,"remark"))){
            pvl.setRemark(remark);
        }
        if(!CommUtils.isEmpty(MapUtils.getString(map,"content"))){
            pvl.setContent(content);
        }
        projectVersionLogService.save(pvl);
    }

    public String getStatusTextAttr(String status){
        //state. 0: Not started, 1: In progress, 2: Delayed release, 3: Released
        if(null == status){
            return "-";
        }
        switch (Integer.parseInt(status)){
            case 0:
                return "has not started";
            case 1:
                return "in progress";
            case 2:
                return "Postponed";
            case 3:
                return "Published";
        }
        return "-";
    }

}
