<template>
    <div>
        <div class="page-header">
            <h2 class="page-title">Movie Management</h2>
            <el-button type="primary" @click="resetForm(); showDialog = true">Add Movie</el-button>
        </div>

        <el-card class="page-card">
            <el-table :data="movies" v-loading="loading" style="width: 100%">
                <el-table-column prop="id" label="ID" width="50" />
                <el-table-column prop="title" label="Title" min-width="100" />
                <el-table-column prop="duration" label="Duration (mins)" width="130" />
                <el-table-column prop="rating" label="Rating" width="80" />
                <el-table-column prop="genre" label="Genre" min-width="200" />
                <el-table-column prop="status" label="Status" width="100">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 'ON' ? 'success' : 'info'">{{ row.status === 'ON' ? 'Showing' : 'Removed' }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="Actions" width="400">
                    <template #default="{ row }">
                        <el-button text type="primary" @click="editMovie(row)">Edit</el-button>
                        <el-button text type="danger" @click="handleDelete(row)">{{ row.status === 'ON' ? 'Pull' : 'Show' }}</el-button>
                        <el-button text type="danger" @click="handleHardDelete(row)">Delete</el-button>
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
                @current-change="loadMovies"
            />
        </div>

        <!-- Add/Edit Dialog -->
        <el-dialog v-model="showDialog" :title="isEdit ? 'Edit Movie' : 'Add Movie'" width="600px">
            <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
                <el-form-item label="Title" prop="title">
                    <el-input v-model="form.title" />
                </el-form-item>
                <el-form-item label="Poster">
                    <div class="upload-wrapper">
                        <div class="upload-actions">
                            <el-upload
                                :auto-upload="false"
                                :on-change="handleFileChange"
                                :on-exceed="handleExceed"
                                :show-file-list="false"
                                :before-upload="beforeUpload"
                                accept="image/*"
                                ref="uploadRef"
                            >
                                <el-button type="primary">Select Image</el-button>
                            </el-upload>
                            <el-button v-if="previewUrl" text type="danger" size="small" @click="removeImage">Remove</el-button>
                        </div>
                        <div v-if="previewUrl" class="upload-preview">
                            <img :src="previewUrl" alt="Poster Preview" />
                        </div>
                    </div>
                </el-form-item>
                <el-form-item label="Synopsis" prop="description">
                    <el-input v-model="form.description" type="textarea" :rows="3" />
                </el-form-item>
                <el-form-item label="Duration" prop="duration">
                    <el-input-number v-model="form.duration" :min="1" />
                </el-form-item>
                <el-form-item label="Rating" prop="rating">
                    <el-input-number v-model="form.rating" :min="0" :max="10" :step="0.1" />
                </el-form-item>
                <el-form-item label="Genre" prop="genre">
                    <el-input v-model="form.genre" placeholder="Separated by commas, e.g. Sci-Fi,Action" />
                </el-form-item>
                <el-form-item label="Release Date" prop="releaseDate">
                    <el-date-picker v-model="form.releaseDate" type="date" format="DD/MM/YYYY" value-format="YYYY-MM-DD" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="showDialog = false">Cancel</el-button>
                <el-button type="primary" @click="handleSave" :loading="saving">Save</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMoviesApi, createMovieApi, updateMovieApi, hideMovieApi, deleteMovieApi } from '@/api/movie'
import type { Movie } from '@/api/movie'
import request from '@/api/request'

const movies = ref<Movie[]>([])
const loading = ref(false)
const saving = ref(false)
const showDialog = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const formRef = ref()
const uploadRef = ref()
const page = ref(1)
const size = ref(10)
const total = ref(0)

const pendingFile = ref<File | null>(null)
const previewUrl = ref<string>('')

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
    title: [{ required: true, message: 'Please enter movie title', trigger: 'blur' }],
    duration: [{ required: true, message: 'Please enter duration', trigger: 'blur' }],
}

async function loadMovies() {
    loading.value = true
    try {
        const result = await getMoviesApi({ page: page.value - 1, size: size.value })
        movies.value = result.content
        total.value = result.total
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
    if (movie.posterUrl) {
        previewUrl.value = movie.posterUrl
    }
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
    pendingFile.value = null
    if (previewUrl.value) {
        URL.revokeObjectURL(previewUrl.value)
    }
    previewUrl.value = ''
    uploadRef.value?.clearFiles()
}

function handleFileChange(uploadFile: any) {
    const file = uploadFile.raw
    if (!file) return
    // Release previous preview URL
    if (previewUrl.value && !previewUrl.value.startsWith('http')) {
        URL.revokeObjectURL(previewUrl.value)
    }
    pendingFile.value = file
    previewUrl.value = URL.createObjectURL(file)
}

function handleExceed() {
    ElMessage.warning('Only one file can be selected')
}

function removeImage() {
    pendingFile.value = null
    if (previewUrl.value && !previewUrl.value.startsWith('http')) {
        URL.revokeObjectURL(previewUrl.value)
    }
    previewUrl.value = ''
    form.posterUrl = ''
    uploadRef.value?.clearFiles()
}

function beforeUpload(file: File) {
    const isImage = file.type.startsWith('image/')
    // Some browsers may not detect MIME types for certain images; check extension as fallback
    const imageExts = ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.bmp', '.svg', '.ico', '.tiff', '.tif']
    const ext = file.name.substring(file.name.lastIndexOf('.')).toLowerCase()
    if (!isImage && !imageExts.includes(ext)) {
        ElMessage.error('Only image files are supported (JPG/PNG/GIF/WebP/BMP/SVG etc.)')
        return false
    }
    const isLt10M = file.size / 1024 / 1024 < 10
    if (!isLt10M) {
        ElMessage.error('Image size cannot exceed 10MB')
        return false
    }
    return true
}

async function uploadPendingFile(): Promise<string> {
    if (!pendingFile.value) return form.posterUrl
    const formData = new FormData()
    formData.append('file', pendingFile.value)
    formData.append('directory', 'movies')
    return request.post('/api/upload', formData)
}

async function handleSave() {
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) return
    saving.value = true
    try {
        // Upload image first if there is a pending file
        if (pendingFile.value) {
            const url = await uploadPendingFile()
            form.posterUrl = url
        }
        if (isEdit.value && editId.value) {
            await updateMovieApi(editId.value, form)
            ElMessage.success('Updated successfully')
        } else {
            await createMovieApi(form as any)
            ElMessage.success('Created successfully')
        }
        showDialog.value = false
        resetForm()
        await loadMovies()
    } finally {
        saving.value = false
    }
}

async function handleDelete(movie: Movie) {
    const action = movie.status === 'ON' ? 'pull' : 'show'
    try {
        await ElMessageBox.confirm(`Are you sure you want to ${action} this movie?`, 'Confirm', { confirmButtonText: 'Confirm', cancelButtonText: 'Cancel', type: 'warning' })
        if (movie.status === 'ON') {
            await hideMovieApi(movie.id)
        } else {
            await updateMovieApi(movie.id, { status: 'ON' })
        }
        ElMessage.success(action === 'pull' ? 'Movie pulled' : 'Movie is now showing')
        await loadMovies()
    } catch {
        // cancelled
    }
}

async function handleHardDelete(movie: Movie) {
    try {
        await ElMessageBox.confirm(`Are you sure you want to delete "${movie.title}"? This action cannot be undone!`, 'Warning', { confirmButtonText: 'Confirm', cancelButtonText: 'Cancel', type: 'error' })
        await deleteMovieApi(movie.id)
        ElMessage.success('Deleted successfully')
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

.upload-tip {
    font-size: 12px;
    color: #909399;
    margin-left: 8px;
}

.upload-wrapper {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
}

.upload-actions {
    display: flex;
    align-items: center;
    gap: 8px;
}

.upload-preview {
    img {
        max-width: 120px;
        max-height: 160px;
        border-radius: 4px;
        border: 1px solid #e4e7ed;
    }
}

.pagination-wrapper {
    display: flex;
    justify-content: center;
    margin-top: 20px;
}
</style>
