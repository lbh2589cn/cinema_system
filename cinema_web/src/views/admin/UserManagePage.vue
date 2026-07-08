<template>
    <div>
        <h2 class="page-title">用户管理</h2>
        <el-card class="page-card">
            <el-table :data="users" v-loading="loading" style="width: 100%">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="userId" label="账号" width="120" />
                <el-table-column prop="username" label="用户名" width="150" />
                <el-table-column prop="phone" label="手机号" width="130" />
                <el-table-column prop="role" label="角色" width="100">
                    <template #default="{ row }">
                        <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'">{{ row.role }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="isMember" label="会员" width="80">
                    <template #default="{ row }">
                        <el-tag :type="row.isMember ? 'success' : 'info'" size="small">{{ row.isMember ? '是' : '否' }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">{{ row.status === 'ACTIVE' ? '正常' : '禁用' }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="200">
                    <template #default="{ row }">
                        <el-button text type="primary" @click="toggleMember(row)">
                            {{ row.isMember ? '取消会员' : '设为会员' }}
                        </el-button>
                        <el-button text :type="row.status === 'ACTIVE' ? 'danger' : 'success'" @click="toggleStatus(row)">
                            {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
            <div class="pagination" v-if="total > 0">
                <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="prev, pager, next" @current-change="loadUsers" />
            </div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminUsersApi, updateAdminUserApi } from '@/api/admin'

const users = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

async function loadUsers() {
    loading.value = true
    try {
        const result = await getAdminUsersApi({ page: page.value - 1, size: size.value })
        users.value = result.content
        total.value = result.total
    } finally {
        loading.value = false
    }
}

async function toggleMember(user: any) {
    try {
        await updateAdminUserApi(user.id, { isMember: !user.isMember })
        ElMessage.success('更新成功')
        await loadUsers()
    } catch {
        // error
    }
}

async function toggleStatus(user: any) {
    const action = user.status === 'ACTIVE' ? '禁用' : '启用'
    try {
        await ElMessageBox.confirm(`确定要${action}该用户吗？`, '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
        await updateAdminUserApi(user.id, { status: user.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE' })
        ElMessage.success(`${action}成功`)
        await loadUsers()
    } catch {
        // cancelled
    }
}

onMounted(loadUsers)
</script>

<style scoped lang="scss">
.page-title {
    font-size: 20px;
    margin-bottom: 20px;
}

.pagination {
    display: flex;
    justify-content: center;
    margin-top: 20px;
}
</style>
