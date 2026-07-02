<template>
    <div>
        <h2 class="page-title">定价规则管理</h2>
        <el-card class="page-card">
            <el-table :data="rules" v-loading="loading" style="width: 100%">
                <el-table-column prop="ruleName" label="规则名称" min-width="150" />
                <el-table-column prop="ruleType" label="规则类型" width="160" />
                <el-table-column prop="ruleValue" label="规则值" width="100">
                    <template #default="{ row }">{{ row.ruleValue }}</template>
                </el-table-column>
                <el-table-column prop="priority" label="优先级" width="80" />
                <el-table-column prop="enabled" label="启用" width="80">
                    <template #default="{ row }">
                        <el-switch v-model="row.enabled" @change="handleToggle(row)" />
                    </template>
                </el-table-column>
                <el-table-column prop="description" label="说明" min-width="200" />
                <el-table-column label="操作" width="120">
                    <template #default="{ row }">
                        <el-button text type="primary" @click="editRule(row)">编辑</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>

        <el-dialog v-model="showDialog" title="编辑定价规则" width="500px">
            <el-form :model="editForm" label-width="100px">
                <el-form-item label="规则名称">
                    <el-input v-model="editForm.ruleName" />
                </el-form-item>
                <el-form-item label="规则值">
                    <el-input-number v-model="editForm.ruleValue" :step="0.01" :precision="4" />
                </el-form-item>
                <el-form-item label="优先级">
                    <el-input-number v-model="editForm.priority" :min="0" />
                </el-form-item>
                <el-form-item label="说明">
                    <el-input v-model="editForm.description" type="textarea" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="showDialog = false">取消</el-button>
                <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPricingRulesApi, updatePricingRuleApi } from '@/api/pricing'
import type { PricingRule } from '@/api/pricing'

const rules = ref<PricingRule[]>([])
const loading = ref(false)
const saving = ref(false)
const showDialog = ref(false)
const editId = ref<number | null>(null)

const editForm = reactive({
    ruleName: '',
    ruleValue: 1,
    priority: 0,
    description: '',
})

async function loadRules() {
    loading.value = true
    try {
        rules.value = await getPricingRulesApi()
    } finally {
        loading.value = false
    }
}

function editRule(rule: PricingRule) {
    editId.value = rule.id
    editForm.ruleName = rule.ruleName
    editForm.ruleValue = rule.ruleValue
    editForm.priority = rule.priority
    editForm.description = rule.description || ''
    showDialog.value = true
}

async function handleSave() {
    if (!editId.value) return
    saving.value = true
    try {
        await updatePricingRuleApi(editId.value, editForm)
        ElMessage.success('更新成功')
        showDialog.value = false
        await loadRules()
    } finally {
        saving.value = false
    }
}

async function handleToggle(rule: PricingRule) {
    try {
        await updatePricingRuleApi(rule.id, { enabled: rule.enabled })
        ElMessage.success('更新成功')
    } catch {
        rule.enabled = !rule.enabled
    }
}

onMounted(loadRules)
</script>

<style scoped lang="scss">
.page-title {
    font-size: 20px;
    margin-bottom: 20px;
}
</style>
