package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     *
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        /**
         1、将页面提交的密码password进行md5加密处理
         2、根据页面提交的用户名username查询数据库
         3、如果没有查询到则返回登录失败结果
         4、密码比对，如果不一致则返回登录失败结果
         5、查看员工状态，如果是禁用状态，则返回员工已禁用结果
         6、登陆成功、将员工id存入Session并返回登录结果
         */

        // 1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

//        3、如果没有查询到则返回登录失败结果
        if (emp == null) {
            return R.error("登录失败，用户名不存在");
        }
//      4、密码比对，如果不一致则返回登录失败结果
        if (!emp.getPassword().equals(password)) {
            return R.error("登录失败，密码错误");
        }

        //            5、查看员工状态，如果是禁用状态，则返回员工已禁用结果
        if (emp.getStatus() == 0) {
            return R.error("账号已禁用");
        }
//        6、登陆成功、将员工id存入Session并返回登录结果
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }

    /**
     * 退出功能开发
     * @param request
     * @return String
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param employee
     * @return string
     */
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee) {
        log.info("新增员工，员工信息: {}",employee.toString());
//        设置初始密码，需要md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("12345".getBytes()));
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
//        获取当前登录用户id
        Long empId = (Long) request.getSession().getAttribute("employee");
        employee.setUpdateUser((empId));
        employee.setCreateUser(empId);

        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    /**
     * 员工信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page = {}, pageSize = {}, name = {}", page, pageSize, name);

        // 构造分页构造器
        Page pageInfo = new Page(page, pageSize);
//        条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
//        添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
//        排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        employeeService.page(pageInfo, queryWrapper);
//        执行查询
        return R.success(pageInfo);
    }
}
