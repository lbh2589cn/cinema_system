<template>
    <div>
        <div class="page-header">
            <h2 class="page-title">Hall Management</h2>
            <el-button type="primary" @click="openCreate">Add Hall</el-button>
        </div>

        <el-card class="page-card">
            <el-table :data="halls" v-loading="loading" style="width: 100%">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="name" label="Hall Name" min-width="150" />
                <el-table-column prop="rows" label="Rows" width="80" />
                <el-table-column prop="cols" label="Columns" width="80" />
                <el-table-column prop="seatCount" label="Total Seats" width="80" />
                <el-table-column prop="description" label="Description" min-width="200">
                    <template #default="{ row }">{{ row.description || '-' }}</template>
                </el-table-column>
                <el-table-column label="Actions" width="280">
                    <template #default="{ row }">
                        <el-button text type="primary" @click="viewSeats(row)">View Seats</el-button>
                        <el-button text type="warning" @click="openEdit(row)">Edit</el-button>
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
                @current-change="loadHalls"
            />
        </div>

        <el-dialog v-model="showDialog" :title="isEdit ? 'Edit Hall' : 'Add Hall'" width="560px">
            <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
                <el-form-item label="Hall Name" prop="name">
                    <el-input v-model="form.name" />
                </el-form-item>
                <el-form-item label="Rows" prop="rows">
                    <el-input-number v-model="form.rows" :min="1" :max="30" />
                </el-form-item>
                <el-form-item label="Columns" prop="cols">
                    <el-input-number v-model="form.cols" :min="1" :max="30" />
                </el-form-item>
                <el-form-item label="Seat Map" v-if="showDialog">
                    <div class="seat-edit-area">
                        <div class="screen-label">Screen</div>
                        <div class="screen-line"></div>
                        <div class="seat-grid-preview" :style="{ gridTemplateColumns: `repeat(${form.cols}, minmax(36px, 1fr))` }">
                            <el-dropdown v-for="seat in editSeats" :key="`${seat.rowNum}-${seat.colNum}`"
                                         trigger="click" @command="(cmd: string) => seat.status = cmd">
                                <div class="mini-seat" :class="seat.status.toLowerCase()">
                                    {{ seat.rowNum }}-{{ seat.colNum }}
                                </div>
                                <template #dropdown>
                                    <el-dropdown-menu>
                                        <el-dropdown-item command="STANDARD">Standard</el-dropdown-item>
                                        <el-dropdown-item command="VIP">VIP</el-dropdown-item>
                                        <el-dropdown-item command="UNAVAILABLE">Unavailable</el-dropdown-item>
                                    </el-dropdown-menu>
                                </template>
                            </el-dropdown>
                        </div>
                        <div class="seat-legend">
                            <div class="legend-item"><span class="legend-dot standard"></span>Standard</div>
                            <div class="legend-item"><span class="legend-dot vip"></span>VIP</div>
                            <div class="legend-item"><span class="legend-dot unavailable"></span>Unavailable</div>
                        </div>
                         <p class="seat-tip">* Click a seat to change its status</p>
                     </div>
                 </el-form-item>
                 <el-form-item label="Description" prop="description">
                     <el-input v-model="form.description" type="textarea" />
                 </el-form-item>
             </el-form>
            <template #footer>
                <el-button @click="showDialog = false">Cancel</el-button>
                <el-button type="primary" @click="handleSave" :loading="saving">{{ isEdit ? 'Update' : 'Save' }}</el-button>
            </template>
        </el-dialog>

        <el-dialog v-model="showSeatsDialog" title="Seat Layout" width="600px">
            <div v-if="selectedHall" class="seat-layout">
                <div class="screen-label">Screen</div>
                <div class="screen-line"></div>
                <div class="seat-grid-preview" :style="{ gridTemplateColumns: `repeat(${selectedHall.cols}, minmax(36px, 1fr))` }">
                    <div v-for="seat in hallSeats" :key="seat.id" class="mini-seat" :class="seat.status.toLowerCase()">
                        {{ seat.rowNum }}-{{ seat.colNum }}
                    </div>
                </div>
                <div class="seat-legend">
                    <div class="legend-item"><span class="legend-dot standard"></span>Standard</div>
                    <div class="legend-item"><span class="legend-dot vip"></span>VIP</div>
                    <div class="legend-item"><span class="legend-dot unavailable"></span>Unavailable</div>
                </div>
            </div>
            <template #footer>
                <el-button @click="showSeatsDialog = false">Close</el-button>
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
    name: [{ required: true, message: 'Please enter hall name', trigger: 'blur' }],
    rows: [{ required: true, message: 'Please enter row count', trigger: 'blur' }],
    cols: [{ required: true, message: 'Please enter column count', trigger: 'blur' }],
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
            seats.push({ id: 0, hallId: 0, rowNum: r, colNum: c, status: 'STANDARD' })
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
            .map(s => ({ rowNum: s.rowNum, colNum: s.colNum, status: s.status }))
        const payload = { ...form, seats: changes.length > 0 ? changes : undefined }
        if (isEdit.value && editId.value) {
            await updateHallApi(editId.value, payload)
            ElMessage.success('Update successful')
        } else {
            await createHallApi(payload as any)
            ElMessage.success('Created successfully')
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
        await ElMessageBox.confirm(`Are you sure you want to delete hall "${hall.name}"? This action cannot be undone!`, 'Warning', { confirmButtonText: 'Confirm', cancelButtonText: 'Cancel', type: 'error' })
        await deleteHallApi(hall.id)
        ElMessage.success('Deleted successfully')
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

    .el-dropdown {
        width: 100%;
    }
}

.mini-seat {
    height: 24px;
    width: 100%;
    padding: 0 2px;
    font-size: 10px;
    line-height: 24px;
    border-radius: 4px;
    background: #e8f4e8;
    color: #67c23a;
    border: 1px solid #b3e0b3;
    text-align: center;
    overflow: hidden;

    &.vip {
        background: #fdf6ec;
        color: #e6a23c;
        border-color: #f5dab1;
        border-style: dashed;
    }

    &.unavailable {
        background: #fef0f0;
        color: #f56c6c;
        border-color: #fbc4c4;
    }
}

.seat-legend {
    display: flex;
    gap: 20px;
    font-size: 13px;
    color: #606266;
}

.legend-item {
    display: flex;
    align-items: center;
    gap: 6px;
}

.legend-dot {
    display: inline-block;
    width: 16px;
    height: 16px;
    border-radius: 4px;
    border: 1px solid;

    &.standard {
        background: #e8f4e8;
        border-color: #b3e0b3;
    }

    &.vip {
        background: #fdf6ec;
        border-color: #f5dab1;
        border-style: dashed;
    }

    &.unavailable {
        background: #fef0f0;
        border-color: #fbc4c4;
    }
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
