package com.libraryms.servlet;

import com.libraryms.dao.UserDao;
import com.libraryms.entity.Admin;
import com.libraryms.entity.Operator;
import com.libraryms.entity.Student;
import com.libraryms.entity.Teacher;
import com.libraryms.entity.User;
import com.libraryms.util.BizException;
import com.libraryms.util.IdUtil;
import com.libraryms.util.Result;
import com.libraryms.util.UserFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** 用户注册：角色联动字段 + 同名/手机号重复校验 */
@WebServlet("/api/register")
public class RegisterServlet extends BaseServlet {

    private final UserDao userDao = new UserDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        run(resp, () -> {
            String name = param(req, "name");
            String password = param(req, "password");
            String phone = param(req, "phone");
            String major = param(req, "major");
            String role = UserFactory.normalize(param(req, "role"));

            if (name == null || name.trim().isEmpty()) throw new BizException("请输入姓名");
            if (password == null || password.trim().isEmpty()) throw new BizException("请输入密码");
            if (phone == null || !phone.trim().matches("^1[3-9]\\d{9}$")) {
                throw new BizException("手机号格式不正确，应为 11 位中国大陆手机号");
            }
            if (userDao.existsName(name.trim())) throw new BizException("同名账号不能重复注册");
            if (userDao.existsPhone(phone.trim())) throw new BizException("该手机号已被注册");

            User user = UserFactory.create(role);
            LocalDateTime now = LocalDateTime.now();
            user.setId(IdUtil.uuid());
            user.setCreateTime(now);
            user.setUpdateTime(now);
            user.setName(name.trim());
            user.setPassword(password);
            user.setPhone(phone.trim());
            user.setRole(role);
            user.setMajor(major);

            // 按角色填写子类专有信息（注册页面会随角色动态显示对应输入项）
            if (user instanceof Admin) {
                ((Admin) user).setPermissionLevel(intParam(req, "permissionLevel", 1));
                String entry = param(req, "entryTime");
                if (entry != null && !entry.isEmpty()) ((Admin) user).setEntryTime(LocalDate.parse(entry));
            } else if (user instanceof Operator) {
                ((Operator) user).setDuty(param(req, "duty"));
                ((Operator) user).setArea(param(req, "area"));
            } else if (user instanceof Teacher) {
                ((Teacher) user).setCollege(param(req, "college"));
                ((Teacher) user).setTitle(param(req, "title"));
            } else {
                ((Student) user).setGrade(param(req, "grade"));
                ((Student) user).setCollege(param(req, "college"));
            }
            userDao.save(user);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", user.getId());
            data.put("name", user.getName());
            data.put("role", user.getRole());
            return Result.ok("注册成功，请登录", data);
        });
    }
}
