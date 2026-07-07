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
            <PriceSummary :total-amount="totalAmount" :items="discountItems" :final-amount="finalAmount" />
            <div class="actions">
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
import { calculatePriceApi } from '@/api/pricing'
import type { Movie } from '@/api/movie'
import type { Showing } from '@/api/showing'
import type { DiscountItem } from '@/api/pricing'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const submitting = ref(false)
const movie = ref<Movie | null>(null)
const showing = ref<Showing | null>(null)
const discountResult = ref<{ finalAmount: number; items: DiscountItem[] } | null>(null)

const totalAmount = computed(() => {
    const seatTotal = (showing.value?.basePrice || 0) * appStore.selectedSeats.length
    const snackTotal = appStore.selectedSnacks.reduce((sum: number, s: any) => sum + s.price * s.quantity, 0)
    return seatTotal + snackTotal
})

const discountItems = computed(() => discountResult.value?.items || [])

const finalAmount = computed(() => {
    if (discountResult.value) return discountResult.value.finalAmount
    return totalAmount.value
})

async function fetchPriceBreakdown() {
    const showingId = Number(route.query.showingId) || appStore.currentShowingId
    if (!showingId || totalAmount.value <= 0) return
    try {
        const result = await calculatePriceApi({
            showingId,
            ticketCount: appStore.selectedSeats.length,
            totalAmount: totalAmount.value,
        })
        discountResult.value = { finalAmount: result.finalAmount, items: result.items }
    } catch {
        // Breakdown unavailable, fall back to totalAmount
    }
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
    await fetchPriceBreakdown()
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
