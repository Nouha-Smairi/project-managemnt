package com.projectm.project.controller;

import com.framework.common.exception.CustomException;
import com.framework.common.utils.ServletUtils;
import com.framework.common.utils.StringUtils;
import com.framework.utils.FileUtils;
import com.projectm.config.MProjectConfig;
import org.apache.commons.collections.MapUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.Map;

import static com.framework.common.utils.ServletUtils.getResponse;

@RestController
public class CommonController {

    /**
     * Universal Download Request
     *
     * @param filePathName file path and file name

     * @param delete delete or not

     */
    @GetMapping("/common/download")
    public void fileDownload(@RequestParam Map<String,Object> mmap)
    {
        String filePathName =  MapUtils.getString(mmap,"filePathName");
        String realFileName =  MapUtils.getString(mmap,"realFileName");
        boolean delete =  MapUtils.getBoolean(mmap,"delete",false);
        try
        {
            /*if (!FileUtils.isValidFilename(filePathName))
            {
                throw new CustomException(StringUtils.format("The file name ({}) is illegal and the download is not allowed. ", filePathName));
            }*/
            String filePath = MProjectConfig.getProfile() + filePathName;

            getResponse().setCharacterEncoding("utf-8");
            getResponse().setContentType("multipart/form-data");
            getResponse().setHeader("Content-Disposition",
                    "attachment;fileName=" + FileUtils.setFileDownloadHeader(ServletUtils.getRequest(), realFileName));
            FileUtils.writeBytes(filePath, getResponse().getOutputStream());
            if (delete)
            {
                FileUtils.deleteFile(filePath);
            }
        }
        catch (Exception e)
        {

        }
    }

    @GetMapping("/common/image")
    public void image(@RequestParam Map<String,Object> mmap)
    {
        String filePathName =  MapUtils.getString(mmap,"filePathName");
        String realFileName =  MapUtils.getString(mmap,"realFileName");
        try
        {

            String filePath = MProjectConfig.getProfile() + filePathName;

            FileUtils.writeBytes(filePath, getResponse().getOutputStream());

        }
        catch (Exception e)
        {e.printStackTrace();
            throw new CustomException(e.getMessage());
        }
    }
}
