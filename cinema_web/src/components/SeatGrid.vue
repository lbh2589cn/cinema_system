<template>
    <div class="seat-grid-wrapper">
        <div class="screen-label">SCREEN</div>
        <div class="screen"></div>
        <div class="seat-grid" :style="gridStyle">
            <div
                v-for="seat in seats"
                :key="seat.id"
                class="seat"
                :class="[
                    seat.status === 'AVAILABLE' ? 'available' : seat.status === 'LOCKED' ? 'locked' : 'booked',
                    selectedIds.has(seat.id) ? 'selected' : '',
                    seat.seatType === 'UNAVAILABLE' ? 'unavailable' : '',
                    isVipRestricted(seat) ? 'vip-restricted' : '',
                ]"
                @click="handleSeatClick(seat)"
            >
                <el-icon v-if="seat.status === 'BOOKED'" :size="16"><Lock /></el-icon>
                <span v-else-if="seat.seatType === 'UNAVAILABLE'" style="font-size: 14px; font-weight: bold;">✕</span>
                <span v-else-if="isVipRestricted(seat)">
                    <el-icon :size="14"><WarningFilled /></el-icon>
                </span>
                <span v-else>{{ seat.rowNum }}-{{ seat.colNum }}</span>
            </div>
        </div>
        <div class="seat-legend">
            <div class="legend-item"><span class="legend-dot available"></span>Available</div>
            <div class="legend-item"><span class="legend-dot selected"></span>Selected</div>
            <div class="legend-item"><span class="legend-dot locked"></span>Locked</div>
            <div class="legend-item"><span class="legend-dot booked-dot"></span>Sold</div>
            <div class="legend-item"><span class="legend-dot vip-restricted-dot"></span>VIP Only</div>
            <div class="legend-item"><span class="legend-dot unavailable-dot"></span>Unavailable</div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { WarningFilled } from '@element-plus/icons-vue'
import type { SeatStatus } from '@/api/seat'

const props = defineProps<{
    seats: SeatStatus[]
    selectedIds: Set<number>
    cols: number
    isMember: boolean
}>()

const emit = defineEmits<{
    select: [seat: SeatStatus]
}>()

const gridStyle = computed(() => ({
    gridTemplateColumns: `repeat(${props.cols}, 40px)`,
}))

function isVipRestricted(seat: SeatStatus): boolean {
    return seat.seatType === 'VIP' && !props.isMember
}

function handleSeatClick(seat: SeatStatus) {
    if (seat.seatType === 'UNAVAILABLE') {
        ElMessage.warning('This seat is unavailable')
        return
    }
    if (isVipRestricted(seat)) {
        ElMessage.warning('This seat is for VIP members only. Please upgrade your account.')
        return
    }
    if (props.selectedIds.has(seat.id)) {
        emit('select', seat)
    } else if (seat.status === 'AVAILABLE') {
        emit('select', seat)
    }
}
</script>

<style scoped lang="scss">
.seat-grid-wrapper {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
    padding: 20px 0;
}

.screen-label {
    font-size: 13px;
    color: #909399;
    text-transform: uppercase;
    letter-spacing: 4px;
}

.screen {
    width: 80%;
    height: 8px;
    background: linear-gradient(to right, transparent, #409eff, transparent);
    border-radius: 50%;
    margin-bottom: 20px;
}

.seat-grid {
    display: grid;
    gap: 6px;
    justify-content: center;
}

.seat {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 6px;
    font-size: 11px;
    cursor: pointer;
    transition: all 0.2s;
    user-select: none;

    &.available {
        background: #e8f4e8;
        color: #67c23a;
        border: 1px solid #b3e0b3;

        &:hover {
            background: #67c23a;
            color: #fff;
        }
    }

    &.locked {
        background: #eef1f6;
        color: #909399;
        border: 1px solid #d3d9e4;
        cursor: not-allowed;

        &:hover {
            background: #eef1f6;
            color: #909399;
            border-color: #d3d9e4;
        }
    }

    &.booked {
        background: #f0f0f0;
        color: #c0c4cc;
        border: 1px solid #e4e7ed;
        cursor: not-allowed;

        &:hover {
            background: #f0f0f0;
            color: #c0c4cc;
            border-color: #e4e7ed;
        }
    }

    &.selected {
        background: #409eff;
        color: #fff;
        border-color: #409eff;
        cursor: pointer;

        &:hover {
            background: #409eff;
            color: #fff;
            border-color: #409eff;
        }
    }

    &.unavailable {
        background: #fef0f0;
        color: #f56c6c;
        border-color: #fbc4c4;
        cursor: not-allowed;

        &:hover {
            background: #fef0f0;
            color: #f56c6c;
            border-color: #fbc4c4;
        }
    }

    &.vip-restricted {
        &.available {
            background: #fef6e7;
            color: #e6a23c;
            border-color: #f5dab1;
            cursor: not-allowed;

            &:hover {
                background: #fef0d5;
                color: #d4880f;
            }
        }
    }
}

.seat-legend {
    display: flex;
    gap: 20px;
    margin-top: 10px;

    .legend-item {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 13px;
        color: #606266;
    }

    .legend-dot {
        width: 16px;
        height: 16px;
        border-radius: 4px;
        border: 1px solid;

        &.available {
            background: #e8f4e8;
            border-color: #b3e0b3;
        }

        &.selected {
            background: #409eff;
            border-color: #409eff;
        }

        &.locked {
            background: #eef1f6;
            border-color: #d3d9e4;
        }

        &.booked-dot {
            background: #f0f0f0;
            border-color: #e4e7ed;
        }

        &.vip-restricted-dot {
            background: #fef6e7;
            border-color: #f5dab1;
        }

        &.unavailable-dot {
            background: #fef0f0;
            border-color: #fbc4c4;
        }
    }
}
</style>
