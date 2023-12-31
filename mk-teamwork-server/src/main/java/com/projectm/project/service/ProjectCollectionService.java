package com.projectm.project.service;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.framework.common.exception.CustomException;
import com.framework.utils.StringUtils;
import com.projectm.common.DateUtil;
import com.projectm.project.domain.Project;
import com.projectm.project.domain.ProjectCollection;
import com.projectm.project.mapper.ProjectCollectionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;

@Service
public class ProjectCollectionService extends ServiceImpl<ProjectCollectionMapper, ProjectCollection> {

    @Autowired
    ProjectService projectService;
    //add to favorites
    public ProjectCollection collect(ProjectCollection pc){
        int i = baseMapper.insert(pc);
        return pc;
    }
    public boolean collect(String memberCode,String projectCode,String type){
        type = StringUtils.isEmpty(type)?"collect":type;
        Project project = projectService.lambdaQuery().eq(Project::getCode,projectCode).eq(Project::getDeleted,0).one();
        if(ObjectUtils.isEmpty(project)){
            throw new CustomException("This project has expired");
        }
        ProjectCollection hasCollected = lambdaQuery().eq(ProjectCollection::getMember_code,memberCode).eq(ProjectCollection::getProject_code,projectCode).one();
        if("collect".equals(type)){
            if(ObjectUtil.isNotEmpty(hasCollected)){
                throw new CustomException("This item has been collected");
            }
            ProjectCollection projectCollection = ProjectCollection.builder().member_code(memberCode).project_code(projectCode).create_time(DateUtil.getCurrentDateTime()).build();
            return save(projectCollection);
        }else{
            if(ObjectUtil.isEmpty(hasCollected)){
                throw new CustomException("This item has not been favorited yet");
            }
            return lambdaUpdate().eq(ProjectCollection::getMember_code,memberCode).eq(ProjectCollection::getProject_code,projectCode).remove();
        }
    }
    //cancel favorite

    public int cancel(ProjectCollection pc){
        UpdateWrapper updateWrapper = new UpdateWrapper();
        updateWrapper.eq("member_code",pc.getMember_code());
        updateWrapper.eq("project_code",pc.getProject_code());
        return baseMapper.delete(updateWrapper);
    }

    //Obtain collection records according to projectCode and memberCode
    public List<Map> getProjectCollection(String projectCode, String memberCode){
        LambdaQueryWrapper<ProjectCollection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectCollection::getMember_code, memberCode);
        queryWrapper.eq(ProjectCollection::getProject_code, projectCode);
        return baseMapper.selectProjectCollection(projectCode,memberCode);
    }
}
