<template>
    <el-header class="app-header">
        <div class="header-left">
            <el-icon class="collapse-btn" @click="toggleSidebar" v-if="isAdmin">
                <Fold />
            </el-icon>
            <router-link to="/" class="logo">
                <el-icon :size="24"><Film /></el-icon>
                <span>电影院购票系统</span>
            </router-link>
        </div>
        <div class="header-right">
            <template v-if="userStore.isLoggedIn">
                <el-dropdown trigger="click">
                    <span class="user-info">
                        <el-avatar :size="32" icon="UserFilled" />
                        <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
                    </span>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item @click="$router.push('/profile')">
                                <el-icon><User /></el-icon>个人中心
                            </el-dropdown-item>
                            <el-dropdown-item @click="$router.push('/orders')">
                                <el-icon><List /></el-icon>我的订单
                            </el-dropdown-item>
                            <el-dropdown-item v-if="userStore.isAdmin" @click="$router.push('/admin/dashboard')">
                                <el-icon><Setting /></el-icon>管理后台
                            </el-dropdown-item>
                            <el-dropdown-item divided @click="handleLogout">
                                <el-icon><SwitchButton /></el-icon>退出登录
                            </el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
            </template>
            <template v-else>
                <el-button text @click="$router.push('/login')">登录</el-button>
                <el-button text @click="$router.push('/register')">注册</el-button>
            </template>
        </div>
    </el-header>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { useRouter } from 'vue-router'
import { computed } from 'vue'

const userStore = useUserStore()
const appStore = useAppStore()
const router = useRouter()

const isAdmin = computed(() => userStore.isAdmin)

function toggleSidebar() {
    appStore.toggleSidebar()
}

function handleLogout() {
    userStore.logout()
    router.push('/login')
}
</script>

<style scoped lang="scss">
.app-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: #fff;
    border-bottom: 1px solid #e4e7ed;
    height: 60px;
    padding: 0 20px;
}

.header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .logo {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 18px;
        font-weight: 600;
        color: #303133;
        text-decoration: none;
    }

    .collapse-btn {
        font-size: 20px;
        cursor: pointer;
        color: #909399;
    }
}

.header-right {
    display: flex;
    align-items: center;
    gap: 8px;

    .user-info {
        display: flex;
        align-items: center;
        gap: 8px;
        cursor: pointer;
        padding: 4px 8px;
        border-radius: 4px;

        &:hover {
            background: #f5f7fa;
        }

        .username {
            font-size: 14px;
            color: #303133;
        }
    }
}
</style>
