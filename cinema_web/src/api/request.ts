import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
    timeout: 15000,
})

request.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => Promise.reject(error)
)

request.interceptors.response.use(
    (response) => {
        const res = response.data
        if (res.code !== 200) {
            ElMessage.error(res.message || 'Request failed')
            return Promise.reject(new Error(res.message))
        }
        return res.data
    },
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('token')
            router.push('/login')
            ElMessage.error('Session expired, please sign in again')
        } else {
            ElMessage.error(error.response?.data?.message || 'Network error')
        }
        return Promise.reject(error)
    }
)

export default request
