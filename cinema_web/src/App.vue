<template>
    <router-view />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

onMounted(async () => {
    if (userStore.isLoggedIn) {
        try {
            await userStore.fetchUserInfo()
            ElMessage.success('读取本地缓存信息，已自动登录')
        } catch {
            // token 已失效，清除本地缓存
            userStore.logout()
        }
    }
})
</script>
