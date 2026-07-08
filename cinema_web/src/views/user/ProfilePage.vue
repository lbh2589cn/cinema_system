<template>
    <div class="page-container">
        <el-card class="page-card">
            <template #header>
                <span class="card-title">个人中心</span>
            </template>

            <el-descriptions :column="1" border>
                <el-descriptions-item label="账号">
                    {{ profile.userId }}
                </el-descriptions-item>
                <el-descriptions-item label="用户名">
                    <div class="desc-content">
                        <template v-if="editingKey === 'username'">
                            <el-input v-model="editValue" size="small" :style="{ width: Math.max(120, editValue.length * 14 + 32) + 'px' }" class="dynamic-input" />
                            <span class="btn-group">
                                <el-button type="primary" size="small" @click="saveEdit('username')" :loading="saving">保存</el-button>
                                <el-button size="small" @click="cancelEdit">取消</el-button>
                            </span>
                        </template>
                        <template v-else>
                            <span>{{ profile.username }}</span>
                            <el-button type="primary" size="small" @click="startEdit('username')">
                                <el-icon><Edit /></el-icon>
                            </el-button>
                        </template>
                    </div>
                </el-descriptions-item>
                <el-descriptions-item label="手机号">
                    <div class="desc-content">
                        <template v-if="editingKey === 'phone'">
                            <el-input v-model="editValue" size="small" :style="{ width: Math.max(120, editValue.length * 14 + 32) + 'px' }" class="dynamic-input" />
                            <span class="btn-group">
                                <el-button type="primary" size="small" @click="saveEdit('phone')" :loading="saving">保存</el-button>
                                <el-button size="small" @click="cancelEdit">取消</el-button>
                            </span>
                        </template>
                        <template v-else>
                            <span>{{ profile.phone || '-' }}</span>
                            <el-button type="primary" size="small" @click="startEdit('phone')">
                                <el-icon><Edit /></el-icon>
                            </el-button>
                        </template>
                    </div>
                </el-descriptions-item>
                <el-descriptions-item label="角色">
                    <el-tag :type="profile.role === 'ADMIN' ? 'danger' : 'primary'">
                        {{ profile.role === 'ADMIN' ? '管理员' : '普通用户' }}
                    </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="会员">
                    <div class="desc-content">
                        <el-tag :type="profile.isMember ? 'success' : 'info'">
                            {{ profile.isMember ? '是' : '否' }}
                        </el-tag>
                        <el-button
                            :type="profile.isMember ? 'danger' : 'success'"
                            plain
                            size="small"
                            @click="handleMembership"
                        >
                            {{ profile.isMember ? '取消会员' : '加入会员' }}
                        </el-button>
                    </div>
                </el-descriptions-item>
                <el-descriptions-item label="状态">
                    <el-tag :type="profile.status === 'ACTIVE' ? 'success' : 'danger'">
                        {{ profile.status === 'ACTIVE' ? '正常' : '禁用' }}
                    </el-tag>
                </el-descriptions-item>
            </el-descriptions>

            <div class="card-actions">
                <el-button type="warning" plain @click="showChangePasswordDialog">
                    修改密码
                </el-button>
                <el-button type="danger" plain @click="confirmDeleteAccount">
                    注销账户
                </el-button>
            </div>
        </el-card>

        <!-- 修改密码对话框 -->
        <el-dialog v-model="passwordDialogVisible" title="修改密码" width="400px">
            <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
                <el-form-item label="原密码" prop="oldPassword">
                    <el-input v-model="passwordForm.oldPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                    <el-input v-model="passwordForm.newPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="确认新密码" prop="confirmPassword">
                    <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="passwordDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleChangePassword" :loading="changingPwd">确认修改</el-button>
            </template>
        </el-dialog>

        <!-- 会员付款对话框 -->
        <el-dialog v-model="memberPayDialogVisible" title="加入会员" width="420px">
            <div class="member-pay-info">
                <div class="member-price">¥10</div>
                <div class="member-desc">会员月费 · 享受专属折扣优惠</div>
            </div>
            <el-divider />
            <h3 class="pay-method-title">— 选择支付方式 —</h3>
            <el-radio-group v-model="memberPaymentMethod" class="pay-methods">
                <el-radio value="WECHAT" class="pay-method-item" border>
                    <el-icon><Iphone /></el-icon>
                    微信支付
                </el-radio>
                <el-radio value="ALIPAY" class="pay-method-item" border>
                    <el-icon><Wallet /></el-icon>
                    支付宝
                </el-radio>
            </el-radio-group>
            <template #footer>
                <el-button @click="memberPayDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleMemberPay" :loading="memberPaying">
                    确认支付
                </el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Iphone, Wallet } from '@element-plus/icons-vue'
import { getProfileApi, updateProfileApi, changePasswordApi, deleteAccountApi } from '@/api/auth'
import type { UserProfile } from '@/api/auth'
import { useAuth } from '@/composables/useAuth'
import type { FormInstance } from 'element-plus'

const router = useRouter()
const { logout } = useAuth()

const profile = ref<UserProfile>({} as UserProfile)

// 内联编辑
const editingKey = ref<string | null>(null)
const editValue = ref('')
const saving = ref(false)

function startEdit(key: string) {
    editingKey.value = key
    editValue.value = String(profile.value[key as keyof UserProfile] ?? '')
}

function cancelEdit() {
    editingKey.value = null
    editValue.value = ''
}

async function saveEdit(key: string) {
    saving.value = true
    try {
        await updateProfileApi({ [key]: editValue.value } as Partial<UserProfile>)
        ;(profile.value as any)[key] = editValue.value
        editingKey.value = null
        ElMessage.success('更新成功')
    } catch {
        ElMessage.error('更新失败')
    } finally {
        saving.value = false
    }
}

// 会员管理
const memberPayDialogVisible = ref(false)
const memberPaymentMethod = ref('WECHAT')
const memberPaying = ref(false)

function handleMembership() {
    if (profile.value.isMember) {
        ElMessageBox.confirm('确定要取消会员资格吗？', '取消会员', {
            confirmButtonText: '确认取消',
            cancelButtonText: '再想想',
            type: 'warning',
        }).then(async () => {
            await updateProfileApi({ isMember: false } as any)
            profile.value.isMember = false
            ElMessage.success('已取消会员')
        }).catch(() => {})
    } else {
        memberPaymentMethod.value = 'WECHAT'
        memberPayDialogVisible.value = true
    }
}

async function handleMemberPay() {
    memberPaying.value = true
    // 模拟支付延迟
    await new Promise(resolve => setTimeout(resolve, 300))
    try {
        await updateProfileApi({ isMember: true } as any)
        profile.value.isMember = true
        memberPayDialogVisible.value = false
        ElMessage.success('欢迎成为会员！')
    } catch {
        ElMessage.error('操作失败')
    } finally {
        memberPaying.value = false
    }
}

// 修改密码
const passwordDialogVisible = ref(false)
const changingPwd = ref(false)
const passwordFormRef = ref<FormInstance>()

const passwordForm = ref({
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
})

const passwordRules = {
    oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
    newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, message: '密码至少6位', trigger: 'blur' },
    ],
    confirmPassword: [
        { required: true, message: '请确认新密码', trigger: 'blur' },
        {
            validator: (_rule: any, value: string, callback: (e?: Error) => void) => {
                if (value !== passwordForm.value.newPassword) {
                    callback(new Error('两次输入的密码不一致'))
                } else {
                    callback()
                }
            },
            trigger: 'blur',
        },
    ],
}

function showChangePasswordDialog() {
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
    passwordDialogVisible.value = true
}

async function handleChangePassword() {
    const valid = await passwordFormRef.value?.validate().catch(() => false)
    if (!valid) return
    changingPwd.value = true
    try {
        await changePasswordApi({
            oldPassword: passwordForm.value.oldPassword,
            newPassword: passwordForm.value.newPassword,
        })
        ElMessage.success('密码修改成功')
        passwordDialogVisible.value = false
    } catch {
        ElMessage.error('密码修改失败，请检查原密码是否正确')
    } finally {
        changingPwd.value = false
    }
}

// 注销账户
async function confirmDeleteAccount() {
    try {
        await ElMessageBox.confirm(
            '确定要注销账户吗？注销后账号将被禁用且无法恢复。',
            '注销确认',
            { confirmButtonText: '确认注销', cancelButtonText: '取消', type: 'warning' }
        )
        await deleteAccountApi()
        ElMessage.success('账号已注销')
        logout()
        router.push('/login')
    } catch {
        // 取消操作不做处理
    }
}

onMounted(async () => {
    try {
        profile.value = await getProfileApi()
    } catch {
        ElMessage.error('获取用户信息失败')
    }
})
</script>

<style scoped lang="scss">
.card-title {
    font-size: 16px;
    font-weight: 600;
}

.el-descriptions {
    :deep(.el-descriptions__label) {
        width: 80px;
    }
}

.desc-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    min-height: 28px;
    width: 100%;
}

.dynamic-input {
    min-width: 120px;
    max-width: 600px;
    width: auto;
    flex-shrink: 1;
}

.btn-group {
    display: flex;
    gap: 6px;
    flex-shrink: 0;
}

.card-actions {
    margin-top: 24px;
    padding-top: 16px;
    border-top: 1px solid #ebeef5;
    display: flex;
    justify-content: flex-end;
    gap: 12px;
}

.member-pay-info {
    text-align: center;
    padding: 16px 0;

    .member-price {
        font-size: 36px;
        font-weight: 700;
        color: #f56c6c;
    }

    .member-desc {
        margin-top: 8px;
        font-size: 14px;
        color: #909399;
    }
}

.pay-method-title {
    text-align: center;
    font-size: 15px;
    font-weight: 600;
    margin: 0 0 16px;
}

.pay-methods {
    display: flex;
    justify-content: center;
    gap: 16px;
}

.pay-method-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 24px;
    border-radius: 8px;
}
</style>
