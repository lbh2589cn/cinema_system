import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginApi, getProfileApi } from '@/api/auth'
import type { LoginRequest, UserProfile } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const userInfo = ref<UserProfile | null>(null)

    const isLoggedIn = computed(() => !!token.value)
    const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')
    const userId = computed(() => userInfo.value?.id)

    async function login(credentials: LoginRequest) {
        const res = await loginApi(credentials)
        token.value = res.token
        userInfo.value = { id: res.id, userId: res.userId, username: res.username, role: res.role, phone: '', isMember: res.isMember, status: 'ACTIVE' }
        localStorage.setItem('token', res.token)
    }

    async function fetchUserInfo() {
        try {
            const res = await getProfileApi()
            userInfo.value = res
        } catch {
            logout()
        }
    }

    function logout() {
        token.value = ''
        userInfo.value = null
        localStorage.removeItem('token')
    }

    return { token, userInfo, isLoggedIn, isAdmin, userId, login, fetchUserInfo, logout }
})
