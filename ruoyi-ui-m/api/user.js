import request from '@/config/request.js';

// 获取用户信息
export const getInfo = () => request.get('/getInfo')
// 修改用户个人信息
export const updateUserProfile = (data) => request.put('/system/user/profile',data)
