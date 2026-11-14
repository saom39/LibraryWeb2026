/* 公共脚本：Axios 请求封装 + 登录守卫 + 导航栏 + 提示框（jQuery 辅助 DOM 操作） */
window.Lib = (function () {
    const api = axios.create({ baseURL: './', timeout: 20000 });

    api.interceptors.response.use(function (response) {
        return response;
    }, function (error) {
        if (error.response && error.response.status === 401) {
            window.location.href = 'login.html';
            return new Promise(function () { });
        }
        toast('网络或服务器异常：' + (error.message || ''), 'err');
        return Promise.reject(error);
    });

    async function get(url, params) {
        const r = await api.get(url, { params: params || {} });
        return r.data;
    }

    async function post(url, data) {
        const r = await api.post(url, data || {});
        return r.data;
    }

    /** 检查登录状态；requireLogin=true 时未登录自动跳转 */
    async function currentUser(requireLogin) {
        const r = await get('api/auth/current');
        if (!r.success) {
            if (requireLogin !== false) window.location.href = 'login.html';
            return null;
        }
        return r.data;
    }

    function toast(message, type) {
        type = type || 'info';
        let $el = $('#__lib_toast');
        if (!$el.length) {
            $('body').append('<div id="__lib_toast" class="toast"></div>');
            $el = $('#__lib_toast');
        }
        $el.attr('class', 'toast ' + type).text(message).addClass('show');
        clearTimeout(window.__toastTimer);
        window.__toastTimer = setTimeout(function () { $el.removeClass('show'); }, 3200);
    }

    function handle(r, onOk) {
        if (r && r.success) {
            toast(r.message || '操作成功', 'ok');
            if (onOk) onOk(r.data);
        } else {
            toast((r && r.message) || '操作失败', 'err');
        }
        return r;
    }

    function fmt(v) {
        if (v === null || v === undefined || v === '') return '-';
        return String(v).replace('T', ' ').substring(0, 19);
    }

    function cards(role) {
        const all = {
            '管理员': [
                ['用户信息查询', '模糊查询用户，查看详情（只读）', 'users.html'],
                ['借阅记录查询', '关键字 / 超期 / 日期范围三种查询', 'loans.html'],
                ['借阅数据统计', '按图书类型、按用户专业归集统计', 'stats.html'],
                ['借书', '查看可借图书并办理借阅', 'borrow.html#borrow'],
                ['还书', '按副本 id 归还图书', 'borrow.html#return'],
                ['审阅推荐图书', '通过入库或填写理由驳回', 'recommend.html']
            ],
            '操作员': [
                ['添加书籍', '录入新书并按库存自动生成副本', 'books.html#add'],
                ['查看所有书籍', '关键字模糊查询，空关键字查全部', 'books.html'],
                ['废弃图书', '副本全部归还后才可废弃', 'books.html'],
                ['编辑图书', '仅更新书名、作者、类型、出版社、期限、价格', 'books.html'],
                ['图书副本编辑', '新增副本库存+1，删除副本库存-1', 'books.html#copies'],
                ['借书', '查看可借图书并办理借阅', 'borrow.html#borrow'],
                ['还书', '按副本 id 归还图书', 'borrow.html#return'],
                ['个人借阅查询', '全部 / 未还 / 日期范围', 'loans.html']
            ],
            '学生': [
                ['借书', '查看可借图书并办理借阅', 'borrow.html#borrow'],
                ['还书', '按副本 id 归还图书', 'borrow.html#return'],
                ['个人借阅查询', '全部 / 未还 / 日期范围', 'loans.html']
            ],
            '教师': [
                ['借书', '查看可借图书并办理借阅', 'borrow.html#borrow'],
                ['还书', '按副本 id 归还图书', 'borrow.html#return'],
                ['个人借阅查询', '全部 / 未还 / 日期范围', 'loans.html'],
                ['推荐图书', '馆内无相关书籍时提交审阅', 'recommend.html']
            ]
        };
        return all[role] || all['学生'];
    }

    /** 渲染顶部导航；active 为当前页面标识 */
    function mountNavbar(user, active) {
        const links = [
            ['index.html', '首页', 'index'],
            ['books.html', '图书', 'books'],
            ['borrow.html', '借还', 'borrow'],
            ['loans.html', '借阅记录', 'loans'],
            ['users.html', '用户查询', 'users'],
            ['stats.html', '统计', 'stats'],
            ['recommend.html', '推荐审阅', 'recommend']
        ];
        let html = '<div class="topbar"><div class="brand">图书馆管理系统<small>LibraryWeb2026</small></div><div class="nav-links">';
        links.forEach(function (l) {
            html += '<a href="' + l[0] + '" class="' + (active === l[2] ? 'active' : '') + '">' + l[1] + '</a>';
        });
        html += '</div><div class="user-box"><span class="role-tag">' + (user.role || '') + '</span>'
            + '<span>' + (user.name || '') + '</span><button class="btn-ghost btn-sm" id="logoutBtn">退出</button></div></div>';
        $('#navbar').html(html);
        $('#logoutBtn').on('click', async function () {
            await post('api/auth/logout');
            window.location.href = 'login.html';
        });
    }

    function reloadCaptcha($img) {
        $img.attr('src', 'api/captcha?t=' + Date.now());
    }

    return {
        api: api, get: get, post: post, currentUser: currentUser, toast: toast,
        handle: handle, fmt: fmt, cards: cards, mountNavbar: mountNavbar,
        reloadCaptcha: reloadCaptcha, confirm: function (msg) { return window.confirm(msg); }
    };
})();
