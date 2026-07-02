import { useUserStore } from '@/stores/user'
import { computed } from 'vue'

export function useAuth() {
    const userStore = useUserStore()

    const isLoggedIn = computed(() => userStore.isLoggedIn)
    const isAdmin = computed(() => userStore.isAdmin)
    const currentUser = computed(() => userStore.userInfo)

    return {
        isLoggedIn,
        isAdmin,
        currentUser,
        login: userStore.login,
        logout: userStore.logout,
        fetchUserInfo: userStore.fetchUserInfo,
    }
}
