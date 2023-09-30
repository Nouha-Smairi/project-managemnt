package com.projectm.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.framework.common.utils.StringUtils;
import com.projectm.common.CommUtils;
import com.projectm.common.DateUtil;
import com.projectm.common.DateUtils;
import com.projectm.member.domain.Member;
import com.projectm.member.service.MemberService;
import com.projectm.project.domain.Project;
import com.projectm.project.domain.ProjectLog;
import com.projectm.project.mapper.ProjectLogMapper;
import com.projectm.system.domain.Notify;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.smartcardio.CommandAPDU;
import java.util.Map;

@Service
public class ProjectLogService   extends ServiceImpl<ProjectLogMapper, ProjectLog> {

    @Autowired
    ProjectService projectService;
    @Autowired
    MemberService memberService;
    

    public IPage<Map> getProjectLogByParam(IPage<Map> ipage,Map params){
	    return baseMapper.selectProjectLogByParam(ipage,params);
	}

	public Project run(Map param){
        ProjectLog projectLog = ProjectLog.builder().action_type(MapUtils.getString(param,"action_type")).code(CommUtils.getUUID())
                .create_time(DateUtil.getCurrentDateTime()).to_member_code(MapUtils.getString(param,"to_member_code"))
                .is_comment(MapUtils.getInteger(param,"is_comment")).content(MapUtils.getString(param,"content",""))
                .type(MapUtils.getString(param,"type")).source_code(MapUtils.getString(param,"source_code"))
                .member_code(MapUtils.getString(param,"member_code")).project_code(MapUtils.getString(param,"project_code")).build();
        Project project = projectService.getProjectProjectByCode(projectLog.getProject_code());
        projectLog.setProject_code(project.getCode());
        Member toMember = new Member();
        if(StringUtils.isNotEmpty(projectLog.getTo_member_code())){
            toMember = memberService.getMemberByCode(projectLog.getTo_member_code());
        }
        Notify notify = new Notify();
        String type = projectLog.getType();
        if("create".equals(type)){
            projectLog.setIcon("plus");
            projectLog.setRemark("created the project");
            projectLog.setContent(project.getName());
        }else if("edit".equals(type)){
            projectLog.setIcon("edit");
            projectLog.setRemark("edited item");
            projectLog.setContent(project.getName());
        }else if("name".equals(type)){
            projectLog.setIcon("edit");
            projectLog.setRemark("changed the project name");
            projectLog.setContent(project.getName());
        }else if("content".equals(type)){
            projectLog.setIcon("file-text");
            projectLog.setRemark("updated notes");
            projectLog.setContent(project.getDescription());
        }else if("clearContent".equals(type)){
            projectLog.setIcon("file-text");
            projectLog.setRemark("notes cleared");
        }else if("inviteMember".equals(type)){
            projectLog.setIcon("user-add");
            projectLog.setRemark("invite"+toMember.getName()+"join project");
            projectLog.setContent(toMember.getName());
        }else if("removeMember".equals(type)){
            projectLog.setIcon("user-delete");
            projectLog.setRemark("member removed"+toMember.getName());
            projectLog.setContent(toMember.getName());
        }else if("recycle".equals(type)){
            projectLog.setIcon("delete");
            projectLog.setRemark("moved item to trash");
        }else if("recovery".equals(type)){
            projectLog.setIcon("undo");
            projectLog.setRemark("restored the project");
        }else if("archive".equals(type)){
            projectLog.setIcon("delete");
            projectLog.setRemark("archived item");
        }else if("recoveryArchive".equals(type)){
            projectLog.setIcon("undo");
            projectLog.setRemark("restored the project");
        }else if("uploadFile".equals(type)){
            projectLog.setIcon("link");
            projectLog.setRemark("file uploaded");
            projectLog.setContent("<a target=\"_blank\" class=\"muted\" href=\""+MapUtils.getString(param,"url")+" \">\""+MapUtils.getString(param,"title")+"</a>");
        }else if("deleteFile".equals(type)){
            projectLog.setIcon("disconnect");
            projectLog.setRemark("deleted file");
            projectLog.setContent("<a target=\"_blank\" class=\"muted\" href=\""+MapUtils.getString(param,"url")+" \">\""+MapUtils.getString(param,"title")+"</a>");
        }else{
            projectLog.setIcon("plus");
            projectLog.setRemark("created the file");
        }
        baseMapper.insert(projectLog);
        return project;
    }


}
