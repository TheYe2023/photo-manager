<template>
  <div id="homePage">
    <!-- 搜索框 -->
    <div class="search-bar">
      <a-input-search
        placeholder="从海量图片中搜索"
        v-model:value="searchParams.searchText"
        enter-button="搜索"
        size="large"
        @search="doSearch"
      >
      </a-input-search>
    </div>
    <AiChatDrawer ref="chatDrawerRef" />
    <div class="ai-float-button" @click="onOpenChat">
      <span style="font-size: 24px;">🤖</span>
    </div>
    <!-- 分类 + 标签 -->
    <a-tabs v-model:activeKey="selectedCategory" @change="doSearch">
      <a-tab-pane key="all" tab="全部" />
      <a-tab-pane v-for="category in categoryList" :key="category" :tab="category" />
    </a-tabs>
    <div class="tag-bar">
      <span style="margin-right: 8px">标签：</span>
      <a-space :size="[0, 8]" wrap>
        <a-checkable-tag
          v-for="(tag, index) in tagList"
          :key="tag"
          v-model:checked="selectedTagList[index]"
          @change="doSearch"
        >
          {{ tag }}
        </a-checkable-tag>
      </a-space>
    </div>

    <!-- 图片列表 -->
    <PictureList :dataList="dataList" :loading="loading" />
    <!-- 分页 -->
    <a-pagination
      style="text-align: right"
      v-model:current="searchParams.current"
      v-model:pageSize="searchParams.pageSize"
      :total="total"
      @change="onPageChange"
    />
  </div>

</template>

<script setup lang="ts">
  import { onMounted, reactive, ref } from 'vue'
  import {
    listPictureTagCategory,
    listPictureVoByPage
  } from '@/api/pictureController'
  import { message } from 'ant-design-vue'
  import PictureList from '@/components/PictureList.vue'
  import AiChatDrawer from '@/components/AiChatDrawer.vue'


  // ai 聊天抽屉
  const chatDrawerRef = ref()
  // 数据
  const dataList = ref<API.PictureVO[]>([])
  const total = ref(0)
  const loading = ref(true)

  // 定义打开 AI 助手的方法
  const onOpenChat = () => {
    chatDrawerRef.value?.openDrawer()
  }

  // 搜索条件
  const searchParams = reactive<API.PictureQueryRequest>({
    current: 1,
    pageSize: 12,
    sortField: 'createTime',
    sortOrder: 'descend',
  })

  // 分页参数
  const onPageChange = (page: number, pageSize: number) => {
    searchParams.current = page
    searchParams.pageSize = pageSize
    fetchData()
  }

  const doSearch = () => {
    // 重置搜索条件
    searchParams.current = 1
    fetchData()
  }

  // 获取数据
  const fetchData = async () => {
    loading.value = true
    // 转换搜索参数
    const params = {
      ...searchParams,
      tags: [] as string[],
    }
    if (selectedCategory.value !== 'all') {
      params.category = selectedCategory.value
    }
    // [true, false, false] => ['java']
    selectedTagList.value.forEach((useTag, index) => {
      if (useTag) {
        params.tags.push(tagList.value[index])
      }
    })
    const res = await listPictureVoByPage(params);
    if (res.data.code === 0 && res.data.data) {
      dataList.value = res.data.data.records ?? []
      total.value = res.data.data.total ?? 0
    } else {
      message.error('获取数据失败，' + res.data.message)
    }
    loading.value = false
  }

  // 标签和分类列表
  const categoryList = ref<string[]>([])
  const selectedCategory = ref<string>('all')
  const tagList = ref<string[]>([])
  const selectedTagList = ref<boolean[]>([])

  /**
   * 获取标签和分类选项
   * @param values
   */
  const getTagCategoryOptions = async () => {
    const res = await listPictureTagCategory
    if (res.data.code === 0 && res.data.data) {
      tagList.value = res.data.data.tagList ?? []
      categoryList.value = res.data.data.categoryList ?? []
    } else {
      message.error('获取标签分类列表失败，' + res.data.message)
    }
  }

  // 页面加载时请求一次
  onMounted(() => {
    getTagCategoryOptions()
    fetchData()
  })
</script>

<style scoped>
#homePage {
  margin-bottom: 16px;
}

#homePage .search-bar {
  max-width: 480px;
  margin: 0 auto 16px;
}

#homePage .tag-bar {
  margin-bottom: 16px;
}

.ai-float-button {
  position: fixed;
  right: 40px;
  bottom: 80px;
  width: 56px;
  height: 56px;
  background: #1890ff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  transition: all 0.3s;
  z-index: 1000;
}

.ai-float-button:hover {
  transform: scale(1.1);
  background: #1890ff;
}
</style>
