import { h } from 'vue'
import { HomeOutlined } from '@ant-design/icons-vue'
import HomeView from '@/views/HomeView.vue'
import UserLoginPage from '@/pages/user/UserLoginPage.vue'
import UserRegisterPage from '@/pages/user/UserRegisterPage.vue'
import UserManagePage from '@/pages/admin/UserManagePage.vue'
import ACCESS_ENUM from '@/access/accessEnum'

const routes = [
  {
    path: '/',
    name: '主页',
    component: HomeView,
    meta: {
      icon: () => h(HomeOutlined),
    },
  },
  {
    path: '/user/login',
    name: '用户登录',
    component: UserLoginPage,
    meta: {
      hideInMenu: true
    }
  },
  {
    path: '/user/register',
    name: '用户注册',
    component: UserRegisterPage,
    meta: {
      hideInMenu: true
    }
  },
  {
    path: '/admin/userManage',
    name: '用户管理',
    component: UserManagePage,
    meta: {
      access: ACCESS_ENUM.ADMIN,
    },
  },
  // {
  //   path: '/picture/manage',
  //   name: '图片管理',
  //   component: PictureManagePage,
  //   meta: {
  //     access: ACCESS_ENUM.ADMIN,
  //   },
  // },
]
export default routes
