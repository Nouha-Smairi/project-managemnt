package com.projectm.web;

import com.framework.common.AjaxResult;
import com.framework.common.ResultCode;
import com.framework.common.exception.CustomException;
import com.framework.common.utils.ServletUtils;
import com.framework.common.utils.StringUtils;
import com.framework.security.util.UserUtil;
import com.framework.utils.FileUtils;
import com.projectm.common.DateUtils;
import com.projectm.config.MProjectConfig;
import com.projectm.login.entity.LoginUser;

import cn.hutool.core.util.StrUtil;

import org.apache.commons.collections.MapUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestParam;

import java.beans.PropertyEditorSupport;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BaseController {
    /**
     *Convertir automatiquement la chaîne de format de date transmise par la réception en type Date
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // Date type conversion
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(DateUtils.parseDate(text));
            }
        });
    }


    /**
     * response return result
     *
     * @param rows Affected rows
     * @return operation result
     */
    protected AjaxResult toAjax(int rows) {
        return rows > 0 ? success() : error();
    }

    /**
     * response return result
     *
     * @param result result
     * @return operation result
     */
    protected AjaxResult toAjax(boolean result) {
        return result ? success() : error();
    }

    /**
     * return success
     */
    public AjaxResult success() {
        return AjaxResult.success();
    }

    /**
     * return failure message
     */
    public AjaxResult error() {
        return AjaxResult.error();
    }

    /**
     * return a success message
     */
    public AjaxResult success(String message) {
        return AjaxResult.success(message);
    }

    /**
     * return failure message
     */
    public AjaxResult error(String message) {
        return AjaxResult.error(message);
    }


    public Map getLoginMember() {
        LoginUser loginUser = UserUtil.getLoginUser();
        Map<String, String> member = new HashMap<>(4);
        if (loginUser != null) {
            member.put("memberCode", loginUser.getUser().getCode());
            member.put("organizationCode", ServletUtils.getHeaderParam("organizationCode"));
        }
        //member.put("departmentCode","6v7be19pwman2fird04gqu53");
        //member.put("memberCountCode","6v7be19pwman2fird04gqu11");
        return member;
    }
    
    public String getOrgCode() {
    	String organizationCode = ServletUtils.getHeaderParam("organizationCode");
    		if (StrUtil.isEmpty(organizationCode)) {
    			throw new CustomException("Missing organization information");
    		}
    		return organizationCode;
    	}
}
