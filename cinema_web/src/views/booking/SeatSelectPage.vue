<template>
    <div class="page-container">
        <el-card class="page-card">
            <template #header>
                <span class="card-title">选择座位</span>
            </template>
            <div class="hall-info" v-if="showing">
                影厅 #{{ showing.hallId }} | {{ showing.showDate }} {{ showing.showTime }}
            </div>
            <SeatGrid
                v-if="seats.length > 0"
                :seats="seats"
                :selected-ids="selectedIds"
                :cols="maxCols"
                @select="toggleSeat"
            />
            <div class="actions" v-if="selectedIds.size > 0">
                <div class="selected-info">已选 {{ selectedIds.size }} 个座位</div>
                <el-button type="primary" size="large" @click="handleNext" :loading="locking">
                    去选购零食
                </el-button>
            </div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSeatsApi, lockSeatsApi, unlockSeatsApi } from '@/api/seat'
import { getShowingApi } from '@/api/showing'
import { useAppStore } from '@/stores/app'
import type { SeatStatus } from '@/api/seat'
import type { Showing } from '@/api/showing'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const seats = ref<SeatStatus[]>([])
const selectedIds = ref(new Set<number>())
const showing = ref<Showing | null>(null)
const loading = ref(false)
const locking = ref(false)

const maxCols = computed(() => {
    if (seats.value.length === 0) return 10
    const cols = new Set(seats.value.map(s => s.colNum))
    return Math.max(...cols)
})

function toggleSeat(seat: SeatStatus) {
    if (selectedIds.value.has(seat.id)) {
        // 取消选中：释放该座位的锁定
        selectedIds.value.delete(seat.id)
        const showingId = Number(route.query.showingId)
        if (showingId) {
            unlockSeatsApi({ showingId, seatIds: [seat.id] }).then(() => {
                seat.status = 'AVAILABLE'
            }).catch(() => {})
        }
    } else if (seat.status === 'AVAILABLE') {
        selectedIds.value.add(seat.id)
    }
}

async function handleNext() {
    locking.value = true
    try {
        const showingId = Number(route.query.showingId)
        const movieId = route.query.movieId

        // 只锁定尚未锁定的座位
        const seatsToLock = seats.value.filter(s => selectedIds.value.has(s.id) && s.status === 'AVAILABLE')
        if (seatsToLock.length > 0) {
            await lockSeatsApi({ showingId, seatIds: seatsToLock.map(s => s.id) })
        }

        appStore.setSelectedSeats(
            seats.value.filter(s => selectedIds.value.has(s.id))
        )
        router.push(`/snacks?movieId=${movieId}&showingId=${showingId}`)
    } catch {
        ElMessage.error('锁定座位失败，请重试')
    } finally {
        locking.value = false
    }
}

onMounted(async () => {
    loading.value = true
    try {
        const showingId = Number(route.query.showingId)
        if (!showingId) {
            ElMessage.error('缺少场次信息')
            router.push('/movies')
            return
        }
        showing.value = await getShowingApi(showingId)

        // 重新获取最新座位数据
        seats.value = await getSeatsApi(showingId)

        // 从零食页返回时，恢复之前选中的座位
        appStore.selectedSeats.forEach((s: any) => {
            selectedIds.value.add(s.id)
        })
    } catch (e: any) {
        ElMessage.error(e?.message || '获取座位信息失败')
    } finally {
        loading.value = false
    }
})
</script>

<style scoped lang="scss">
.card-title {
    font-size: 16px;
    font-weight: 600;
}

.hall-info {
    text-align: center;
    margin-bottom: 16px;
    color: #909399;
    font-size: 14px;
}

.actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 24px;
    padding-top: 16px;
    border-top: 1px solid #ebeef5;

    .selected-info {
        font-size: 14px;
        color: #606266;
    }
}
</style>
