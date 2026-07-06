<template>
    <div>
        <div class="page-header">
            <h2 class="page-title">影厅管理</h2>
            <el-button type="primary" @click="openCreate">新增影厅</el-button>
        </div>

        <div class="hall-grid" v-loading="loading">
            <el-card v-for="hall in halls" :key="hall.id" class="hall-card" shadow="hover">
                <h3>{{ hall.name }}</h3>
                <div class="hall-info">
                    <p>{{ hall.rows }} 行 × {{ hall.cols }} 列</p>
                    <p>共 {{ hall.seatCount }} 个座位</p>
                    <p v-if="hall.description" class="desc">{{ hall.description }}</p>
                </div>
                <div class="hall-actions">
                    <el-button text type="primary" @click="viewSeats(hall)">查看座位</el-button>
                    <el-button text type="warning" @click="openEdit(hall)">编辑</el-button>
                    <el-button text type="danger" @click="handleDelete(hall)">删除</el-button>
                </div>
            </el-card>
        </div>

        <div class="pagination-wrapper" v-if="total > 0">
            <el-pagination
                v-model:current-page="page"
                :page-size="size"
                :total="total"
                layout="prev, pager, next"
                @current-change="loadHalls"
            />
        </div>

        <el-dialog v-model="showDialog" :title="isEdit ? '编辑影厅' : '新增影厅'" width="560px">
            <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
                <el-form-item label="名称" prop="name">
                    <el-input v-model="form.name" />
                </el-form-item>
                <el-form-item label="行数" prop="rows">
                    <el-input-number v-model="form.rows" :min="1" :max="30" />
                </el-form-item>
                <el-form-item label="列数" prop="cols">
                    <el-input-number v-model="form.cols" :min="1" :max="30" />
                </el-form-item>
                <el-form-item label="座位图" v-if="showDialog">
                    <div class="seat-edit-area">
                        <div class="screen-label">银幕</div>
                        <div class="screen-line"></div>
                        <div class="seat-grid-preview" :style="{ gridTemplateColumns: `repeat(${form.cols}, minmax(36px, 1fr))` }">
                            <div v-for="seat in editSeats" :key="`${seat.rowNum}-${seat.colNum}`"
                                 class="mini-seat" :class="seat.seatType.toLowerCase()"
                                 @click="toggleSeatType(seat.rowNum, seat.colNum)"
                                 :title="'点击切换'">
                                {{ seat.rowNum }}-{{ seat.colNum }}
                             </div>
                         </div>
                         <p class="seat-legend">
                             <span class="mini-seat standard">标准座</span>
                             <span class="mini-seat vip">VIP座</span>
                         </p>
                         <p class="seat-tip">* 点击切换座位类型（标准座 / VIP座）</p>
                     </div>
                 </el-form-item>
                 <el-form-item label="描述" prop="description">
                     <el-input v-model="form.description" type="textarea" />
                 </el-form-item>
             </el-form>
            <template #footer>
                <el-button @click="showDialog = false">取消</el-button>
                <el-button type="primary" @click="handleSave" :loading="saving">{{ isEdit ? '更新' : '保存' }}</el-button>
            </template>
        </el-dialog>

        <el-dialog v-model="showSeatsDialog" title="座位布局" width="600px">
            <div v-if="selectedHall" class="seat-layout">
                <div class="screen-label">银幕</div>
                <div class="screen-line"></div>
                <div class="seat-grid-preview" :style="{ gridTemplateColumns: `repeat(${selectedHall.cols}, minmax(36px, 1fr))` }">
                    <div v-for="seat in hallSeats" :key="seat.id" class="mini-seat" :class="seat.seatType.toLowerCase()">
                        {{ seat.rowNum }}-{{ seat.colNum }}
                    </div>
                </div>
                <p class="seat-legend">
                    <span class="mini-seat standard">标准座</span>
                    <span class="mini-seat vip">VIP座</span>
                </p>
            </div>
            <template #footer>
                <el-button @click="showSeatsDialog = false">关闭</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getHallDetailApi, createHallApi, updateHallApi, deleteHallApi } from '@/api/hall'
import { getAdminHallsApi } from '@/api/admin'
import type { Hall, HallSeat, SeatTypeUpdate } from '@/api/hall'

const halls = ref<Hall[]>([])
const hallSeats = ref<HallSeat[]>([])
const selectedHall = ref<Hall | null>(null)
const loading = ref(false)
const saving = ref(false)
const showDialog = ref(false)
const showSeatsDialog = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const formRef = ref()
const page = ref(1)
const size = ref(10)
const total = ref(0)

const editSeats = ref<HallSeat[]>([])

const form = reactive({
    name: '',
    rows: 5,
    cols: 10,
    description: '',
})

const rules = {
    name: [{ required: true, message: '请输入影厅名称', trigger: 'blur' }],
    rows: [{ required: true, message: '请输入行数', trigger: 'blur' }],
    cols: [{ required: true, message: '请输入列数', trigger: 'blur' }],
}

async function loadHalls() {
    loading.value = true
    try {
        const result = await getAdminHallsApi({ page: page.value - 1, size: size.value })
        halls.value = result.content
        total.value = result.total
    } finally {
        loading.value = false
    }
}

function resetForm() {
    isEdit.value = false
    editId.value = null
    form.name = ''
    form.rows = 5
    form.cols = 10
    form.description = ''
    editSeats.value = []
}

function generateSeats(rows: number, cols: number) {
    const seats: HallSeat[] = []
    for (let r = 1; r <= rows; r++) {
        for (let c = 1; c <= cols; c++) {
            seats.push({ id: 0, hallId: 0, rowNum: r, colNum: c, seatType: 'STANDARD' })
        }
    }
    return seats
}

function openCreate() {
    resetForm()
    editSeats.value = generateSeats(form.rows, form.cols)
    showDialog.value = true
}

async function openEdit(hall: Hall) {
    isEdit.value = true
    editId.value = hall.id
    form.name = hall.name
    form.rows = hall.rows
    form.cols = hall.cols
    form.description = hall.description || ''
    const detail = await getHallDetailApi(hall.id)
    editSeats.value = detail.seats
    showDialog.value = true
}

watch([() => form.rows, () => form.cols], ([rows, cols]) => {
    if (showDialog.value && !isEdit.value) {
        editSeats.value = generateSeats(rows, cols)
    }
})

function toggleSeatType(rowNum: number, colNum: number) {
    const seat = editSeats.value.find(s => s.rowNum === rowNum && s.colNum === colNum)
    if (seat) {
        seat.seatType = seat.seatType === 'STANDARD' ? 'VIP' : 'STANDARD'
    }
}

async function viewSeats(hall: Hall) {
    selectedHall.value = hall
    const detail = await getHallDetailApi(hall.id)
    hallSeats.value = detail.seats
    showSeatsDialog.value = true
}

async function handleSave() {
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) return
    saving.value = true
    try {
        const changes: SeatTypeUpdate[] = editSeats.value
            .filter(s => s.seatType !== 'STANDARD')
            .map(s => ({ rowNum: s.rowNum, colNum: s.colNum, seatType: s.seatType }))
        const payload = { ...form, seats: changes.length > 0 ? changes : undefined }
        if (isEdit.value && editId.value) {
            await updateHallApi(editId.value, payload)
            ElMessage.success('更新成功')
        } else {
            await createHallApi(payload as any)
            ElMessage.success('创建成功')
        }
        showDialog.value = false
        resetForm()
        await loadHalls()
    } finally {
        saving.value = false
    }
}

async function handleDelete(hall: Hall) {
    try {
        await ElMessageBox.confirm(`确定要删除影厅「${hall.name}」吗？此操作不可恢复！`, '警告', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'error' })
        await deleteHallApi(hall.id)
        ElMessage.success('删除成功')
        await loadHalls()
    } catch {
        // cancelled
    }
}

onMounted(loadHalls)
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

.hall-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
}

.hall-card {
    h3 {
        font-size: 18px;
        margin-bottom: 12px;
    }

    .hall-info {
        color: #606266;
        font-size: 14px;
        margin-bottom: 12px;

        p {
            margin-bottom: 4px;
        }

        .desc {
            color: #909399;
            font-size: 13px;
        }
    }

    .hall-actions {
        display: flex;
        gap: 8px;
    }
}

.seat-layout {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
}

.screen-label {
    font-size: 12px;
    color: #909399;
    text-align: center;
    margin-bottom: 2px;
}

.screen-line {
    width: 60%;
    height: 4px;
    background: linear-gradient(to right, transparent, #409eff, transparent);
    border-radius: 50%;
    margin-bottom: 8px;
}

.seat-grid-preview {
    display: grid;
    gap: 3px;
    max-width: 100%;
    overflow-x: auto;
    padding: 4px;
}

.mini-seat {
    height: 24px;
    padding: 0 2px;
    font-size: 10px;
    line-height: 24px;
    border-radius: 4px;
    background: #e8f4e8;
    color: #67c23a;
    border: 1px solid #b3e0b3;
    text-align: center;
    cursor: pointer;
    overflow: hidden;

    &.vip {
        background: #fdf6ec;
        color: #e6a23c;
        border-color: #f5dab1;
        border-style: dashed;
    }
}

.seat-legend {
    display: flex;
    gap: 16px;
    font-size: 13px;
    color: #909399;
}

.seat-edit-area {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    width: 100%;
}

.seat-tip {
    margin: 0;
    font-size: 12px;
    color: #909399;
}

.pagination-wrapper {
    display: flex;
    justify-content: center;
    margin-top: 20px;
}
</style>
