<template>
    <div>
        <div class="page-header">
            <h2 class="page-title">电影管理</h2>
            <el-button type="primary" @click="showDialog = true">新增电影</el-button>
        </div>

        <el-card class="page-card">
            <el-table :data="movies" v-loading="loading" style="width: 100%">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="title" label="电影名称" min-width="180" />
                <el-table-column prop="duration" label="时长(分钟)" width="100" />
                <el-table-column prop="rating" label="评分" width="80" />
                <el-table-column prop="genre" label="类型" width="150" />
                <el-table-column prop="status" label="状态" width="100">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 'ON' ? 'success' : 'info'">{{ row.status === 'ON' ? '上映中' : '已下架' }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="200">
                    <template #default="{ row }">
                        <el-button text type="primary" @click="editMovie(row)">编辑</el-button>
                        <el-button text type="danger" @click="handleDelete(row)">{{ row.status === 'ON' ? '下架' : '上架' }}</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>

        <!-- Add/Edit Dialog -->
        <el-dialog v-model="showDialog" :title="isEdit ? '编辑电影' : '新增电影'" width="600px">
            <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
                <el-form-item label="电影名称" prop="title">
                    <el-input v-model="form.title" />
                </el-form-item>
                <el-form-item label="海报URL" prop="posterUrl">
                    <el-input v-model="form.posterUrl" />
                </el-form-item>
                <el-form-item label="简介" prop="description">
                    <el-input v-model="form.description" type="textarea" :rows="3" />
                </el-form-item>
                <el-form-item label="时长(分钟)" prop="duration">
                    <el-input-number v-model="form.duration" :min="1" />
                </el-form-item>
                <el-form-item label="评分" prop="rating">
                    <el-input-number v-model="form.rating" :min="0" :max="10" :step="0.1" />
                </el-form-item>
                <el-form-item label="类型" prop="genre">
                    <el-input v-model="form.genre" placeholder="用逗号分隔，如 Sci-Fi,Action" />
                </el-form-item>
                <el-form-item label="上映日期" prop="releaseDate">
                    <el-date-picker v-model="form.releaseDate" type="date" value-format="YYYY-MM-DD" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="showDialog = false">取消</el-button>
                <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMoviesApi, createMovieApi, updateMovieApi, deleteMovieApi } from '@/api/movie'
import type { Movie } from '@/api/movie'

const movies = ref<Movie[]>([])
const loading = ref(false)
const saving = ref(false)
const showDialog = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const formRef = ref()

const form = reactive({
    title: '',
    posterUrl: '',
    description: '',
    duration: 120,
    rating: 0,
    genre: '',
    releaseDate: '',
})

const rules = {
    title: [{ required: true, message: '请输入电影名称', trigger: 'blur' }],
    duration: [{ required: true, message: '请输入时长', trigger: 'blur' }],
}

async function loadMovies() {
    loading.value = true
    try {
        const result = await getMoviesApi({ page: 0, size: 100 })
        movies.value = result.content
    } finally {
        loading.value = false
    }
}

function editMovie(movie: Movie) {
    isEdit.value = true
    editId.value = movie.id
    Object.assign(form, {
        title: movie.title,
        posterUrl: movie.posterUrl || '',
        description: movie.description || '',
        duration: movie.duration,
        rating: movie.rating || 0,
        genre: movie.genre || '',
        releaseDate: movie.releaseDate || '',
    })
    showDialog.value = true
}

function resetForm() {
    isEdit.value = false
    editId.value = null
    form.title = ''
    form.posterUrl = ''
    form.description = ''
    form.duration = 120
    form.rating = 0
    form.genre = ''
    form.releaseDate = ''
}

async function handleSave() {
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) return
    saving.value = true
    try {
        if (isEdit.value && editId.value) {
            await updateMovieApi(editId.value, form)
            ElMessage.success('更新成功')
        } else {
            await createMovieApi(form as any)
            ElMessage.success('创建成功')
        }
        showDialog.value = false
        resetForm()
        await loadMovies()
    } finally {
        saving.value = false
    }
}

async function handleDelete(movie: Movie) {
    const action = movie.status === 'ON' ? '下架' : '上架'
    try {
        await ElMessageBox.confirm(`确定要${action}该电影吗？`, '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
        if (movie.status === 'ON') {
            await deleteMovieApi(movie.id)
        } else {
            await updateMovieApi(movie.id, { status: 'ON' })
        }
        ElMessage.success(`${action}成功`)
        await loadMovies()
    } catch {
        // cancelled
    }
}

onMounted(loadMovies)
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
