<template>
    <el-header class="app-header">
        <div class="header-left">
            <router-link to="/" class="logo">
                <el-icon :size="24"><Film /></el-icon>
                <span>电影院购票系统</span>
            </router-link>
            <el-button v-if="showBackButton" text class="back-btn" @click="goBack">
                <el-icon><ArrowLeft /></el-icon>
                <span>返回</span>
            </el-button>
            <el-icon class="collapse-btn" @click="toggleSidebar" v-if="isAdminRoute">
                <Fold />
            </el-icon>
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
import { useRoute, useRouter } from 'vue-router'
import { computed } from 'vue'
import { unlockSeatsApi } from '@/api/seat'

const userStore = useUserStore()
const appStore = useAppStore()
const route = useRoute()
const router = useRouter()

const isAdminRoute = computed(() => route.path.startsWith('/admin'))

const showBackButton = computed(() => {
    return route.name !== 'MovieList'
})

function goBack() {
    const routeName = route.name
    switch (routeName) {
        case 'MovieDetail':
            router.push('/movies')
            break
        case 'Showing': {
            const movieId = route.query.movieId || appStore.currentMovieId
            if (movieId) {
                router.push(`/movies/${movieId}`)
            } else {
                router.push('/movies')
            }
            break
        }
        case 'SeatSelect': {
            // 返回选座之前，释放已锁定的座位
            const showingId = Number(route.query.showingId) || appStore.currentShowingId
            const seatIds = appStore.selectedSeats.map((s: any) => s.id)
            if (showingId && seatIds.length > 0) {
                unlockSeatsApi({ showingId, seatIds }).catch(() => {})
            }
            appStore.resetBooking()
            const movieId = route.query.movieId || appStore.currentMovieId
            router.push(`/showings?movieId=${movieId}`)
            break
        }
        case 'SnackOrder': {
            // 返回选座页面：保留锁定状态，仅恢复选中记录用于回显
            const showingId = Number(route.query.showingId) || appStore.currentShowingId
            const movieId = route.query.movieId || appStore.currentMovieId
            router.push(`/seats?movieId=${movieId}&showingId=${showingId}`)
            break
        }
        case 'OrderConfirm': {
            const movieId = route.query.movieId || appStore.currentMovieId
            const showingId = route.query.showingId || appStore.currentShowingId
            router.push(`/snacks?movieId=${movieId}&showingId=${showingId}`)
            break
        }
        case 'Payment':
        case 'PaymentSuccess':
        case 'OrderList':
        case 'Profile':
        case 'Dashboard':
        case 'MovieManage':
        case 'ShowingManage':
        case 'HallManage':
        case 'OrderManage':
        case 'UserManage':
        case 'PricingRule':
            router.push('/movies')
            break
        case 'OrderDetail':
            if (route.query.from === 'admin' && userStore.isAdmin) {
                router.push('/admin/orders')
            } else {
                router.push('/orders')
            }
            break
        default:
            router.back()
    }
}

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
    gap: 8px;

    .back-btn {
        font-size: 14px;
        color: #606266;
    }

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
