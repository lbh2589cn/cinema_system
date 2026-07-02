<template>
    <div class="page-container">
        <el-card class="page-card">
            <template #header>
                <span class="card-title">个人中心</span>
            </template>
            <el-descriptions :column="1" border>
                <el-descriptions-item label="账号">{{ profile.userId }}</el-descriptions-item>
                <el-descriptions-item label="用户名">{{ profile.username }}</el-descriptions-item>
                <el-descriptions-item label="昵称">{{ profile.nickname || '-' }}</el-descriptions-item>
                <el-descriptions-item label="手机号">{{ profile.phone || '-' }}</el-descriptions-item>
                <el-descriptions-item label="角色">
                    <el-tag :type="profile.role === 'ADMIN' ? 'danger' : 'primary'">
                        {{ profile.role === 'ADMIN' ? '管理员' : '普通用户' }}
                    </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="会员">
                    <el-tag :type="profile.isMember ? 'success' : 'info'">
                        {{ profile.isMember ? '是' : '否' }}
                    </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="状态">
                    <el-tag :type="profile.status === 'ACTIVE' ? 'success' : 'danger'">
                        {{ profile.status === 'ACTIVE' ? '正常' : '禁用' }}
                    </el-tag>
                </el-descriptions-item>
            </el-descriptions>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getProfileApi } from '@/api/auth'
import type { UserProfile } from '@/api/auth'

const profile = ref<UserProfile>({} as UserProfile)

onMounted(async () => {
    profile.value = await getProfileApi()
})
</script>

<style scoped lang="scss">
.card-title {
    font-size: 16px;
    font-weight: 600;
}
</style>
