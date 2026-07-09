<template>
    <div>
        <div class="page-header">
            <h2 class="page-title">Showing Management</h2>
            <el-button type="primary" @click="handleAdd">Add Showing</el-button>
        </div>

        <el-card class="page-card">
            <div class="filters" style="margin-bottom: 16px">
                <el-date-picker v-model="filterDate" type="date" format="DD/MM/YYYY" value-format="YYYY-MM-DD" placeholder="Select date" @change="loadShowings" />
            </div>
            <el-table :data="showings" v-loading="loading" style="width: 100%">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="movieId" label="Movie ID" width="80" />
                <el-table-column prop="hallId" label="Hall ID" width="80" />
                <el-table-column label="Show Date" width="120">
                    <template #default="{ row }">{{ formatDate(row.showDate) }}</template>
                </el-table-column>
                <el-table-column prop="showTime" label="Show Time" width="100" />
                <el-table-column prop="basePrice" label="Base Price" width="100">
                    <template #default="{ row }">¥{{ row.basePrice }}</template>
                </el-table-column>
                <el-table-column prop="status" label="Status" width="120">
                    <template #default="{ row }">
                        <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="Actions" width="260">
                    <template #default="{ row }">
                        <el-button text type="primary" @click="handleEdit(row)">Edit</el-button>
                        <el-button text type="danger" v-if="canCancelRow(row)" @click="handleCancel(row)">Cancel</el-button>
                        <el-button text type="success" v-if="canRestoreRow(row)" @click="handleRestore(row)">Restore</el-button>
                        <el-button text type="danger" @click="handleDelete(row)">Delete</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>

        <div class="pagination-wrapper" v-if="total > 0">
            <el-pagination
                v-model:current-page="page"
                :page-size="size"
                :total="total"
                layout="prev, pager, next"
                @current-change="loadShowings"
            />
        </div>

        <el-dialog v-model="showDialog" :title="isEdit ? 'Edit Showing' : 'Add Showing'" width="500px">
            <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
                <el-form-item label="Movie" prop="movieId">
                    <el-select v-model="form.movieId" placeholder="Select movie" filterable style="width: 100%">
                        <el-option v-for="m in movies" :key="m.id" :label="m.title" :value="m.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="Hall" prop="hallId">
                    <el-select v-model="form.hallId" placeholder="Select hall" style="width: 100%">
                        <el-option v-for="h in halls" :key="h.id" :label="h.name" :value="h.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="Show Date" prop="showDate">
                    <el-date-picker v-model="form.showDate" type="date" format="DD/MM/YYYY" value-format="YYYY-MM-DD" style="width: 100%" />
                </el-form-item>
                <el-form-item label="Show Time" prop="showTime">
                    <el-time-picker v-model="form.showTime" value-format="HH:mm" format="HH:mm" style="width: 100%" />
                </el-form-item>
                <el-form-item label="Base Price" prop="basePrice">
                    <el-input-number v-model="form.basePrice" :min="0" :step="5" :precision="2" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="showDialog = false">Close</el-button>
                <el-button type="primary" @click="handleSave" :loading="saving">Save</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createShowingApi, updateShowingApi, cancelShowingApi, restoreShowingApi, deleteShowingApi } from '@/api/showing'
import { getAdminShowingsApi } from '@/api/admin'
import { getMoviesApi } from '@/api/movie'
import { getHallsApi } from '@/api/hall'
import type { Showing } from '@/api/showing'
import type { Movie } from '@/api/movie'
import type { Hall } from '@/api/hall'
import { formatDate } from '@/composables/useDateFormatter'

const showings = ref<Showing[]>([])
const movies = ref<Movie[]>([])
const halls = ref<Hall[]>([])
const loading = ref(false)
const saving = ref(false)
const showDialog = ref(false)
const filterDate = ref('')
const formRef = ref()
const page = ref(1)
const size = ref(10)
const total = ref(0)

const isEdit = ref(false)
const editingShowing = ref<Showing | null>(null)

const form = reactive({
    movieId: null as number | null,
    hallId: null as number | null,
    showDate: '',
    showTime: '',
    basePrice: 50,
})

const rules = {
    movieId: [{ required: true, message: 'Please select a movie', trigger: 'change' }],
    hallId: [{ required: true, message: 'Please select a hall', trigger: 'change' }],
    showDate: [{ required: true, message: 'Please select a date', trigger: 'change' }],
    showTime: [{ required: true, message: 'Please select a time', trigger: 'change' }],
}

function statusType(status: string) {
    const map: Record<string, string> = { SCHEDULED: 'success', CANCELLED: 'danger' }
    return map[status] || 'info'
}

function statusLabel(status: string) {
    const map: Record<string, string> = { SCHEDULED: 'Scheduled', CANCELLED: 'Cancelled' }
    return map[status] || status
}

function isShowTimeBeforeNow(showing: Showing): boolean {
    const showDateTime = new Date(`${showing.showDate}T${showing.showTime}`)
    return showDateTime > new Date()
}

function canCancelRow(showing: Showing): boolean {
    return showing.status === 'SCHEDULED' && isShowTimeBeforeNow(showing)
}

function canRestoreRow(showing: Showing): boolean {
    return showing.status === 'CANCELLED' && isShowTimeBeforeNow(showing)
}

async function loadShowings() {
    loading.value = true
    try {
        const result = await getAdminShowingsApi({ date: filterDate.value || undefined, page: page.value - 1, size: size.value })
        showings.value = result.content
        total.value = result.total
    } finally {
        loading.value = false
    }
}

function resetForm() {
    form.movieId = null
    form.hallId = null
    form.showDate = ''
    form.showTime = ''
    form.basePrice = 50
    formRef.value?.resetFields()
}

function handleAdd() {
    isEdit.value = false
    editingShowing.value = null
    resetForm()
    showDialog.value = true
}

function handleEdit(showing: Showing) {
    isEdit.value = true
    editingShowing.value = showing
    form.movieId = showing.movieId
    form.hallId = showing.hallId
    form.showDate = showing.showDate
    form.showTime = showing.showTime
    form.basePrice = showing.basePrice
    showDialog.value = true
}

async function handleSave() {
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) return
    saving.value = true
    try {
        if (isEdit.value && editingShowing.value) {
            await updateShowingApi(editingShowing.value.id, {
                movieId: form.movieId ?? undefined,
                hallId: form.hallId ?? undefined,
                showDate: form.showDate,
                showTime: form.showTime,
                basePrice: form.basePrice,
            })
            ElMessage.success('Update successful')
        } else {
            await createShowingApi({
                movieId: form.movieId!,
                hallId: form.hallId!,
                showDate: form.showDate,
                showTime: form.showTime,
                basePrice: form.basePrice,
            })
            ElMessage.success('Created successfully')
        }
        showDialog.value = false
        await loadShowings()
    } finally {
        saving.value = false
    }
}

async function handleCancel(showing: Showing) {
    try {
        await ElMessageBox.confirm('Are you sure you want to cancel this showing?', 'Notice', { confirmButtonText: 'Confirm', cancelButtonText: 'Cancel', type: 'warning' })
        await cancelShowingApi(showing.id)
        ElMessage.success('Cancelled successfully')
        showDialog.value = false
        await loadShowings()
    } catch {
        // cancelled
    }
}

async function handleRestore(showing: Showing) {
    try {
        await ElMessageBox.confirm('Are you sure you want to restore this showing?', 'Notice', { confirmButtonText: 'Confirm', cancelButtonText: 'Cancel', type: 'warning' })
        await restoreShowingApi(showing.id)
        ElMessage.success('Restored successfully')
        showDialog.value = false
        await loadShowings()
    } catch {
        // cancelled
    }
}

async function handleDelete(showing: Showing) {
    try {
        await ElMessageBox.confirm('Are you sure you want to permanently delete this showing? This action cannot be undone and will also delete associated seat booking data.', 'Notice', { confirmButtonText: 'Confirm', cancelButtonText: 'Cancel', type: 'warning' })
        await deleteShowingApi(showing.id)
        ElMessage.success('Deleted successfully')
        showDialog.value = false
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

.pagination-wrapper {
    display: flex;
    justify-content: center;
    margin-top: 20px;
}
</style>