import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { health } from '@/api/mainController'
import HomePage from "@/pages/HomePage.vue";
import UserLoginPage from '@/pages/user/UserLoginPage.vue';
import UserRegisterPage from '@/pages/user/UserRegisterPage.vue';
import UserManagePage from '@/pages/admin/UserManagePage.vue';
import ACCESS_ENUM from '@/access/accessEnum';
import AddPicturePage from '@/pages/AddPicturePage.vue';
import PictureManagePage from '@/pages/admin/PictureManagePage.vue';
import PictureDetailPage from '@/pages/PictureDetailPage.vue';
import SpaceManagePage from '@/pages/admin/SpaceManagePage.vue';
import AddSpacePage from '@/pages/AddSpacePage.vue';
import MySpacePage from '@/pages/MySpacePage.vue';
import SpaceDetailPage from '@/pages/SpaceDetailPage.vue';

health().then((res) => {
  console.log(res)
})


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '主页',
      component: HomePage,
    },
    {
      path: "/user/login",
      name: "用户登录",
      component: UserLoginPage,
    },
    {
      path: "/user/register",
      name: "用户注册",
      component: UserRegisterPage,
    },
    {
      path: "/admin/userManage",
      name: "用户管理",
      component: UserManagePage,
      meta: {
        access: ACCESS_ENUM.ADMIN
      },
    },
    {
      path: '/add_picture',
      name: '创建图片',
      component: AddPicturePage,
    },
    {
      path: '/admin/pictureManage',
      name: '图片管理',
      component: PictureManagePage,
      meta: {
        access: ACCESS_ENUM.ADMIN
      },
    },
    {
      path: '/picture/:id',
      name: '图片详情',
      component: PictureDetailPage,
      props: true,
    },
    {
      path: '/admin/spaceManage',
      name: '空间管理',
      component: SpaceManagePage,
      meta: {
          access: ACCESS_ENUM.ADMIN
      },
    },
    {
      path: '/add_space',
      name: '创建空间',
      component: AddSpacePage,
    },
    {
      path: '/my_space',
      name: '我的空间',
      component: MySpacePage,
    },
    {
      path: '/space/:id',
      name: '空间详情',
      component: SpaceDetailPage,
      props: true,
    },
  ],
})

export default router
