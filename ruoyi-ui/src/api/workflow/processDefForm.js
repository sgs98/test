import request from '@/utils/request'

// 查询流程定义与单配置列表
export function listProcessDefForm(query) {
  return request({
    url: '/workflow/processDefForm/list',
    method: 'get',
    params: query
  })
}

// 查询流程定义与单配置详细
export function getProcessDefForm(id) {
  return request({
    url: '/workflow/processDefForm/' + id,
    method: 'get'
  })
}

// 按流程定义id查询流程定义与单配置详细
export function getProcessDefFormByDefId(id) {
  return request({
    url: '/workflow/processDefForm/getProcessDefFormByDefId/' + id,
    method: 'get'
  })
}

// 校验表单是否关联
export function checkProcessDefFormByDefId(defId,formId) {
  return request({
    url: `/workflow/processDefForm/checkProcessDefFormByDefId/${defId}/${formId}` ,
    method: 'get'
  })
}

// 新增流程定义与单配置
export function addProcessDefForm(data) {
  return request({
    url: '/workflow/processDefForm',
    method: 'post',
    data: data
  })
}

// 修改流程定义与单配置
export function updateProcessDefForm(data) {
  return request({
    url: '/workflow/processDefForm',
    method: 'put',
    data: data
  })
}

// 删除流程定义与单配置
export function delProcessDefForm(id) {
  return request({
    url: '/workflow/processDefForm/' + id,
    method: 'delete'
  })
}
