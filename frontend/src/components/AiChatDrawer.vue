<template>
  <a-drawer
    title="AI 智能助手"
    placement="right"
    :width="420"
    :visible="visible"
    :bodyStyle="{ padding: 0, display: 'flex', flexDirection: 'column', background: '#f5f5f5' }"
    @close="onClose"
  >
  <template #extra>
    <a-tooltip title="清空聊天记录">
      <a-button type="text" danger @click="handleClear">
        <template #icon><DeleteOutlined /></template>
      </a-button>
    </a-tooltip>
  </template>
    <div class="chat-container">
      <div class="message-list" ref="chatBox" @click="handleMessageClick">
        <div
          v-for="(msg, index) in chatStore.messages"
          :key="index"
          :class="['message-wrapper', msg.role]"
        >
          <div class="avatar">
            <a-avatar v-if="msg.role === 'assistant'" style="background-color: #1890ff">
              <template #icon>🤖</template>
            </a-avatar>
            <a-avatar v-else :src="loginUserStore.loginUser.userAvatar">
            </a-avatar>
          </div>
          <div class="message-body">
            <div class="content" v-html="renderMarkdown(msg.content)"></div>
          </div>
        </div>
        <div v-if="loading" class="loading-status">
          <a-spin size="small" />
          <span class="loading-text">AI 正在图库中搜寻...</span>
        </div>
      </div>

      <div class="input-area">
        <div class="input-wrapper">
          <a-textarea
            v-model:value="searchText"
            placeholder="试试：帮我找找猫猫的图片"
            :auto-size="{ minRows: 2, maxRows: 4 }"
            @pressEnter.prevent="onSendMessage"
          />
          <div class="input-actions">
            <span class="tip">Enter 发送</span>
            <a-button type="primary" :loading="loading" @click="onSendMessage">
              发送
            </a-button>
          </div>
        </div>
      </div>
    </div>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue';
import MarkdownIt from 'markdown-it';
import { message, Modal } from 'ant-design-vue';
import { SyncOutlined } from '@ant-design/icons-vue';
import { doChat } from '@/api/pictureController';
import { useLoginUserStore } from "@/stores/useLoginUserStore";
import { useChatStore } from "@/stores/useChatStore";
import { useRouter } from 'vue-router'
import { DeleteOutlined } from '@ant-design/icons-vue';

const chatStore = useChatStore();
const router = useRouter();

// 处理消息列表内的所有点击
const handleMessageClick = (event: MouseEvent) => {
  const target = event.target as HTMLElement;

  // 检查点击的是否是图片
  if (target.tagName === 'IMG') {
    // 获取我们在后端埋下的 data-id
    const pictureId = target.getAttribute('data-id');

    if (pictureId) {
      // 执行跳转
      router.push(`/picture/${pictureId}`);
      // 可选：跳转后关闭抽屉
      visible.value = false;
    }
  }
};

const handleClear = () => {
  Modal.confirm({
    title: '确认清空聊天记录吗？',
    okText: '确认',
    cancelText: '取消',
    onOk() {
      chatStore.clearMessages();
      message.success('记录已清空');
    },
  });
};

const loginUserStore = useLoginUserStore();

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
});

const visible = ref(false);
const loading = ref(false);
const searchText = ref('');

const onSendMessage = async () => {
  if (!searchText.value.trim() || loading.value) return;

  const userMsg = searchText.value;
  chatStore.addMessage({ role: 'user', content: userMsg });

  searchText.value = '';
  loading.value = true;
  scrollToBottom();

  try {
    const res = await doChat({ message: userMsg });
    if (res.data.code === 0) {
      chatStore.addMessage({ role: 'assistant', content: res.data.data });
    } else {
      message.error('AI 失败：' + res.data.message);
    }
  } catch (e: any) {
    message.error('网络拥塞，请稍后再试');
  } finally {
    loading.value = false;
    scrollToBottom();
  }
};
const chatBox = ref<HTMLElement>();

const renderMarkdown = (content: string) => {
  return md.render(content);
};

const scrollToBottom = () => {
  nextTick(() => {
    if (chatBox.value) {
      chatBox.value.scrollTo({
        top: chatBox.value.scrollHeight,
        behavior: 'smooth'
      });
    }
  });
};

const onClose = () => { visible.value = false; };

const openDrawer = () => {
  visible.value = true;
  // 打开时延迟滚动到底部，确保 DOM 已渲染
  setTimeout(() => {
    scrollToBottom();
  }, 100);
};

defineExpose({ openDrawer });
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 55px); /* 减去 Drawer Header 高度 */
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px 16px;
}

.message-wrapper {
  display: flex;
  margin-bottom: 24px;
  align-items: flex-start;
}

.message-wrapper.user {
  flex-direction: row-reverse;
}

.avatar {
  flex-shrink: 0;
  margin: 0 12px;
}

.message-body {
  max-width: 75%;
}

.content {
  padding: 10px 14px;
  border-radius: 12px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  font-size: 14px;
  color: #333;
  word-break: break-all;
}

.assistant .content {
  border-top-left-radius: 0;
  border: 1px solid #e8e8e8;
}

.user .content {
  background: #1890ff;
  color: white;
  border-top-right-radius: 0;
}

.loading-status {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.loading-text {
  margin-left: 8px;
  color: #999;
  font-size: 12px;
}

/* 输入区域美化 */
.input-area {
  padding: 16px;
  background: white;
  border-top: 1px solid #eee;
}

.input-wrapper {
  background: #f9f9f9;
  border-radius: 8px;
  padding: 8px;
  border: 1px solid #d9d9d9;
  transition: all 0.3s;
}

.input-wrapper:focus-within {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
  background: white;
}

.input-wrapper :deep(textarea) {
  background: transparent;
  border: none;
  box-shadow: none;
  resize: none;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  padding-top: 4px;
}

.tip {
  font-size: 12px;
  color: #bfbfbf;
}

:deep(.content img:hover) {
  transform: scale(1.02);
}

:deep(.content p) {
  margin-bottom: 0;
}

/* 自定义滚动条 */
.message-list::-webkit-scrollbar {
  width: 4px;
}
.message-list::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 2px;
}

:deep(.content img) {
  max-width: 100%;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  display: block;
  margin: 10px 0;
}

:deep(.content img:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
  filter: brightness(0.9);
}

:deep(.content img:not([src])) {
  visibility: hidden;
}
:deep(.content img::before) {
  content: "🖼️ 图片加载失败";
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100px;
  background: #f5f5f5;
  color: #999;
}
:deep(.ant-drawer-extra) {
  display: flex;
  align-items: center;
}
</style>
