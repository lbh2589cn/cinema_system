<template>
    <div>
        <h2 class="page-title">订单管理</h2>
        <el-card class="page-card">
            <el-table :data="orders" v-loading="loading" style="width: 100%">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="orderNo" label="订单号" width="200" />
                <el-table-column prop="userId" label="用户ID" width="80" />
                <el-table-column prop="seatCount" label="座位数" width="80" />
                <el-table-column prop="totalAmount" label="总价" width="100">
                    <template #default="{ row }">¥{{ row.totalAmount.toFixed(2) }}</template>
                </el-table-column>
                <el-table-column prop="finalAmount" label="实付" width="100">
                    <template #default="{ row }">¥{{ row.finalAmount.toFixed(2) }}</template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="120">
                    <template #default="{ row }">
                        <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createdAt" label="创建时间" width="180" />
                <el-table-column label="操作" width="180">
                    <template #default="{ row }">
                        <el-button text type="primary" @click="viewDetail(row)">详情</el-button>
                        <el-button text type="danger" @click="handleDelete(row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <div class="pagination" v-if="total > 0">
                <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="prev, pager, next" @current-change="loadOrders" />
            </div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminOrdersApi, hardDeleteOrderApi } from '@/api/admin'
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

function viewDetail(order: Order) {
    router.push(`/orders/${order.id}`)
}

async function handleDelete(order: Order) {
    try {
        await ElMessageBox.confirm(`确认永久删除订单 ${order.orderNo}？此操作将同时删除关联的支付记录，且不可恢复。`, '确认删除', {
            confirmButtonText: '确认删除',
            cancelButtonText: '取消',
            type: 'warning',
        })
        await hardDeleteOrderApi(order.id)
        ElMessage.success('订单已永久删除')
        await loadOrders()
    } catch {
        // cancelled or error
    }
}

async function loadOrders() {
    loading.value = true
    try {
        const result = await getAdminOrdersApi({ page: page.value - 1, size: size.value })
        orders.value = result.content
        total.value = result.total
    } finally {
        loading.value = false
    }
}

onMounted(loadOrders)
</script>

<style scoped lang="scss">
.page-title {
    font-size: 20px;
    margin-bottom: 20px;
}

.pagination {
    display: flex;
    justify-content: center;
    margin-top: 20px;
}
</style>
