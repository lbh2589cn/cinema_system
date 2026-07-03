<template>
    <div class="dashboard" v-loading="loading">
        <h2 class="page-title">仪表盘</h2>
        <div class="stats-grid">
            <el-card class="stat-card" @click="$router.push('/admin/users')">
                <div class="stat-value">{{ stats.totalUsers || 0 }}</div>
                <div class="stat-label">用户总数</div>
            </el-card>
            <el-card class="stat-card" @click="$router.push('/admin/movies')">
                <div class="stat-value">{{ stats.totalMovies || 0 }}</div>
                <div class="stat-label">电影总数</div>
            </el-card>
            <el-card class="stat-card" @click="$router.push('/admin/orders')">
                <div class="stat-value">{{ stats.totalOrders || 0 }}</div>
                <div class="stat-label">订单总数</div>
            </el-card>
            <el-card class="stat-card" @click="$router.push('/admin/orders')">
                <div class="stat-value">¥{{ (stats.totalRevenue || 0).toFixed(2) }}</div>
                <div class="stat-label">总营收</div>
            </el-card>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getDashboardApi, type DashboardStats } from '@/api/admin'

const router = useRouter()
const loading = ref(false)
const stats = ref<DashboardStats>({ totalUsers: 0 })

onMounted(async () => {
    loading.value = true
    try {
        stats.value = await getDashboardApi()
    } finally {
        loading.value = false
    }
})
</script>

<style scoped lang="scss">
.page-title {
    font-size: 20px;
    margin-bottom: 24px;
}

.stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 20px;
}

.stat-card {
    text-align: center;
    padding: 20px;
    cursor: pointer;
    transition: transform 0.2s;

    &:hover {
        transform: translateY(-2px);
    }

    .stat-value {
        font-size: 36px;
        font-weight: 700;
        color: #409eff;
        margin-bottom: 8px;
    }

    .stat-label {
        font-size: 14px;
        color: #909399;
    }
}
</style>
