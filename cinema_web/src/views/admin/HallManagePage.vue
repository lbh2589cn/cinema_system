<template>
    <div>
        <div class="page-header">
            <h2 class="page-title">影厅管理</h2>
            <el-button type="primary" @click="showDialog = true">新增影厅</el-button>
        </div>

        <div class="hall-grid" v-loading="loading">
            <el-card v-for="hall in halls" :key="hall.id" class="hall-card" shadow="hover">
                <h3>{{ hall.name }}</h3>
                <div class="hall-info">
                    <p>{{ hall.rows }} 行 × {{ hall.cols }} 列</p>
                    <p>共 {{ hall.seatCount }} 个座位</p>
                    <p v-if="hall.description" class="desc">{{ hall.description }}</p>
                </div>
                <el-button text type="primary" @click="viewSeats(hall)">查看座位布局</el-button>
            </el-card>
        </div>

        <el-dialog v-model="showDialog" title="新增影厅" width="500px">
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
                <el-form-item label="描述" prop="description">
                    <el-input v-model="form.description" type="textarea" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="showDialog = false">取消</el-button>
                <el-button type="primary" @click="handleCreate" :loading="saving">保存</el-button>
            </template>
        </el-dialog>

        <el-dialog v-model="showSeatsDialog" title="座位布局" width="600px">
            <div v-if="selectedHall" class="seat-layout">
                <div class="screen">银幕</div>
                <div class="seat-grid-preview">
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getHallsApi, getHallDetailApi, createHallApi } from '@/api/hall'
import type { Hall, HallSeat } from '@/api/hall'

const halls = ref<Hall[]>([])
const hallSeats = ref<HallSeat[]>([])
const selectedHall = ref<Hall | null>(null)
const loading = ref(false)
const saving = ref(false)
const showDialog = ref(false)
const showSeatsDialog = ref(false)
const formRef = ref()

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
        halls.value = await getHallsApi()
    } finally {
        loading.value = false
    }
}

async function viewSeats(hall: Hall) {
    selectedHall.value = hall
    const detail = await getHallDetailApi(hall.id)
    hallSeats.value = detail.seats
    showSeatsDialog.value = true
}

async function handleCreate() {
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) return
    saving.value = true
    try {
        await createHallApi(form)
        ElMessage.success('创建成功')
        showDialog.value = false
        form.name = ''
        form.description = ''
        await loadHalls()
    } finally {
        saving.value = false
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
}

.seat-layout {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
}

.screen {
    width: 60%;
    height: 6px;
    background: linear-gradient(to right, transparent, #409eff, transparent);
    border-radius: 50%;
    text-align: center;
    font-size: 12px;
    color: #909399;
    padding-top: 10px;
}

.seat-grid-preview {
    display: grid;
    grid-template-columns: repeat(10, auto);
    gap: 4px;
}

.mini-seat {
    padding: 2px 6px;
    font-size: 11px;
    border-radius: 3px;
    background: #e8f4e8;
    color: #67c23a;
    border: 1px solid #b3e0b3;

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
</style>
