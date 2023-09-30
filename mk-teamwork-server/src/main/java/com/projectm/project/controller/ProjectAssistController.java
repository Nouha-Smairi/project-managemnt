package com.projectm.project.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.framework.common.utils.DateUtils;
import com.framework.common.utils.StringUtils;
import com.framework.common.AjaxResult;
import com.projectm.common.BeanMapUtils;
import com.projectm.common.CommUtils;
import com.projectm.common.Constant;
import com.projectm.common.DateUtil;
import com.projectm.config.MProjectConfig;
import com.projectm.member.domain.Member;
import com.projectm.member.service.MemberService;
import com.projectm.project.domain.*;
import com.projectm.project.service.ProjectFeaturesService;
import com.projectm.project.service.ProjectInfoService;
import com.projectm.project.service.ProjectVersionLogService;
import com.projectm.project.service.ProjectVersionService;
import com.projectm.task.domain.Task;
import com.projectm.task.service.FileService;
import com.projectm.task.service.TaskService;
import com.projectm.web.BaseController;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.*;

@RestController
@RequestMapping("/project")
public class ProjectAssistController  extends BaseController {
    @Autowired
    private FileService fileService;
    @Autowired
    private ProjectInfoService projectInfoService;
    @Autowired
    private ProjectFeaturesService projectFeaturesService;

    @Autowired
    private ProjectVersionService projectVersionService;

    @Autowired
    private MemberService memberService;
    @Autowired
    private TaskService taskService;

    @Autowired
    private ProjectVersionLogService projectVersionLogService;


    /**edit repository
     * @param
     * @return
     */
    @PostMapping("/project_features/edit")
    @ResponseBody
    public AjaxResult getProjectVersionEdit(@RequestParam Map<String,Object> mmap){
        String featuresCode = MapUtils.getString(mmap,"featuresCode");
        String projectCode = MapUtils.getString(mmap,"projectCode");
        String description = MapUtils.getString(mmap,"description");
        String name = MapUtils.getString(mmap,"name");
        if(CommUtils.isEmpty(name)){
            return AjaxResult.warn("Please fill in the repository name");
        }
        if(CommUtils.isEmpty(featuresCode)){
            return AjaxResult.warn("Please select a repository");
        }
        Map m = projectFeaturesService.getProjectFeaturesOneByNameAndProjectCode(name,projectCode);
        if(MapUtils.isNotEmpty(m)){
            return AjaxResult.warn("This repository already exists with the name");
        }
        m = projectFeaturesService.getProjectFeaturesByCode(featuresCode);
        ProjectFeatures pf = new ProjectFeatures();
        pf.setId(MapUtils.getInteger(m,"id"));
        pf.setName(name);pf.setDescription(description);
        return AjaxResult.success(projectFeaturesService.updateById(pf));

    }

    /**delete repository
     * @param
     * @return
     */
    @PostMapping("/project_features/delete")
    @ResponseBody
    public AjaxResult getProjectVersionDelete(@RequestParam Map<String,Object> mmap){
        String featuresCode = MapUtils.getString(mmap,"featuresCode");
        if(CommUtils.isEmpty(featuresCode)){
            return AjaxResult.warn("Please select a repository");
        }
        return AjaxResult.success(projectFeaturesService.delProjectFeaturesAndTask(featuresCode));

    }

    /**
     * @param Create project repository
     * @return
     */
    @PostMapping("/project_features/save")
    @ResponseBody
    public AjaxResult projectProjectFeaturesSave(@RequestParam Map<String,Object> mmap){
        String projectCode = MapUtils.getString(mmap,"projectCode");
        String description = MapUtils.getString(mmap,"description");
        String name = MapUtils.getString(mmap,"name");
        if(null == name || "".equals(name)){
            return AjaxResult.warn("Please fill in the repository name");
        }
        Map m = projectFeaturesService.getProjectFeaturesOneByNameAndProjectCode(name,projectCode);
        if(MapUtils.isNotEmpty(m)){
            return AjaxResult.warn("This repository already exists with the name");
        }
        ProjectFeatures pf = new ProjectFeatures();
        pf.setCode(CommUtils.getUUID());
        pf.setCreate_time(DateUtil.formatDateTime(new Date()));
        pf.setProject_code(projectCode);
        pf.setDescription(description);
        pf.setName(name);
        pf.setOrganization_code(MapUtils.getString(getLoginMember(),"organizationCode"));
        return AjaxResult.success(projectFeaturesService.save(pf));
    }

    /**
     *
     * @param
     * @return
     */
    @PostMapping("/project_features/index")
    @ResponseBody
    public AjaxResult projectProjectFeatures(@RequestParam Map<String,Object> mmap){
        String projectCode = MapUtils.getString(mmap,"projectCode");
        if(CommUtils.isEmpty(projectCode)){
            return AjaxResult.warn("Please select an item");
        }
        return AjaxResult.success(projectFeaturesService.getProjectFeaturesByProjectCode(projectCode));
    }


    /**
     * @param
     * @return
     */
    @PostMapping("/project_version/save")
    @ResponseBody
    public AjaxResult getProjectVersionSave(@RequestParam Map<String,Object> mmap){
        Map memberMap = getLoginMember();
        String name = MapUtils.getString(mmap,"name");
        String description = MapUtils.getString(mmap,"description");
        String startTime = MapUtils.getString(mmap,"startTime");
        String planPublishTime = MapUtils.getString(mmap,"planPublishTime");
        String featuresCode = MapUtils.getString(mmap,"featuresCode");

        if(CommUtils.isEmpty(name)){
            return AjaxResult.warn("Please fill in the version name");
        }

        Map m = projectFeaturesService.getProjectFeaturesByCode(featuresCode);
        if(MapUtils.isEmpty(m)){
            return AjaxResult.warn("This repository has expired");
        }
        m = projectVersionService.gettProjectVersionByNameAndFeaturesCode(name,featuresCode);
        if(MapUtils.isNotEmpty(m)){
            return AjaxResult.warn("This version already exists with the name");
        }

        ProjectVersion pv = new ProjectVersion();
        ProjectVersionLog pvl = new ProjectVersionLog();
        pv.setCreate_time(DateUtil.formatDateTime(new Date()));
        pv.setCode(CommUtils.getUUID());pv.setFeatures_code(featuresCode);
        pv.setStart_time(startTime);pv.setPlan_publish_time(planPublishTime);
        pv.setDescription(description);pv.setName(name);
        pv.setOrganization_code(MapUtils.getString(memberMap,"organizationCode"));
        pvl.setMember_code(MapUtils.getString(mmap,"memberCountCode"));
        pvl.setSource_code(pv.getCode());pvl.setRemark("Created a new version");
        pvl.setType("create");pvl.setContent(name);pvl.setCreate_time(DateUtil.formatDateTime(new Date()));
        pvl.setCode(CommUtils.getUUID());pvl.setFeatures_code(featuresCode);pvl.setIcon("plus");
        Integer i = projectVersionService.addProjectVersionAndVersionLog(pv,pvl);
        if(i == 2){
            Map pvMap = projectVersionService.getProjectVersionByCode(pv.getCode());
            return AjaxResult.success(pv);
        }
        return AjaxResult.warn("save failed");
    }
    /**
     * project version
     * @param
     * @return
     */
    @PostMapping("/project_version/index")
    @ResponseBody
    public AjaxResult getProjectVersion(@RequestParam Map<String,Object> mmap){
        String projectFeaturesCode = MapUtils.getString(mmap,"projectFeaturesCode");
        if(CommUtils.isEmpty(projectFeaturesCode)){
            return AjaxResult.warn("Please select a repository");
        }
        return AjaxResult.success(projectVersionService.getProjectVersion(projectFeaturesCode));
    }

    @PostMapping("/project_version/removeVersionTask")
    @ResponseBody
    public AjaxResult removeVersionTask(@RequestParam Map<String,Object> mmap) throws Exception {
        String taskCode = MapUtils.getString(mmap,"taskCode");
        Map loginMember = getLoginMember();
        /*Map taskMap = taskService.getTaskMapByCode(taskCode);
        if(MapUtils.isEmpty(taskMap)){
            return AjaxResult.warn("This task has been expired");
        }*/
        Map taskMap = taskService.getTaskMapByCode(taskCode);

        /*Task task = Task.builder().id(MapUtils.getInteger(taskMap,"id")).task_tag(MapUtils.getString(taskMap,"task_tag"))
                .assign_to(MapUtils.getString(taskMap,"assign_to")).begin_time(MapUtils.getString(taskMap,"begin_time"))
                .code(MapUtils.getString(taskMap,"code")).create_by(MapUtils.getString(taskMap,"create_by"))
                .create_time(MapUtils.getString(taskMap,"create_time")).deleted_time(MapUtils.getString(taskMap,"delete_time"))
                .description(MapUtils.getString(taskMap,"description")).end_time();*/
        Task task = BeanMapUtils.mapToBean(taskMap,Task.class);
        if(MapUtils.isEmpty(taskMap)){
            return AjaxResult.warn("This task has been expired");
        }
        if(StringUtils.isNotEmpty(MapUtils.getString(taskMap,"version_code"))){
            task.setVersion_code("");
            task.setFeatures_code("");
            task = projectVersionService.removeVersionTask(task,MapUtils.getString(loginMember,"memberCode"),MapUtils.getString(taskMap,"version_code"));
        }
        return AjaxResult.success(task);
    }
    /**
     * Project version delete
     * @param
     * @return
     */
    @PostMapping("/project_version/delete")
    @ResponseBody
    public AjaxResult projectVersionDelete(@RequestParam Map<String,Object> mmap){
        String versionCode = MapUtils.getString(mmap,"versionCode");
        if(CommUtils.isEmpty(versionCode)){
            return AjaxResult.warn("Please select a version");
        }
        return AjaxResult.success(projectVersionService.delProjectVersion(versionCode));
    }
    /**
     * @param
     * @return
     */
    @PostMapping("/project_version/_getVersionTask")
    @ResponseBody
    public AjaxResult getVersionTask(@RequestParam Map<String,Object> mmap){
        String versionCode = MapUtils.getString(mmap,"versionCode");
        if(CommUtils.isEmpty(versionCode)){
            return AjaxResult.warn("Please select a version");
        }
        Map param = new HashMap(){{
            put("versionCode",versionCode);
            put("deleted",0);
        }};
        List<Map>  taskList = taskService.getTaskListByVersionAndDelete(param);
        List<Map> resultList = new ArrayList<>();
        Map memberMap = null;
        for(Map m:taskList){
            memberMap = memberService.getMemberMapByCode(MapUtils.getString(m,"assign_to"));
            m.put("executor",CommUtils.getMapField(memberMap,new String[]{"name","avatar"}));
            resultList.add(m);
        }
        return AjaxResult.success(resultList);
    }

    /**Query version log
     * @param
     * @return
     */
    @PostMapping("/project_version/_getVersionLog")
    @ResponseBody
    public AjaxResult getVersionLog(@RequestParam Map<String,Object> mmap){
        String versionCode = MapUtils.getString(mmap,"versionCode");
        Integer showAll = MapUtils.getInteger(mmap,"all",0);

        List<Map> selList = new ArrayList<>();
        List<Map> listResult = new ArrayList<>();
        Map resultData = new HashMap();

        if(showAll == 0){
           selList = projectVersionLogService.getProjectVersionLogBySourceCodeAll(versionCode);
           if(selList == null)selList = new ArrayList<>();

           resultData.put("total",selList.size());
        }else{
            Integer pageSize = MapUtils.getInteger(mmap,"pageSize",1000);
            Integer page = MapUtils.getInteger(mmap,"page",1);
            IPage<Map> iPage = new Page<>();
            iPage.setCurrent(page);iPage.setSize(pageSize);
            iPage = projectVersionLogService.getProjectVersionBySourceCode(iPage,versionCode);
            selList = iPage.getRecords();
            resultData.put("total",iPage.getTotal());
            resultData.put("page",iPage.getCurrent());
        }
        if(!CollectionUtils.isEmpty(selList)){
            Map memberMap = null;
            for(Map m:selList){
                memberMap = memberService.getMemberMapByCode(MapUtils.getString(m,"member_code"));
                m.put("member",CommUtils.getMapField(memberMap,new String[]{"id","name","avatar","code"}));
                listResult.add(m);
            }
        }
        resultData.put("list",listResult);
        return AjaxResult.success(resultData);
    }

    /**Associated tasks
     * @param
     * @return
     */
    @PostMapping("/project_version/addVersionTask")
    @ResponseBody
    public AjaxResult addVersionTask(@RequestParam Map<String,Object> mmap) throws Exception {
        String taskCodeList = MapUtils.getString(mmap,"taskCodeList");
        String versionCode = MapUtils.getString(mmap,"versionCode");
        Map memberMap = getLoginMember();
        Integer successTotal = 0;
        if(!CommUtils.isEmpty(taskCodeList)){
            JSONArray jsonArray = JSON.parseArray(taskCodeList);
            if(StringUtils.isNotEmpty(jsonArray)){
                List<String> taskListName = new ArrayList<>();
                for (Object obj : jsonArray) {
                    Map taskMap = taskService.getTaskMapByCode(String.valueOf(obj));
                    if(MapUtils.isEmpty(taskMap)){
                        return AjaxResult.warn("This task has been expired");
                    }
                    String versionCodeTask = MapUtils.getString(taskMap,"version_code","0");
                    if(!"0".equals(versionCodeTask) && StringUtils.isNotEmpty(versionCodeTask)){
                        return AjaxResult.warn("This task is already associated");
                    }
                    Map versionMap = projectVersionService.getProjectVersionByCode(versionCode);
                    if(MapUtils.isEmpty(taskMap)){
                        return AjaxResult.warn("This version has been deprecated");
                    }
                    Task task = projectVersionService.addVersionTask(taskMap,versionMap);
                    if(!ObjectUtils.isEmpty(task)){
                        taskListName.add(task.getName());
                        successTotal ++;
                    }
                }
                /*if(successTotal>0){
                    Map projectVersion = projectVersionService.getProjectVersionByCode(versionCode);
                    ProjectVersionLog pvl = new ProjectVersionLog();
                    pvl.setMember_code(MapUtils.getString(memberMap,"memberCode")).setSource_code(versionCode);pvl.setRemark("添加了 " + successTotal + " 项目发布内容");
                    pvl.setType("addVersionTask").setContent(StringUtils.join(taskListName,",")).setCreate_time(DateUtil.formatDateTime(new Date()));
                    pvl.setFeatures_code(MapUtils.getString(projectVersion,"features_code")).setIcon("link");
                    projectVersionLogService.save(pvl);
                }*/
                projectVersionService.run(new HashMap(){{
                    put("memberCode",MapUtils.getString(memberMap,"memberCode"));
                    put("versionCode",versionCode);
                    put("type","addVersionTask");
                    put("data",taskListName);
                }});
            }
        }
        Map result = new HashMap();
        result.put("successTotal",successTotal);
        return AjaxResult.success(result);
    }
    /**
     * change version status
     * @param
     * @return
     */
    @PostMapping("/project_version/changeStatus")
    @ResponseBody
    public AjaxResult projectVersionChangeStatus(@RequestParam Map<String,Object> mmap) {
        String versionCode = MapUtils.getString(mmap, "versionCode");
        Integer status = MapUtils.getInteger(mmap, "status",-1);
        String publishTime = MapUtils.getString(mmap, "publishTime");
        Map memberMap = getLoginMember();
        if (CommUtils.isEmpty(versionCode)) {
            return AjaxResult.warn("Please select a version");
        }
        Map versionMap = projectVersionService.getProjectVersionByCode(versionCode);
        ProjectVersion pv = new ProjectVersion();
        pv.setId(MapUtils.getInteger(versionMap,"id"));
        pv.setStatus(status);
        if(status == 3){
            pv.setPublish_time(publishTime);
        }
        boolean i = projectVersionService.updateById(pv);
        ProjectVersionLog pvl = new ProjectVersionLog();
       pvl.setMember_code(MapUtils.getString(memberMap,"memberCode"));
        pvl.setSource_code(versionCode).setRemark("Updated status to"+ projectVersionService.getStatusTextAttr(String.valueOf(status)));
        pvl.setType("status").setContent("").setCreate_time(DateUtil.formatDateTime(new Date()));
        pvl.setFeatures_code(MapUtils.getString(versionMap,"features_code")).setIcon("check-square");
        projectVersionLogService.save(pvl);
        return AjaxResult.success(i);
    }
    /**
     * Project version editor
     * @param
     * @return
     */
    @PostMapping("/project_version/edit")
    @ResponseBody
    public AjaxResult projectVersionEdit(@RequestParam Map<String,Object> mmap){
        String versionCode = MapUtils.getString(mmap,"versionCode");
        String name = MapUtils.getString(mmap,"name");
        String description = MapUtils.getString(mmap,"description");
        String start_time = MapUtils.getString(mmap,"start_time");
        String plan_publish_time = MapUtils.getString(mmap,"plan_publish_time");
        if(CommUtils.isEmpty(versionCode)){
            return AjaxResult.warn("Please select a version\r\n");
        }
        Map versionMap = projectVersionService.getProjectVersionByCode(versionCode);
        if(MapUtils.isEmpty(versionMap)){
            return AjaxResult.warn("This version is no longer valid");
        }
        if(!CommUtils.isEmpty(name)){
            Map proVerMap = projectVersionService.gettProjectVersionByNameAndFeaturesCode(name,MapUtils.getString(versionMap,"features_code"));
            if(MapUtils.isNotEmpty(proVerMap)){
                return AjaxResult.warn("This version name already exists");
            }
        }
        String type = "name";
        ProjectVersion upProjectVersion = new ProjectVersion();
        upProjectVersion.setId(MapUtils.getInteger(versionMap,"id"));
        if(!CommUtils.isEmpty(name))upProjectVersion.setName(name);
        if(!CommUtils.isEmpty(description))upProjectVersion.setDescription(description);
        if(!CommUtils.isEmpty(start_time))upProjectVersion.setStart_time(start_time);
        if(!CommUtils.isEmpty(plan_publish_time))upProjectVersion.setPlan_publish_time(plan_publish_time);
        boolean bo =  projectVersionService.updateById(upProjectVersion);
        ProjectVersionLog pvl = new ProjectVersionLog();
        String remark = "";
        if(null != name){
            type="name";
            remark = "The name of the update is " + name + " ";
        }
        if(null != description){
            type="content";
            remark = "The update description is " + description + " ";
            if("".equals(description)){
                type="clearContent";
                remark = " Clear description content ";
            }
        }
        if(null != start_time){
            type="setStartTime";
            remark = "The update starts at " + start_time + " ";
            if("".equals(start_time)){
                type="clearStartTime";
                remark = " clear start time ";
            }
        }
        if(null != plan_publish_time){
            type="setPlanPublishTime";
            remark = "Updates are scheduled for release at "+ plan_publish_time;
            if("".equals(plan_publish_time)){
                type="clearPlanPublishTime";
                remark = "Clear schudeled release time";
            }
        }


        Map memberMap = getLoginMember();

        pvl.setMember_code(MapUtils.getString(memberMap,"memberCode"));
        pvl.setSource_code(versionCode).setRemark(remark);
        pvl.setType("status").setContent("").setCreate_time(DateUtil.formatDateTime(new Date()));
        pvl.setFeatures_code(MapUtils.getString(versionMap,"features_code")).setIcon("check-square");
        projectVersionLogService.save(pvl);
        return AjaxResult.success(bo);
    }
    /**
     * project version read
     * @param
     * @return
     */
    @PostMapping("/project_version/read")
    @ResponseBody
    public AjaxResult projectVersionRead(@RequestParam Map<String,Object> mmap){
        String versionCode = MapUtils.getString(mmap,"versionCode");
        if(CommUtils.isEmpty(versionCode)){
            return AjaxResult.warn("Please select a version");
        }
        Map versionMap = projectVersionService.getProjectVersionByCode(versionCode);
        versionMap.put("statusText",projectVersionService.getStatusTextAttr(MapUtils.getString(versionMap,"status")));
        if(MapUtils.isNotEmpty(versionMap)){
            Map featureMap = projectFeaturesService.getProjectFeaturesByCode(MapUtils.getString(versionMap,"features_code"));
            versionMap.put("featureName",MapUtils.getString(featureMap,"name"));
            versionMap.put("projectCode",MapUtils.getString(featureMap,"project_code"));
        }
        return AjaxResult.success(versionMap);
    }

    /**
     * My files move to trash
     * @param
     * @return
     */
    @PostMapping("/project_info/index")
    @ResponseBody
    public AjaxResult projectProjectInfo(@RequestParam Map<String,Object> mmap){
        String projectCode = MapUtils.getString(mmap,"projectCode");
        List<Map> projectInfoList = projectInfoService.getProjectInfoByProjectCode(projectCode);
        return AjaxResult.success(projectInfoList);

    }
    /**
     * My files move to trash
     * @param
     * @return
     */
    @PostMapping("/file/recycle")
    @ResponseBody
    public AjaxResult projectFileRecycle(@RequestParam Map<String,Object> mmap){
        String fileCode = MapUtils.getString(mmap,"fileCode");

        Map fileMap = fileService.getFileByCode(fileCode);
        if(MapUtils.isEmpty(fileMap)){
            return  AjaxResult.warn("file does not exist");
        }
        if(1== MapUtils.getInteger(fileMap,"deleted")){
            return  AjaxResult.warn("File already in Trash");
        }
        com.projectm.task.domain.File projectFile = new com.projectm.task.domain.File();
        projectFile.setId(MapUtils.getInteger(fileMap,"id"));
        projectFile.setDeleted(1);projectFile.setDeleted_time(DateUtil.formatDateTime(new Date()));
        return AjaxResult.success(fileService.updateById(projectFile));
    }
    /**
     * my file rename

     * @param
     * @return
     */
    @PostMapping("/file/edit")
    @ResponseBody
    public AjaxResult projectFileEdit(@RequestParam Map<String,Object> mmap){
        String title = MapUtils.getString(mmap,"title");
        String fileCode = MapUtils.getString(mmap,"fileCode");

        Map fileMap = fileService.getFileByCode(fileCode);
        com.projectm.task.domain.File projectFile = new com.projectm.task.domain.File();
        projectFile.setId(MapUtils.getInteger(fileMap,"id"));
        projectFile.setTitle(title);
        return AjaxResult.success(fileService.updateById(projectFile));
    }

    /**
     * my file list
     * @param
     * @return
     */
    @PostMapping("/file/index")
    @ResponseBody
    public AjaxResult getProjectFile(@RequestParam Map<String,Object> mmap){

        String projectCode = MapUtils.getString(mmap,"projectCode");
        Integer deleted = MapUtils.getInteger(mmap,"deleted",0);
        Map params = new HashMap(){{
            put("projectCode",projectCode);
            put("deleted",deleted);
        }};
        IPage<com.projectm.task.domain.File> page_ = Constant.createPage(new Page<com.projectm.task.domain.File>(),mmap);
        page_=fileService.lambdaQuery().eq(com.projectm.task.domain.File::getProject_code,projectCode).eq(com.projectm.task.domain.File::getDeleted,0).page(page_);
        List<com.projectm.task.domain.File> resultList = new ArrayList<>();
        for(int i=0;page_ !=null && page_.getRecords() !=null && i<page_.getRecords().size();i++){
            com.projectm.task.domain.File f = page_.getRecords().get(i);
            Member member = memberService.lambdaQuery().eq(Member::getCode,f.getCreate_by()).one();
            f.setCreatorName(member.getName());
            f.setFullName(f.getTitle()+"."+f.getExtension());
            resultList.add(f);
        }
        page_.setRecords(resultList);
        Map data = Constant.createPageResultMap(page_);
        return AjaxResult.success(data);
    }
    @Value("${mproject.downloadServer}")
    private String downloadServer;

    
    /**
     * Each upload block will contain the following block information:
     * chunkNumber: The order of the current block, the first block is 1, note that it does not start from 0.
     * totalChunks: The total number of chunks the file is divided into.
     * chunkSize: chunk size, according to totalSize and this value you can calculate the total number of chunks. Note that the size of the last block may be larger than this.
     * currentChunkSize: The size of the current chunk, the actual size.
     * totalSize: total file size.
     * identifier: This is the unique identifier for each file.
     * filename: file name.
     * relativePath: The relative path attribute of the file when the folder is uploaded.
     * A chunk can be uploaded multiple times. Of course, this is definitely not a standard behavior, but this kind of thing may happen during the actual upload process. This retransmission is also one of the features of this library.
     *
     * Considered successful or failed according to the response code:
     * 200 file upload completed
     * 201 The document was uploaded successfully
     * 500 Failed to upload the first piece, cancel the entire file upload
     * 507 Server Error Automatically retry the file chunk upload
     *
     * This is still incomplete, and the encryption of resumable uploads, instant transfer processing, etc. have not been processed.
     */
	@PostMapping("/file/uploadFiles")
    @ResponseBody
    public AjaxResult uploadFiels(HttpServletRequest request, @RequestParam("file") MultipartFile multipartFile)  throws Exception{
        String  fileName= request.getParameter("identifier");
        String  orgFileName= request.getParameter("filename");
        int  chunkNumber= request.getParameter("chunkNumber") == null ?0:new Integer(request.getParameter("chunkNumber"));
        int  totalChunks= request.getParameter("totalChunks") == null ?0:new Integer(request.getParameter("totalChunks"));

        String  taskCode= request.getParameter("taskCode");
        String projectCode = request.getParameter("projectCode");
        Map loginMember = getLoginMember();
        String orgCode = MapUtils.getString(loginMember,"organizationCode");
        String memberCode = MapUtils.getString(loginMember,"memberCode");
        if (multipartFile.isEmpty()) {
            return  AjaxResult.warn("file cannot be empty!");
        } else {
            String dateTimeNow = DateUtils.dateTimeNow();
            String date = DateUtils.dateTimeNow("yyyyMMdd");
            String uuid = CommUtils.getUUID();
            // original file name
            String originFileName = multipartFile.getOriginalFilename().toString();
            // upload file rename
            String uploadFileName = uuid+"-"+originFileName;
            //String file_url = MProjectConfig.getUploadFolderPath()+memberCode+"/"+date+"/"+dateTimeNow+uploadFileName;
            //String base_url = MProjectConfig.getStaticUploadPrefix()+memberCode+"/"+date+"/"+dateTimeNow+uploadFileName;
            String file_url = MProjectConfig.getProfile()+"/projectfile/"+memberCode+"/"+date+"/";
            String base_url = "/projectfile/"+memberCode+"/"+date+"/"+uploadFileName;
            String downloadUrl = "/common/download?filePathName="+base_url+"&realFileName="+originFileName;
            // Here use Apache's FileUtils method to save
            //FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), new File(file_url, uploadFileName));
            File tempFile= new File(file_url, originFileName);
            Long fileSize = 0L;
            //The first block, then create a new file
            if(1==chunkNumber && !tempFile.exists()){
            	FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), tempFile);
            }else{
                //perform file write operations
                try(
                        //write chunks to file
                        InputStream fos=multipartFile.getInputStream();
                        RandomAccessFile raf =new RandomAccessFile(tempFile,"rw")
                ) {
                    int len=-1;
                    byte[] buffer=new byte[1024];
                    raf.seek((chunkNumber-1)*1024*1024);
                    while((len=fos.read(buffer))!=-1){
                        raf.write(buffer,0,len);
                    }
                    //File size
                    fileSize = raf.length();
                } catch (IOException e) {
                    e.printStackTrace();
                    if(chunkNumber==1) {
                    	tempFile.delete();
                    }
                    throw new Exception("Error reading and writing files!");
                }
            }

            
            if(chunkNumber == totalChunks){
                //Shard reading and writing end
            	//rename to avoid duplication
            	File newFile  = new File(file_url, uploadFileName);
            	tempFile.renameTo(newFile);
                com.projectm.task.domain.File file = com.projectm.task.domain.File.builder().fsize(fileSize)
                        .path_name(file_url+uploadFileName)
                        .file_url(downloadServer+downloadUrl)
                        .title(originFileName.substring(0,originFileName.lastIndexOf(".")))
                        .file_type("text/plain")
                        .create_time(DateUtil.getCurrentDateTime())
                        .code(uuid)
                        .organization_code(orgCode)
                        .project_code(projectCode)
                        .create_by(memberCode)
                        .deleted(0)
                        .downloads(0l)
                        .task_code(taskCode)
                        .extension(originFileName.substring(originFileName.lastIndexOf(".")+1)).build();
                Project project = fileService.uploadFiles(file,memberCode,projectCode);
                Map result = new HashMap();
                result.put("key",file.getPath_name());
                result.put("url",file.getFile_url());
                result.put("projectName",project.getName());
                return AjaxResult.success(result);
            }else {
                //normal return
                return AjaxResult.success();
            }
        }
    }

    @PostMapping("/file/recovery")
    @ResponseBody
    public AjaxResult fileRecovery(@RequestParam Map<String,Object> mmap) {
        String fileCode = MapUtils.getString(mmap,"fileCode");
        fileService.recovery(fileCode);
        return  AjaxResult.success();
    }
    @PostMapping("/file/delete")
    @ResponseBody
    public AjaxResult deleteFile(@RequestParam Map<String,Object> mmap) {
        String fileCode = MapUtils.getString(mmap,"fileCode");
        fileService.deleteFile(fileCode);
        return  AjaxResult.success();
    }

}
