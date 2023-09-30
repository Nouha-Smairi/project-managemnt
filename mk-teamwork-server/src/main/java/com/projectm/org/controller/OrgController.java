package com.projectm.org.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.framework.common.AjaxResult;
import com.framework.common.utils.ServletUtils;
import com.framework.common.utils.StringUtils;
import com.framework.utils.FileUtils;
import com.projectm.common.CommUtils;
import com.projectm.common.Constant;
import com.projectm.common.DateUtil;
import com.projectm.config.MProjectConfig;
import com.projectm.member.service.MemberService;
import com.projectm.org.domain.Department;
import com.projectm.org.domain.Organization;
import com.projectm.org.service.DepartmentMemberService;
import com.projectm.org.service.DepartmentService;
import com.projectm.org.service.OrgService;
import com.projectm.org.service.OrganizationService;
import com.projectm.task.controller.TaskFileController;
import com.projectm.web.BaseController;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.*;

@RestController
@RequestMapping("/project")
public class OrgController   extends BaseController {

    @Autowired
    private OrgService orgService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private MemberService memberService;
    @Autowired
    private DepartmentMemberService departmentMemberService;

    /**
     * Add new preservation department
     * @param mmap
     * @return
     */
    @PostMapping("/department/save")
    @ResponseBody
    public AjaxResult departmentSave(@RequestParam Map<String,Object> mmap)
    {
        Map loginMember = getLoginMember();
        String organizationCode = MapUtils.getString(loginMember,"organizationCode");
        String departmentCode = MapUtils.getString(mmap,"departmentCode");
        String parentDepartmentCode = MapUtils.getString(mmap,"parentDepartmentCode");
        String name = MapUtils.getString(mmap,"name");
        if(StringUtils.isEmpty(name)){
            return AjaxResult.warn("Please fill in the department name");
        }
        Department dep = Department.builder().code(CommUtils.getUUID()).create_time(DateUtil.formatDateTime(new Date()))
                .name(name).organization_code(organizationCode).pcode(parentDepartmentCode).build();
        boolean result = departmentService.save(dep);
        if(result){
            return AjaxResult.success(dep);
        }
        return AjaxResult.warn("Operation failed, please try again later！");

    }


    @PostMapping("/department/index")
    @ResponseBody
    public AjaxResult getDepartment(@RequestParam Map<String,Object> mmap)
    {
        Map loginMember = getLoginMember();
        String organizationCode = MapUtils.getString(loginMember,"organizationCode");
        String pCode = MapUtils.getString(mmap,"pcode","");
        IPage<Map> ipage = departmentService.getDepartmentByOrgCodeAndPCode(Constant.createPage(mmap),organizationCode,pCode);
        return AjaxResult.success(Constant.createPageResultMap(ipage));
    }

    @PostMapping("/department/read")
    @ResponseBody
    public AjaxResult departmentRead(@RequestParam Map<String,Object> mmap)
    {
        String departmentCode = MapUtils.getString(mmap,"departmentCode");
        Map deptMap = departmentService.getDepartmentByCode(departmentCode);

        Map resultData = CommUtils.getMapField(deptMap,new String[]{ "code","organization_code","name","sort","pcode","icon","create_time","path"});

        return new AjaxResult(AjaxResult.Type.SUCCESS, "", resultData);

    }
    @PostMapping("/department/delete")
    @ResponseBody
    public AjaxResult departmentDelete(@RequestParam Map<String,Object> mmap)
    {
        String departmentCode = MapUtils.getString(mmap,"departmentCode");
        Integer resultData = departmentService.delDepartmentByCodes(new ArrayList<String>(){{add(departmentCode);}});
        return new AjaxResult(AjaxResult.Type.SUCCESS, "", resultData);

    }
    @PostMapping("/department/edit")
    @ResponseBody
    public AjaxResult departmentEdit(@RequestParam Map<String,Object> mmap)
    {
        String departmentCode = MapUtils.getString(mmap,"departmentCode");
        String parentDepartmentCode = MapUtils.getString(mmap,"parentDepartmentCode");
        String name = MapUtils.getString(mmap,"name");
        Map depMap = departmentService.getDepartmentByCode(departmentCode);
        Department dep = new Department();
        dep.setId(MapUtils.getInteger(depMap,"id"));
        dep.setName(name);
        boolean resultData = departmentService.updateById(dep);
        return new AjaxResult(AjaxResult.Type.SUCCESS, "", resultData);
    }


    @PostMapping("/organization/_getOrgList")
    @ResponseBody
    public AjaxResult getOrgList()
    {
        Map memberMap = getLoginMember();
        return AjaxResult.success(memberService.getOrgList(MapUtils.getString(memberMap,"memberCode")));
    }

    /**
     * project management   project list    My organization page initialization
     * @param mmap
     * @return
     */
    @PostMapping("/organization/index")
    @ResponseBody
    public AjaxResult getOrganization(@RequestParam Map<String,Object> mmap)
    {
        Map loginMember = getLoginMember();
        String memberCode = MapUtils.getString(loginMember,"memberCode");
        IPage<Map> ipage = Constant.createPage(mmap);
        IPage<Map> orgData = organizationService.getAllOrganizationByMemberCode(ipage,memberCode);

        return new AjaxResult(AjaxResult.Type.SUCCESS, "", Constant.createPageResultMap(orgData));
    }

    /**
     * project management  project list    My organization page initialization
     * @param mmap
     * @return
     */
    @PostMapping("/organization/edit")
    @ResponseBody
    public AjaxResult orgEdit(@RequestParam Map<String,Object> mmap)
    {

        String name = MapUtils.getString(mmap,"name","");
        String address = MapUtils.getString(mmap,"address","");
        Integer areas = MapUtils.getInteger(mmap,"areas",-1);
        String organizationCode = MapUtils.getString(mmap,"organizationCode","");

        Organization org = organizationService.getOrganizationByCode(organizationCode);
        if(!ObjectUtils.isEmpty(org)&& !ObjectUtils.isEmpty(org.getId())){
            org.setName(name);
            org.setAddress(address);
            if(areas != -1){
                org.setArea(areas);
            }
            boolean updateResult = organizationService.updateById(org);
            return new AjaxResult(AjaxResult.Type.SUCCESS, "", updateResult);
        }
        return new AjaxResult(AjaxResult.Type.SUCCESS, "Organization does not exist",false);
    }

    //@GetMapping("/department_member/_downloadTemplate")
    //@ResponseBody
    public void downloadTemplate(HttpServletResponse response) {
        String headInfo = "Name:required\n" +
                "E-mail: required, if you have already registered, you will be directly invited to join the organization, if you have not registered, a new account will be created\n" +
                "Department:ptional，Upper and lower departments use English'/'separated；multiple departements in english';'separated.like\"development department/mobile group;R&D department/Design group\"\n" +
                "default password：optional，if it is a new account ,it can be set，at least 6 characters long\n" +
                "Note: Items marked with * are required.";
        String[] head = {"name*", "mail*", "department", "position", "cell phone", "default password", "describe"};
        String fileName = "Bulk import member templates";
        TaskFileController.downFile(response, headInfo, head, fileName);
    }

    @GetMapping("/department_member/_downloadTemplate")
    @ResponseBody
    public void downloadTemplate(@RequestParam Map<String,Object> mmap)
    {
        try
        {
            String filePath = MProjectConfig.getProfile() +"/template/importMember.xlsx";

            ServletUtils.getResponse().setCharacterEncoding("utf-8");
            ServletUtils.getResponse().setContentType("multipart/form-data");
            ServletUtils.getResponse().setHeader("Content-Disposition",
                    "attachment;fileName=" + FileUtils.setFileDownloadHeader(ServletUtils.getRequest(), "批量导入成员模板.xlsx"));
            FileUtils.writeBytes(filePath, ServletUtils.getResponse().getOutputStream());
        }
        catch (Exception e)
        {

        }
    }

    @PostMapping("/department_member/uploadFile")
    public AjaxResult uploadFile(@RequestParam Map<String,Object> mmap, MultipartFile file)throws Exception
    {
        InputStream ins = file.getInputStream();
        List<String> list = null;
        String rst = "";
        if(ins != null){
            departmentMemberService.uploadFile(ServletUtils.getHeaderParam("organizationCode"),ins);
        }
        return AjaxResult.success();
    }

    /**
     * Team Members - Query Users
     * @param request ask
     * @param keyword email or username
     * @param departmentCode department code
     * @return
     */
    @PostMapping("/department_member/searchInviteMember")
    public AjaxResult searchInviteMember(HttpServletRequest request, String keyword, String departmentCode) {
        String organizationcode = request.getHeader("organizationcode");
        return AjaxResult.success(orgService.searchInviteMember(organizationcode, keyword, departmentCode));
    }

    /**
     * Add users (from department and from organization)
     * @param accountCode organization account code
     * @param departmentCode department code
     * @return
     */
    @PostMapping("/department_member/inviteMember")
    public AjaxResult inviteMember(HttpServletRequest request, String accountCode, String departmentCode) {
        String organizationcode = request.getHeader("organizationcode");
        return AjaxResult.success("", orgService.inviteMember(organizationcode, accountCode, departmentCode));
    }

    /**
     * Remove users (remove from department and remove from organization)
     * @param accountCode organization account code
     * @param departmentCode department code
     * @return
     */
    @PostMapping("/department_member/removeMember")
    public AjaxResult removeMember(HttpServletRequest request, String accountCode, String departmentCode) {
        String organizationcode = request.getHeader("organizationcode");
        return AjaxResult.success("", orgService.removeMember(organizationcode, accountCode, departmentCode));
    }

    /**
     * switch current organization
     * @param orgCode organize code
     * @return
     */
    @PostMapping("/index/changeCurrentOrganization")
    public AjaxResult changeCurrentOrganization(String orgCode) {

        return AjaxResult.success("", orgService.getCurrentUserMenu(orgCode));
    }

}
