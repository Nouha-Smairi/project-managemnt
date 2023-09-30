package com.projectm.vo;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @version V1.0
 * @program: teamwork
 * @package: com.projectm.vo
 * @description: invite user-vo
 * @create: 2020-07-01 11:28
 **/
@Builder
@Data
public class MemberVo {

    private String accountCode;
    private String avatar;
    private String name;
    private String email;
    private boolean joined;

}
