<template>
    <div>
        <div class="page-header">
            <h2 class="page-title">Snack Management</h2>
            <el-button type="primary" @click="resetForm(); showDialog = true">Add Snack</el-button>
        </div>

        <el-card class="page-card">
            <el-table :data="snacks" v-loading="loading" style="width: 100%">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="name" label="Name" min-width="100" />
                <el-table-column prop="price" label="Unit Price" width="100">
                    <template #default="{ row }">¥{{ row.price }}</template>
                </el-table-column>
                <el-table-column prop="stock" label="Stock" width="80" />
                <el-table-column prop="status" label="Status" width="100">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 'ON' ? 'success' : 'info'">{{ row.status === 'ON' ? 'Active' : 'Offline' }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="Actions" width="400">
                    <template #default="{ row }">
                        <el-button text type="primary" @click="editSnack(row)">Edit</el-button>
                        <el-button text type="danger" @click="toggleStatus(row)">{{ row.status === 'ON' ? 'Deactivate' : 'Activate' }}</el-button>
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
                @current-change="loadSnacks"
            />
        </div>

        <!-- Add/Edit Dialog -->
        <el-dialog v-model="showDialog" :title="isEdit ? 'Edit Snack' : 'Add Snack'" width="500px">
            <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
                <el-form-item label="Name" prop="name">
                    <el-input v-model="form.name" />
                </el-form-item>
                <el-form-item label="Image">
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
                    <div v-if="previewUrl" class="upload-preview">
                        <img :src="previewUrl" alt="Preview" />
                        <el-button text type="danger" size="small" @click="removeImage">Remove</el-button>
                    </div>
                </el-form-item>
                <el-form-item label="Unit Price" prop="price">
                    <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" />
                </el-form-item>
                <el-form-item label="Stock" prop="stock">
                    <el-input-number v-model="form.stock" :min="0" />
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
import { createSnackApi, updateSnackApi, deleteSnackApi } from '@/api/snack'
import { getAdminSnacksApi } from '@/api/admin'
import type { Snack } from '@/api/snack'
import request from '@/api/request'

const snacks = ref<Snack[]>([])
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
    name: '',
    imageUrl: '',
    price: 0,
    stock: 0,
})

const rules = {
    name: [{ required: true, message: 'Please enter snack name', trigger: 'blur' }],
    price: [{ required: true, message: 'Please enter unit price', trigger: 'blur' }],
    stock: [{ required: true, message: 'Please enter stock quantity', trigger: 'blur' }],
}

async function loadSnacks() {
    loading.value = true
    try {
        const result = await getAdminSnacksApi({ page: page.value - 1, size: size.value })
        snacks.value = result.content
        total.value = result.total
    } finally {
        loading.value = false
    }
}

function editSnack(snack: Snack) {
    isEdit.value = true
    editId.value = snack.id
    form.name = snack.name
    form.imageUrl = snack.imageUrl || ''
    form.price = snack.price
    form.stock = snack.stock
    if (snack.imageUrl) {
        previewUrl.value = snack.imageUrl
    }
    showDialog.value = true
}

function resetForm() {
    isEdit.value = false
    editId.value = null
    form.name = ''
    form.imageUrl = ''
    form.price = 0
    form.stock = 0
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
    form.imageUrl = ''
    uploadRef.value?.clearFiles()
}

function beforeUpload(file: File) {
    const isImage = file.type.startsWith('image/')
    if (!isImage) {
        ElMessage.error('Only image files can be uploaded')
        return false
    }
    const isLt5M = file.size / 1024 / 1024 < 5
    if (!isLt5M) {
        ElMessage.error('Image size cannot exceed 5MB')
        return false
    }
    return true
}

async function uploadPendingFile(): Promise<string> {
    if (!pendingFile.value) return form.imageUrl
    const formData = new FormData()
    formData.append('file', pendingFile.value)
    formData.append('directory', 'snacks')
    return request.post('/api/upload', formData)
}

async function handleSave() {
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) return
    saving.value = true
    try {
        if (pendingFile.value) {
            const url = await uploadPendingFile()
            form.imageUrl = url
        }
        if (isEdit.value && editId.value) {
            await updateSnackApi(editId.value, { name: form.name, imageUrl: form.imageUrl, price: form.price, stock: form.stock })
            ElMessage.success('Update successful')
        } else {
            await createSnackApi({ name: form.name, imageUrl: form.imageUrl, price: form.price, stock: form.stock })
            ElMessage.success('Created successfully')
        }
        showDialog.value = false
        resetForm()
        await loadSnacks()
    } finally {
        saving.value = false
    }
}

async function toggleStatus(snack: Snack) {
    const newStatus = snack.status === 'ON' ? 'OFF' : 'ON'
    await updateSnackApi(snack.id, { status: newStatus })
    ElMessage.success(newStatus === 'ON' ? 'Activated successfully' : 'Deactivated successfully')
    await loadSnacks()
}

async function handleHardDelete(snack: Snack) {
    try {
        await ElMessageBox.confirm(`Are you sure you want to permanently delete "${snack.name}"? This action cannot be undone!`, 'Warning', { confirmButtonText: 'Confirm', cancelButtonText: 'Cancel', type: 'error' })
        await deleteSnackApi(snack.id)
        ElMessage.success('Deleted successfully')
        await loadSnacks()
    } catch {
        // cancelled
    }
}

onMounted(loadSnacks)
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

.upload-preview {
    margin-top: 12px;
    display: flex;
    align-items: flex-start;
    gap: 12px;

    img {
        max-width: 100px;
        max-height: 100px;
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
