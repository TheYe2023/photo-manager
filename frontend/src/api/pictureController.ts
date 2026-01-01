// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /picture/chat */
export async function doChat(body: API.ChatRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/picture/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /picture/delete */
export async function deletePicture(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/picture/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /picture/edit */
export async function editPicture(body: API.PictureEditRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/picture/edit', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /picture/get */
export async function getPictureById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getPictureByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePicture>('/picture/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /picture/get/vo */
export async function getPictureVoById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getPictureVOByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePictureVO>('/picture/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /picture/list/page */
export async function listPictureByPage(
  body: API.PictureQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePagePicture>('/picture/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /picture/list/page/vo */
export async function listPictureVoByPage(
  body: API.PictureQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePagePictureVO>('/picture/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /picture/review */
export async function doPictureReview(
  body: API.PictureReviewRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/picture/review', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /picture/tag_category */
export async function listPictureTagCategory(options?: { [key: string]: any }) {
  return request<API.BaseResponsePictureTagCategory>('/picture/tag_category', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /picture/tag_category/create_task */
export async function createAiPictureAnalyzeTask(
  body: API.CreatePictureAnalysisTaskRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseListString>('/picture/tag_category/create_task', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /picture/update */
export async function updatePicture(
  body: API.PictureUpdateRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/picture/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /picture/upload */
export async function uploadPicture(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.uploadPictureParams,
  body: {},
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePictureVO>('/picture/upload', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    params: {
      ...params,
      pictureUploadRequest: undefined,
      ...params['pictureUploadRequest'],
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /picture/upload/url */
export async function uploadPictureByUrl(
  body: API.PictureUploadRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePictureVO>('/picture/upload/url', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
