import request from '@/utils/request'

// 查询流程单列表
export function listProcessForm(query) {
  return request({
    url: '/workflow/processForm/list',
    method: 'get',
    params: query
  })
}

// 查询流程单详细
export function getProcessForm(id) {
  return request({
    url: '/workflow/processForm/' + id,
    method: 'get'
  })
}

// 新增流程单
export function addProcessForm(data) {
  return request({
    url: '/workflow/processForm',
    method: 'post',
    data: data
  })
}

// 修改流程单
export function updateProcessForm(data) {
  return request({
    url: '/workflow/processForm',
    method: 'put',
    data: data
  })
}

// 修改流程单
export function editForm(data) {
  return request({
    url: '/workflow/processForm/editForm',
    method: 'put',
    data: data
  })
}

// 删除流程单
export function delProcessForm(id) {
  return request({
    url: '/workflow/processForm/' + id,
    method: 'delete'
  })
}
