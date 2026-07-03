<template>
    <div class="admin-layout">
        <AppHeader />
        <div class="admin-body">
            <el-menu
                :default-active="route.path"
                class="admin-sidebar"
                :collapse="appStore.sidebarCollapsed"
                router
            >
                <el-menu-item index="/admin/dashboard">
                    <el-icon><DataAnalysis /></el-icon>
                    <span>仪表盘</span>
                </el-menu-item>
                <el-menu-item index="/admin/movies">
                    <el-icon><Film /></el-icon>
                    <span>电影管理</span>
                </el-menu-item>
                <el-menu-item index="/admin/showings">
                    <el-icon><Timer /></el-icon>
                    <span>排片管理</span>
                </el-menu-item>
                <el-menu-item index="/admin/halls">
                    <el-icon><OfficeBuilding /></el-icon>
                    <span>影厅管理</span>
                </el-menu-item>
                <el-menu-item index="/admin/orders">
                    <el-icon><List /></el-icon>
                    <span>订单管理</span>
                </el-menu-item>
                <el-menu-item index="/admin/users">
                    <el-icon><User /></el-icon>
                    <span>用户管理</span>
                </el-menu-item>
                <el-menu-item index="/admin/pricing">
                    <el-icon><Coin /></el-icon>
                    <span>定价规则</span>
                </el-menu-item>
            </el-menu>
            <div class="admin-content">
                <router-view />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const appStore = useAppStore()
</script>

<style scoped lang="scss">
.admin-layout {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
}

.admin-body {
    display: flex;
    flex: 1;
}

.admin-sidebar {
    width: 220px;
    min-height: calc(100vh - 60px);
    border-right: 1px solid #e4e7ed;
    overflow-y: auto;

    &.el-menu--collapse {
        width: 64px;
    }
}

.admin-content {
    flex: 1;
    padding: 20px;
    background: #f5f7fa;
    overflow-y: auto;
}
</style>
