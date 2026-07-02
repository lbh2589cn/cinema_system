<template>
    <div class="page-container">
        <el-card class="page-card">
            <template #header>
                <span class="card-title">我的订单</span>
            </template>
            <el-table :data="orders" v-loading="loading" style="width: 100%" @row-click="goToDetail">
                <el-table-column prop="orderNo" label="订单号" min-width="150" align="center" />
                <el-table-column prop="seatCount" label="座位数" width="80" align="center" />
                <el-table-column prop="totalAmount" label="总价" width="90" align="center">
                    <template #default="{ row }">¥{{ row.totalAmount.toFixed(2) }}</template>
                </el-table-column>
                <el-table-column prop="finalAmount" label="实付" width="90" align="center">
                    <template #default="{ row }">¥{{ row.finalAmount.toFixed(2) }}</template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100" align="center">
                    <template #default="{ row }">
                        <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createdAt" label="创建时间" min-width="150" align="center" />
                <el-table-column label="操作" min-width="160" align="center">
                    <template #default="{ row }">
                        <div class="action-btns">
                            <el-button text type="primary" @click.stop="goToDetail(row)">详情</el-button>
                            <el-button v-if="row.status === 'PAID'" text type="danger" @click.stop="handleRefund(row)">退票</el-button>
                            <el-button text type="danger" @click.stop="handleDelete(row)">删除</el-button>
                        </div>
                    </template>
                </el-table-column>
            </el-table>
            <div class="pagination" v-if="total > 0">
                <el-pagination
                    v-model:current-page="page"
                    :page-size="size"
                    :total="total"
                    layout="prev, pager, next"
                    @current-change="loadOrders"
                />
            </div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { getOrdersApi, refundOrderApi, deleteOrderApi } from '@/api/order'
import type { Order } from '@/api/order'

const router = useRouter()
const orders = ref<Order[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

function statusType(status: string) {
    const map: Record<string, string> = { PENDING: 'warning', PAID: 'success', REFUNDING: 'warning', REFUNDED: 'info', CANCELLED: 'info' }
    return map[status] || 'info'
}

function statusLabel(status: string) {
    const map: Record<string, string> = { PENDING: '待支付', PAID: '已支付', REFUNDING: '退款中', REFUNDED: '已退款', CANCELLED: '已取消' }
    return map[status] || status
}

function goToDetail(order: Order) {
    router.push(`/orders/${order.id}`)
}

async function handleRefund(order: Order) {
    try {
        await ElMessageBox.confirm('确定要退票吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
        await refundOrderApi(order.id)
        ElMessage.success('退票成功')
        await loadOrders()
    } catch {
        // cancelled or error
    }
}

async function handleDelete(order: Order) {
    try {
        await ElMessageBox.confirm('确定要删除此订单吗？删除后不可恢复。', '确定删除', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
        await deleteOrderApi(order.id)
        ElMessage.success('订单已删除')
        await loadOrders()
    } catch {
        // cancelled or error
    }
}

async function loadOrders() {
    loading.value = true
    try {
        const result = await getOrdersApi({ page: page.value - 1, size: size.value })
        orders.value = result.content
        total.value = result.total
    } finally {
        loading.value = false
    }
}

onMounted(loadOrders)
</script>

<style scoped lang="scss">
.card-title {
    font-size: 16px;
    font-weight: 600;
}

.action-btns {
    display: flex;
    justify-content: center;
    gap: 4px;
    white-space: nowrap;
}

.pagination {
    display: flex;
    justify-content: center;
    margin-top: 20px;
}
</style>
