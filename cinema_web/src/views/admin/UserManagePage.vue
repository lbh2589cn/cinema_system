<template>
    <div>
        <h2 class="page-title">User Management</h2>
        <el-card class="page-card">
            <el-table :data="users" v-loading="loading" style="width: 100%">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="userId" label="Account" width="120" />
                <el-table-column prop="username" label="Username" width="150" />
                <el-table-column prop="phone" label="Phone" width="130" />
                <el-table-column prop="role" label="Role" width="100">
                    <template #default="{ row }">
                        <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'">{{ row.role }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="isMember" label="VIP" width="80">
                    <template #default="{ row }">
                        <el-tag :type="row.isMember ? 'success' : 'info'" size="small">{{ row.isMember ? 'Yes' : 'No' }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="status" label="Status" width="100">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">{{ row.status === 'ACTIVE' ? 'Active' : 'Disabled' }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="Actions" width="250">
                    <template #default="{ row }">
                        <el-button text type="primary" @click="toggleMember(row)">
                            {{ row.isMember ? 'Revoke VIP' : 'Set as VIP' }}
                        </el-button>
                        <el-button text :type="row.status === 'ACTIVE' ? 'danger' : 'success'" @click="toggleStatus(row)">
                            {{ row.status === 'ACTIVE' ? 'Disable' : 'Enable' }}
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
        ElMessage.success('Update successful')
        await loadUsers()
    } catch {
        // error
    }
}

async function toggleStatus(user: any) {
    const action = user.status === 'ACTIVE' ? 'disable' : 'enable'
    try {
        await ElMessageBox.confirm(`Are you sure you want to ${action} this user?`, 'Notice', { confirmButtonText: 'Confirm', cancelButtonText: 'Cancel', type: 'warning' })
        await updateAdminUserApi(user.id, { status: user.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE' })
        ElMessage.success(`${action} successful`)
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
