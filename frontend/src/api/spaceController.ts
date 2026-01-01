// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /space/add */
export async function addSpace(body: API.SpaceAddRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseLong>('/space/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /space/delete */
export async function deleteSpace(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/space/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /space/edit */
export async function editSpace(body: API.SpaceEditRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/space/edit', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /space/get */
export async function getSpaceById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getSpaceByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseSpace>('/space/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /space/get/vo */
export async function getSpaceVoById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getSpaceVOByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseSpaceVO>('/space/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /space/list/level */
export async function listSpaceLevel(options?: { [key: string]: any }) {
  return request<API.BaseResponseListSpaceLevel>('/space/list/level', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /space/list/page */
export async function listSpaceByPage(
  body: API.SpaceQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageSpace>('/space/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /space/list/page/vo */
export async function listSpaceVoByPage(
  body: API.SpaceQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageSpaceVO>('/space/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /space/update */
export async function updateSpace(body: API.SpaceUpdateRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/space/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
