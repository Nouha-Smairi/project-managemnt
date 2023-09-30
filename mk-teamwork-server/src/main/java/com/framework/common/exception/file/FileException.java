package com.framework.common.exception.file;

import com.framework.common.exception.BaseException;

/**
 * File Information Exception Class
 * 
 */
public class FileException extends BaseException
{
    private static final long serialVersionUID = 1L;

    public FileException(String code, Object[] args)
    {
        super("file", code, args, null);
    }

}
