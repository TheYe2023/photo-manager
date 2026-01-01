import { defineStore } from "pinia";
import { ref } from "vue";

export const useChatStore = defineStore("chat", () => {
  // 1. 定义初始欢迎语（常量）
  const WELCOME_MESSAGE = {
    role: 'assistant',
    content: '你好！我是你的 **PhotoManager** 智能管家。你可以告诉我你想寻找的图片名字、介绍、分类或标签，我会直接为你展示结果。'
  };

  // 2. 定义状态：初始化时尝试从欢迎语开始
  const messages = ref([WELCOME_MESSAGE]);

  /**
   * 添加消息
   * @param msg 消息对象 { role: 'user' | 'assistant', content: string }
   */
  function addMessage(msg: { role: string; content: string }) {
    messages.value.push(msg);
  }

  /**
   * 清除聊天记录（保留欢迎语）
   */
  function clearMessages() {
    messages.value = [WELCOME_MESSAGE];
  }

  // 返回给组件使用
  return { messages, addMessage, clearMessages };
}, {
  // 3. 核心：开启持久化配置
  persist: true,
});
