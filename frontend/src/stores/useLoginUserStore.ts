import { defineStore } from "pinia";
import { ref } from "vue";
import { getLoginUser } from '@/api/userController';

export const useLoginUserStore = defineStore("loginUser", () => {
  const loginUser = ref<API.LoginUserVO>({
    userName: "未登录",
  });

async function fetchLoginUser() {
  const res = await getLoginUser();
  if (res.data.code === 0 && res.data.data) {
    loginUser.value = res.data.data;
  }
}

  /**
   * 设置登录用户
   * @param newLoginUser
   */
  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser;
  }

  return { loginUser, setLoginUser, fetchLoginUser };
});
