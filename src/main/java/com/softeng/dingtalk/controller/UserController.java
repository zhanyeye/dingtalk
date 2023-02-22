package com.softeng.dingtalk.controller;

import com.softeng.dingtalk.component.dingApi.BaseApi;
import com.softeng.dingtalk.aspect.AccessPermission;
import com.softeng.dingtalk.component.UserContextHolder;
import com.softeng.dingtalk.component.convertor.PermissionConvertor;
import com.softeng.dingtalk.component.convertor.TeamConvertor;
import com.softeng.dingtalk.dto.CommonResult;
import com.softeng.dingtalk.dto.resp.PermissionResp;
import com.softeng.dingtalk.dto.resp.TeamResp;
import com.softeng.dingtalk.enums.PermissionEnum;
import com.softeng.dingtalk.po_entity.Message;
import com.softeng.dingtalk.service.NotifyService;
import com.softeng.dingtalk.service.UserService;
import com.softeng.dingtalk.utils.StreamUtils;
import com.softeng.dingtalk.vo.UserInfoVO;
import com.softeng.dingtalk.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author zhanyeye
 * @description
 * @create 1/10/2020 8:38 PM
 */

@Slf4j
@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    UserService userService;
    @Autowired
    NotifyService notifyService;
    @Autowired
    BaseApi baseApi;
    @Resource
    UserContextHolder userContextHolder;
    @Resource
    TeamConvertor teamConvertor;
    @Resource
    PermissionConvertor permissionConvertor;


    /**
     * 获取用户信息, 用户登录后调用
     * @param uid
     * @return java.util.Map
     * @Date 4:57 PM 1/13/2020
     **/
    @GetMapping("/user/info")
    public Map getInfo(@RequestAttribute int uid) {
        return userService.getUserInfo(uid);
    }


    /**
     * 查询所有审核人
     * @return
     */
    @GetMapping("/user/getAuditors")
    public Map getAuditors() {
        return userService.getAuditorUser();
    }


    /**
     * 查询系统中所有可用用户
     * @return
     */
    @GetMapping("/userlist")
    public List<UserVO> listusers() {
        return userService.listUserVO();
    }


    /**
     * 钉钉鉴权
     * @param map
     * @return
     */
    @PostMapping("/jsapi_signature")
    public Map jspai(@RequestBody Map<String, String> map) {
        return baseApi.authentication(map.get("url"));
    }


    /**
     * 获取用户的消息
     * @param page
     * @param size
     * @param uid
     * @return
     */
    @GetMapping("/message/page/{page}/{size}")
    public Map listUserMessage(@PathVariable int page, @PathVariable int size, @RequestAttribute int uid) {
        Page<Message> messages = notifyService.listUserMessage(uid, page, size);
        return Map.of("content", messages.getContent(), "total", messages.getTotalElements());
    }


    /**
     * 更新用户权限
     * @param map
     */
    @AccessPermission(PermissionEnum.EDIT_ANY_USER_INFO)
    @PostMapping("/updaterole")
    public void updateUserRole(@RequestBody Map<String, Object> map) {
        userService.updateRole((int) map.get("uid"), (int) map.get("authority"));
    }


    /**
     * 获取用户信息
     * @param uid
     * @return
     */
    @GetMapping("/user/detail")
    public UserInfoVO getUserDetail(@RequestAttribute int uid){
        return userService.getUserDetail(uid);
    }


    @PostMapping("/user/update")
    public void updateUserInfo(@RequestBody UserInfoVO userInfoVO, @RequestAttribute int uid) {
        userService.updateUserInfo(userInfoVO, uid);
    }

    @PostMapping("/user/leaseContract")
    public void saveLeaseContract(@RequestParam MultipartFile file, @RequestAttribute int uid) {
        userService.saveLeaseContractFile(file,uid);
    }

    @GetMapping("/user/leaseContract")
    public void downloadLeaseContract(@RequestAttribute int uid, HttpServletResponse response) throws IOException {
        userService.downloadContractFile(uid,response);
    }

    /**
     * @author LiXiaoKang
     * @description 新增获取用户权限与所属组信息
     * @create 1/10/2023 8:38 PM
     */

    /**
     * 获取用户权限信息
     * @param uid
     * @return
     */
    @GetMapping("/v2/user/permission")
    public CommonResult<List<PermissionResp>> getPermissions(@RequestAttribute int uid){
        return CommonResult.success(StreamUtils.map(
                userService.getPermissions(userContextHolder.getUserContext().getUid()),
                permission -> permissionConvertor.entity_PO2Resp(permission)
        ));
    }

    @GetMapping("/v2/user/team")
    public CommonResult<List<TeamResp>> getTeams(@RequestAttribute int uid){
        return CommonResult.success(StreamUtils.map(
                userService.getTeams(userContextHolder.getUserContext().getUid()),
                team -> teamConvertor.entity_PO2Resp(team)
        ));
    }
}
