import axios from 'axios';
export const api=axios.create({baseURL:'/api',timeout:15000});
api.interceptors.request.use(config=>{const token=sessionStorage.getItem('onlinelearn.token');if(token)config.headers.Authorization=`Bearer ${token}`;return config});
export const message=e=>e.response?.data?.errors?.join(' · ')||e.response?.data?.message||'Không thể kết nối máy chủ. Vui lòng thử lại.';
export const money=value=>value==null?'Chưa có gói học':new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND'}).format(value);
