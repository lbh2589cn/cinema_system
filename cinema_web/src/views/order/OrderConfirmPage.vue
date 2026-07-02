<template>
    <div class="page-container">
        <el-card class="page-card">
            <template #header>
                <span class="card-title">确认订单</span>
            </template>
            <el-descriptions :column="1" border>
                <el-descriptions-item label="电影">{{ movie?.title || '待加载' }}</el-descriptions-item>
                <el-descriptions-item label="场次">{{ showing ? `影厅 #${showing.hallId} | ${showing.showDate} ${showing.showTime}` : '待加载' }}</el-descriptions-item>
                <el-descriptions-item label="座位数">{{ appStore.selectedSeats.length }} 个</el-descriptions-item>
                <el-descriptions-item label="零食">
                    <div v-if="appStore.selectedSnacks.length === 0">无</div>
                    <div v-for="s in appStore.selectedSnacks" :key="s.id">
                        {{ s.name }} × {{ s.quantity }}
                    </div>
                </el-descriptions-item>
            </el-descriptions>
            <PriceSummary :total-amount="totalAmount" :discount-amount="0" :final-amount="totalAmount" />
            <div class="actions">
                <el-button @click="goBack">返回修改</el-button>
                <el-button type="primary" size="large" @click="handleSubmit" :loading="submitting">
                    提交订单
                </el-button>
            </div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAppStore } from '@/stores/app'
import { createOrderApi } from '@/api/order'
import { getMovieApi } from '@/api/movie'
import { getShowingApi } from '@/api/showing'
import type { Movie } from '@/api/movie'
import type { Showing } from '@/api/showing'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const submitting = ref(false)
const movie = ref<Movie | null>(null)
const showing = ref<Showing | null>(null)

const totalAmount = computed(() => {
    const seatTotal = (showing.value?.basePrice || 0) * appStore.selectedSeats.length
    const snackTotal = appStore.selectedSnacks.reduce((sum: number, s: any) => sum + s.price * s.quantity, 0)
    return seatTotal + snackTotal
})

function goBack() {
    const movieId = route.query.movieId || appStore.currentMovieId
    const showingId = route.query.showingId || appStore.currentShowingId
    router.push(`/snacks?movieId=${movieId}&showingId=${showingId}`)
}

async function handleSubmit() {
    submitting.value = true
    try {
        const showingId = Number(route.query.showingId) || appStore.currentShowingId
        const order = await createOrderApi({
            showingId: showingId!,
            seatBookingIds: appStore.selectedSeats.map((s: any) => s.id),
            snackItems: appStore.selectedSnacks.map((s: any) => ({
                snackId: s.id,
                quantity: s.quantity,
            })),
        })
        ElMessage.success('订单创建成功')
        appStore.resetBooking()
        router.push(`/order/payment?orderId=${order.id}`)
    } catch {
        // Error handled by interceptor
    } finally {
        submitting.value = false
    }
}

onMounted(async () => {
    const movieId = Number(route.query.movieId) || appStore.currentMovieId
    const showingId = Number(route.query.showingId) || appStore.currentShowingId
    if (movieId) {
        movie.value = await getMovieApi(movieId)
    }
    if (showingId) {
        showing.value = await getShowingApi(showingId)
    }
})
</script>

<style scoped lang="scss">
.card-title {
    font-size: 16px;
    font-weight: 600;
}

.actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 24px;
}
</style>
