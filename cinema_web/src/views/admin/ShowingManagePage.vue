<template>
    <div>
        <div class="page-header">
            <h2 class="page-title">排片管理</h2>
            <el-button type="primary" @click="showDialog = true">新增排片</el-button>
        </div>

        <el-card class="page-card">
            <div class="filters" style="margin-bottom: 16px">
                <el-date-picker v-model="filterDate" type="date" placeholder="选择日期" @change="loadShowings" />
            </div>
            <el-table :data="showings" v-loading="loading" style="width: 100%">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="movieId" label="电影ID" width="80" />
                <el-table-column prop="hallId" label="影厅ID" width="80" />
                <el-table-column prop="showDate" label="放映日期" width="120" />
                <el-table-column prop="showTime" label="放映时间" width="100" />
                <el-table-column prop="basePrice" label="票价" width="100">
                    <template #default="{ row }">¥{{ row.basePrice }}</template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="120">
                    <template #default="{ row }">
                        <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="120">
                    <template #default="{ row }">
                        <el-button v-if="row.status === 'SCHEDULED'" text type="danger" @click="handleCancel(row)">取消</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>

        <el-dialog v-model="showDialog" title="新增排片" width="500px">
            <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
                <el-form-item label="电影" prop="movieId">
                    <el-select v-model="form.movieId" placeholder="选择电影" filterable style="width: 100%">
                        <el-option v-for="m in movies" :key="m.id" :label="m.title" :value="m.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="影厅" prop="hallId">
                    <el-select v-model="form.hallId" placeholder="选择影厅" style="width: 100%">
                        <el-option v-for="h in halls" :key="h.id" :label="h.name" :value="h.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="放映日期" prop="showDate">
                    <el-date-picker v-model="form.showDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
                </el-form-item>
                <el-form-item label="放映时间" prop="showTime">
                    <el-time-picker v-model="form.showTime" value-format="HH:mm" format="HH:mm" style="width: 100%" />
                </el-form-item>
                <el-form-item label="票价" prop="basePrice">
                    <el-input-number v-model="form.basePrice" :min="0" :step="5" :precision="2" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="showDialog = false">取消</el-button>
                <el-button type="primary" @click="handleCreate" :loading="saving">保存</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getShowingsApi, createShowingApi, cancelShowingApi } from '@/api/showing'
import { getMoviesApi } from '@/api/movie'
import { getHallsApi } from '@/api/hall'
import type { Showing } from '@/api/showing'
import type { Movie } from '@/api/movie'
import type { Hall } from '@/api/hall'

const showings = ref<Showing[]>([])
const movies = ref<Movie[]>([])
const halls = ref<Hall[]>([])
const loading = ref(false)
const saving = ref(false)
const showDialog = ref(false)
const filterDate = ref('')
const formRef = ref()

const form = reactive({
    movieId: null as number | null,
    hallId: null as number | null,
    showDate: '',
    showTime: '',
    basePrice: 50,
})

const rules = {
    movieId: [{ required: true, message: '请选择电影', trigger: 'change' }],
    hallId: [{ required: true, message: '请选择影厅', trigger: 'change' }],
    showDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
    showTime: [{ required: true, message: '请选择时间', trigger: 'change' }],
}

function statusType(status: string) {
    const map: Record<string, string> = { SCHEDULED: 'success', ONGOING: 'warning', FINISHED: 'info', CANCELLED: 'danger' }
    return map[status] || 'info'
}

function statusLabel(status: string) {
    const map: Record<string, string> = { SCHEDULED: '待放映', ONGOING: '放映中', FINISHED: '已结束', CANCELLED: '已取消' }
    return map[status] || status
}

async function loadShowings() {
    loading.value = true
    try {
        showings.value = await getShowingsApi({ date: filterDate.value || undefined })
    } finally {
        loading.value = false
    }
}

async function handleCreate() {
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) return
    saving.value = true
    try {
        await createShowingApi(form as any)
        ElMessage.success('创建成功')
        showDialog.value = false
        await loadShowings()
    } finally {
        saving.value = false
    }
}

async function handleCancel(showing: Showing) {
    try {
        await ElMessageBox.confirm('确定要取消该场次吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
        await cancelShowingApi(showing.id)
        ElMessage.success('已取消')
        await loadShowings()
    } catch {
        // cancelled
    }
}

onMounted(async () => {
    movies.value = (await getMoviesApi({ page: 0, size: 100 })).content
    halls.value = await getHallsApi()
    await loadShowings()
})
</script>

<style scoped lang="scss">
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.page-title {
    font-size: 20px;
}
</style>
