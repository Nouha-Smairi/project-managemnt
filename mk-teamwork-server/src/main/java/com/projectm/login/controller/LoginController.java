package com.projectm.login.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.framework.common.AjaxResult;
import com.framework.common.ResultCode;
import com.framework.common.constant.Constants;
import com.framework.common.exception.CustomException;
import com.framework.common.utils.DateUtils;
import com.framework.common.utils.security.Md5Utils;
import com.framework.security.util.RedisCache;
import com.framework.security.util.TokenUtil;
import com.framework.security.util.UserUtil;
import com.framework.utils.PinYinUtil;
import com.projectm.config.MProjectConfig;
import com.projectm.login.entity.LoginUser;
import com.projectm.login.service.LoginService;
import com.projectm.member.domain.Member;
import com.projectm.member.domain.MemberAccount;
import com.projectm.member.service.MemberAccountService;
import com.projectm.member.service.MemberService;
import com.projectm.org.domain.Organization;
import com.projectm.org.service.OrganizationService;
import com.projectm.project.domain.ProjectAuth;
import com.projectm.project.domain.ProjectAuthNode;
import com.projectm.project.domain.ProjectNode;
import com.projectm.project.service.ProjectAuthNodeService;
import com.projectm.project.service.ProjectAuthService;
import com.projectm.project.service.ProjectNodeService;
import com.sun.org.apache.bcel.internal.generic.NEW;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bytecode.Throw;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class LoginController {

    @Resource
    private AuthenticationManager authenticationManager;

    @Autowired
    private LoginService loginService;

    @Value("${jwt.expiration}")
    private int expireTime;
    @Value("${mproject.downloadServer}")
    private String downloadServer;

    @Autowired
    private TokenUtil tokenUtil;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberAccountService memberAccountService;
    @Autowired
    private ProjectAuthService projectAuthService;
    @Autowired
    private ProjectAuthNodeService projectAuthNodeService;
    @Autowired
    private ProjectNodeService projectNodeService;

    /**
     * méthode de connexion
     *
     * @param account  nom d'utilisateur

     * @param password mot de passe
     * @return résultat

     */
    @PostMapping("/teamwork/login")
    @ResponseBody
    public AjaxResult login(String account, String password) {
        //Vérification du code de vérification

        //Authentification d'utilisateur
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(account, password));
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        String userCode = loginUser.getUser().getCode();
        List<MemberAccount> list = memberAccountService.lambdaQuery().eq(MemberAccount::getMember_code, userCode).list();
        loginUser.getUser().setMemberAccountList(list);
        Set<String> authSet = list.stream().map(MemberAccount::getAuthorize).collect(Collectors.toSet());
        List<ProjectAuthNode> projectAuthNodeList = projectAuthNodeService.lambdaQuery().in(ProjectAuthNode::getAuth, authSet).list();
        list.forEach(memberAccount -> {
            List<String> nodeList = projectAuthNodeList.parallelStream().filter(auth -> Objects.equals(auth.getAuth().toString(), memberAccount.getAuthorize()))
                    .map(ProjectAuthNode::getNode).collect(Collectors.toList());
            memberAccount.setNodeList(nodeList);
        });
        redisCache.setCacheObject(Constants.LOGIN_USER_KEY + userCode, loginUser, expireTime + 1, TimeUnit.MINUTES);
        // générer token
        Map<String, Object> tokenList = buildToken(userCode);
        Set<String> collect = list.stream().map(MemberAccount::getOrganization_code).collect(Collectors.toSet());
        List<Organization> organizationList = organizationService.lambdaQuery().in(Organization::getCode, collect).list();
        Map<String, Object> resultMap = new HashMap<>(8);
        resultMap.put("member", loginUser.getUser());
        resultMap.put("organizationList", organizationList);
        resultMap.put("tokenList", tokenList);
        return AjaxResult.success(resultMap);
    }

    /**
     * rafraîchir Token
     * @param refreshToken token value
     * @return
     */
    @PostMapping("/index/index/refreshAccessToken")
    @ResponseBody
    public AjaxResult login(String refreshToken) {
        if (tokenUtil.verifyToken(refreshToken)) {
            String userCode = tokenUtil.getUserCode(refreshToken);
            boolean expire = redisCache.expire(Constants.LOGIN_USER_KEY + userCode, expireTime + 1, TimeUnit.MINUTES);
            log.info("update cache time：{}", expire);
            // generate token
            Map<String, Object> tokenList = buildToken(userCode);
            return AjaxResult.success(tokenList);
        } else {
            throw new CustomException(ResultCode.VERIFY_TOKEN_FAIL);
        }
    };

    /**
     * Build Token information
     * @param userCode user Code
     * @return
     */
    private Map<String, Object> buildToken(String userCode) {

        Calendar instance = Calendar.getInstance();
        Date issDate = instance.getTime();
        instance.add(Calendar.MINUTE, expireTime);
        Date expireDate = instance.getTime();
        String token = tokenUtil.createToken(issDate, expireDate, userCode);
        instance.add(Calendar.MINUTE, 10);
        Date expireDate1 = instance.getTime();
        String refreshToken = tokenUtil.createToken(issDate, expireDate1, userCode);
        Map<String, Object> tokenList = new HashMap<>(10);
        Calendar.getInstance();
        tokenList.put("accessToken", token);
        tokenList.put("refreshToken", refreshToken);
        tokenList.put("tokenType", Constants.TOKEN_PREFIX.trim());
        tokenList.put("accessTokenExp", expireDate.getTime()/1000);
        return tokenList;
    }

    /**
     * get verification code

     *
     * @param mobile Phone number

     * @return verification code

     */
    @PostMapping("/project/login/getCaptcha")
    @ResponseBody
    public AjaxResult getCaptcha(String mobile) {
        int captcha = (int) ((Math.random() * 9 + 1) * 100000);
        redisCache.setCacheObject(mobile, captcha, Constants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        return AjaxResult.success(captcha);
    }

    @Value("${mproject.projectImg}")
    private String projectImg;

    /**
     * registered user

     *
     * @param mobile Phone number

     * @return verification code

     */
    @PostMapping("/project/login/register")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult register(String email, String name, String password, String mobile, String captcha) {
        String defaultAvatar = "fd7c214cd05ffa9870efd13205aa6dd3.jpg";
        String downloadUrl = "/common/download?filePathName="+ MProjectConfig.getProfile()+"/"+projectImg+"&realFileName="+projectImg;
        Object cacheObject = redisCache.getCacheObject(mobile);
        if (cacheObject != null && StrUtil.equals(captcha, cacheObject.toString())) {
            List<Member> list = memberService.lambdaQuery().list();
            List<String> accountList = list.parallelStream().map(Member::getAccount).distinct().collect(Collectors.toList());
            Member member = list.parallelStream().filter(o -> StrUtil.equals(email, o.getEmail())).findAny().orElse(null);
            if (member == null) {
                boolean used = true;
                String account = null;
                while (used) {
//                    account = IdUtil.fastSimpleUUID().substring(0, 7);
                   /*Convert username from Chinese characters to pinyin*/
                    account = PinYinUtil.getPinyin(name);
                    if (!accountList.contains(account)) {
                        used = false;
                    }
                }
                String uuid = IdUtil.fastSimpleUUID();
                //user
                Member saveMember = Member.builder().code(uuid).account(account).password(Md5Utils.hash(password)).name(name).mobile(mobile)
                        .create_time(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateUtils.YYYY_MM_DD_HH_MM_SS)))
                        .status(1).avatar(downloadServer + downloadUrl).email(email).build();
                //organize
                Organization saveOrganization = Organization.builder().name(name + "personal project").owner_code(uuid)
                        .create_time(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateUtils.YYYY_MM_DD_HH_MM_SS)))
                        .personal(1).code(IdUtil.fastSimpleUUID()).build();
                //organizational role
                ProjectAuth auth1 = ProjectAuth.builder().title("administrator").status(1).sort(0).desc("administrator")
                        .create_at(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateUtils.YYYY_MM_DD_HH_MM_SS)))
                        .organization_code(saveOrganization.getCode()).type("admin").build();
                ProjectAuth auth2 = ProjectAuth.builder().title("user").status(1).sort(1).desc("user")
                        .create_at(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateUtils.YYYY_MM_DD_HH_MM_SS)))
                        .organization_code(saveOrganization.getCode()).type("member").is_default(1).build();
                boolean saveAuth1 = projectAuthService.save(auth1);
                boolean saveAuth2 = projectAuthService.save(auth2);
                log.info("create admin：{}，create user：{}", saveAuth1, saveAuth2);
                //organization account

                MemberAccount saveMemberAccount = MemberAccount.builder().code(IdUtil.fastSimpleUUID()).member_code(saveMember.getCode())
                        .organization_code(saveOrganization.getCode()).authorize(auth1.getId().toString()).is_owner(1).name(name).mobile(mobile).email(email)
                        .create_time(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateUtils.YYYY_MM_DD_HH_MM_SS)))
                        .status(1).avatar(saveMember.getAvatar()).build();
                //Get permission node
                List<ProjectNode> adminNode = projectNodeService.lambdaQuery().list();
                List<ProjectNode> memberNode = adminNode.parallelStream().filter(o -> o.getNode().contains("project/notify") ||
                        o.getNode().contains("project/project") || o.getNode().contains("project/task")).collect(Collectors.toList());
                //Actors and Node Collections
                List<ProjectAuthNode> saveAuthNode = new ArrayList<>();
                adminNode.forEach(o -> {
                    saveAuthNode.add(ProjectAuthNode.builder().auth(auth2.getId()).node(o.getNode()).build());
                });
                memberNode.forEach(o -> {
                    saveAuthNode.add(ProjectAuthNode.builder().auth(auth1.getId()).node(o.getNode()).build());
                });
                boolean b = memberService.save(saveMember);
                boolean b1 = organizationService.save(saveOrganization);
                boolean b2 = projectAuthNodeService.saveBatch(saveAuthNode);
                boolean b3 = memberAccountService.save(saveMemberAccount);
                log.info("new user：{}，new organization：{}，new character node：{}，Organization User Relationship：{}", b, b1, b2, b3);
                return AjaxResult.success();
            }
            return new AjaxResult(ResultCode.EMAIL_USED);
        } else {
            return new AjaxResult(ResultCode.CAPTCHA_EXPIRED);
        }
    }

    /**
     * change Password
     * @param id user primary key
     * @param password Old Password
     * @param newPassword New Password
     * @return
     */
    @PostMapping("/project/index/editPassword")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult editPassword(String id, String password, String newPassword) {
        Member member = memberService.lambdaQuery().select(Member::getPassword).eq(Member::getId, id).one();
        if (StrUtil.equals(member.getPassword(), Md5Utils.hash(password))) {
            boolean update = memberService.lambdaUpdate().set(Member::getPassword, Md5Utils.hash(newPassword)).eq(Member::getId, id).update();
            log.info("Change password：{}", update);
            String str = update ? "Password reset complete" : "password modification failed";
            return AjaxResult.success(str, update);
        } else {
            return AjaxResult.warn("The original password is wrong！");
        }
    }

    /**
     * bind number
     * @param mobile Number
     * @param captcha verification code
     * @return
     */
    @PostMapping("/project/login/_bindMobile")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult bindMobile(String mobile, String captcha) {
        Object cacheObject = redisCache.getCacheObject(mobile);
        if (cacheObject != null && StrUtil.equals(captcha, cacheObject.toString())) {
            LoginUser loginUser = UserUtil.getLoginUser();
            Member one = memberService.lambdaQuery().select(Member::getName).eq(Member::getMobile, mobile).one();
            if (ObjectUtil.isEmpty(one)) {
                boolean update = memberService.lambdaUpdate().set(Member::getMobile, mobile).eq(Member::getCode, loginUser.getUser().getCode()).update();
                log.info("phone number modification：{}", update);
                String str = update ? "bin successfully" : "binding failed";
                return AjaxResult.success(str, update);
            } else {
                return new AjaxResult(ResultCode.MOBILE_USED);
            }
        } else {
            return new AjaxResult(ResultCode.CAPTCHA_EXPIRED);
        }
    }

    @PostMapping("/project/login/_bindMail")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult bindMobile(String mail) {
        LoginUser loginUser = UserUtil.getLoginUser();
        Member one = memberService.lambdaQuery().select(Member::getName).eq(Member::getEmail, mail).one();
        if (ObjectUtil.isEmpty(one)) {
            boolean update = memberService.lambdaUpdate().set(Member::getEmail, mail).eq(Member::getCode, loginUser.getUser().getCode()).update();
            log.info("Email modification：{}", update);
            String str = update ? "bind successfully" : "binding failed";
            return AjaxResult.success(str, update);
        } else {
            return new AjaxResult(ResultCode.EMAIL_USED);
        }
    }
    
    @PostMapping("/project/login/_out")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult logOut(String mail) {
         return AjaxResult.success("exit successfully", true);      
    }
}
