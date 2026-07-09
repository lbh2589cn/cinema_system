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
            ElMessage.success('Restored session from local cache')
        } catch {
            // Token expired, clearing local cache
            userStore.logout()
        }
    }
})
</script>
