<template>
  <div class="px-8 py-16 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[1440px] mx-auto">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-12 gap-6">
        <div>
          <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">Quản lý Nhân sự</h2>
          <p class="text-[18px] text-[#5D6C7B] mt-2 font-normal">Danh sách tài khoản và phân quyền người dùng hệ thống</p>
        </div>
        <el-button class="meta-btn-primary shadow-[0_2px_4px_0_rgba(0,0,0,0.1)]" @click="$router.push('/admin/users/new')">
          <span class="mr-2 text-[18px]">+</span> Thêm nhân viên
        </el-button>
      </div>

      <el-card shadow="never" class="meta-card mb-8">
        <div class="flex flex-wrap gap-6 items-center">
          <div class="flex-1 min-w-[250px]">
             <el-input 
              v-model="searchKeyword" 
              @input="handleSearch" 
              placeholder="Tìm theo tên hoặc username..." 
              clearable
              class="meta-input w-full"
            >
              <template #prefix><span class="text-[#65676B]">🔍</span></template>
            </el-input>
          </div>
         
          <div class="flex items-center gap-3" v-if="auth.role && auth.role.includes('ADMIN')">
            <span class="text-[14px] font-bold text-[#1C2B33] whitespace-nowrap">Chi nhánh:</span>
            <el-select v-model="selectedBranch" @change="resetAndLoad" class="meta-input w-64" placeholder="-- Tất cả chi nhánh --" clearable>
              <el-option v-for="b in branches" :key="b.branchId" :label="b.branchName" :value="b.branchId" />
            </el-select>
          </div>
          <div v-else class="text-[14px] text-[#5D6C7B] bg-[#F1F4F7] px-4 py-2 rounded-[8px]">
            Đang xem nhân sự chi nhánh:&nbsp;<b class="text-[#1C2B33]">{{ auth.user?.branchName || 'Của bạn' }}</b>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="meta-card shadow-[0_12px_28px_0_rgba(0,0,0,0.05)]">
        <el-table :data="users" v-loading="loading" class="meta-table" style="width: 100%">
          <el-table-column prop="username" label="Username" width="160">
            <template #default="{row}">
              <span class="text-[#5D6C7B] font-mono bg-[#F1F4F7] px-2 py-1 rounded-[6px]">{{ row.username }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="fullName" label="Họ tên" min-width="220">
            <template #default="{row}">
              <span class="text-[16px] font-medium text-[#1C2B33]">{{ row.fullName }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="role" label="Vai trò" width="160">
            <template #default="{row}">
              <span class="text-[#0064E0] font-bold">{{ row.role }}</span>
            </template>
          </el-table-column>

          <el-table-column label="Chi nhánh" min-width="200">
            <template #default="{row}">
              <span class="text-[#5D6C7B]">{{ row.branch?.branchName || row.branchName || 'Chưa gán' }}</span>
            </template>
          </el-table-column>

          <el-table-column label="Trạng thái" width="160" align="center">
            <template #default="{row}">
              <el-tag :class="row.status?.toUpperCase() === 'ACTIVE' ? 'meta-tag-success' : 'meta-tag-error'">
                {{ row.status?.toUpperCase() }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="Thao tác" width="300" align="center" fixed="right">
            <template #default="{row}">
              <div class="flex justify-center gap-2">
                <el-button class="meta-btn-secondary-sm" @click="$router.push(`/admin/users/edit/${row.userId}`)">Sửa</el-button>
                <el-button class="meta-btn-secondary-sm" @click="handleResetPassword(row.userId)">🔑 Reset</el-button>
                <el-button 
                  v-if="row.status?.toUpperCase() === 'ACTIVE'" 
                  class="meta-btn-danger-sm" 
                  @click="handleLock(row.userId)"
                >Khóa</el-button>
                <el-button 
                  v-else 
                  class="meta-btn-outline-sm text-[#D6311F]" 
                  @click="handleUnlock(row.userId)"
                >Mở khóa</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="mt-8 flex justify-end" v-if="!selectedBranch && !searchKeyword && totalPages > 1">
           <el-pagination
            background
            layout="prev, pager, next"
            :total="totalPages * pageSize"
            :page-size="pageSize"
            :current-page="currentPage + 1"
            @current-change="p => changePage(p - 1)"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { userApi } from '@/api/user';
import { branchApi } from '@/api/branch';
import { useAuthStore } from '@/stores/auth';
import { ElMessage, ElMessageBox } from 'element-plus';

const auth = useAuthStore();
const users = ref([]);
const originalUsers = ref([]);
const branches = ref([]);
const selectedBranch = ref('');
const searchKeyword = ref('');
const currentPage = ref(0);
const totalPages = ref(1);
const pageSize = 10;
const loading = ref(false);

const loadData = async () => {
  try {
    if (auth.role && auth.role.includes('ADMIN')) {
      const resBranches = await branchApi.getAll();
      if (resBranches?.data?.success) {
        branches.value = resBranches.data.data || [];
      }
    } else {
      selectedBranch.value = auth.user?.branchId || auth.branchId || '';
    }
    await loadUsers();
  } catch (error) {
    console.error("Lỗi tải dữ liệu ban đầu:", error);
  }
};

const loadUsers = async () => {
  loading.value = true;
  try {
    let res;
    let dataList = [];

    if (auth.role && auth.role.includes('MANAGER')) {
      // ✅ ĐÃ SỬA: Fallback từ auth.user?.branchId → auth.branchId → sessionStorage
      const myBranchId = auth.user?.branchId || auth.branchId || sessionStorage.getItem('branchId');
      if (!myBranchId || myBranchId === 'null' || myBranchId === 'undefined') {
        dataList = [];
      } else {
        res = await userApi.getByBranch(myBranchId);
        dataList = res.data?.data || [];
      }
      totalPages.value = 1; 
    } else {
      if (selectedBranch.value) {
        res = await userApi.getByBranch(selectedBranch.value);
        dataList = res.data?.data || [];
        totalPages.value = 1;
      } else {
        res = await userApi.getAllPaged(currentPage.value, pageSize);
        dataList = res.data?.data?.content || [];
        totalPages.value = res.data?.data?.totalPages || 1;
      }
    }

    originalUsers.value = dataList;
    users.value = dataList;
    if (searchKeyword.value) handleSearch();
  } catch (error) {
    users.value = [];
  } finally {
    loading.value = false;
  }
};

const resetAndLoad = () => {
  currentPage.value = 0;
  searchKeyword.value = ''; 
  loadUsers();
};

const changePage = (p) => {
  currentPage.value = p;
  loadUsers();
};

const handleSearch = () => {
  if (!searchKeyword.value) {
    users.value = [...originalUsers.value]; 
    return;
  }
  const keyword = searchKeyword.value.toLowerCase();
  users.value = originalUsers.value.filter(u => 
    (u.fullName && u.fullName.toLowerCase().includes(keyword)) || 
    (u.username && u.username.toLowerCase().includes(keyword))
  );
};

const handleLock = async (id) => {
  ElMessageBox.confirm("Bạn có chắc chắn muốn KHÓA tài khoản này?", "Cảnh báo", {
    confirmButtonText: "Khóa tài khoản", cancelButtonText: "Hủy", type: "warning"
  }).then(async () => {
    try {
      await userApi.lock(id); 
      ElMessage.success("Đã khóa tài khoản");
      await loadUsers();
    } catch (e) {
      ElMessage.error(e.response?.data?.message || "Lỗi khóa tài khoản");
    }
  }).catch(() => {});
};

const handleUnlock = async (id) => {
  ElMessageBox.confirm("Mở khóa cho tài khoản này?", "Xác nhận", {
    confirmButtonText: "Mở khóa", cancelButtonText: "Hủy", type: "success"
  }).then(async () => {
    try {
      await userApi.unlock(id); 
      ElMessage.success("Đã mở khóa tài khoản");
      await loadUsers();
    } catch (e) {
      ElMessage.error(e.response?.data?.message || "Lỗi mở khóa tài khoản");
    }
  }).catch(() => {});
};

// ✅ ĐÃ THÊM: Reset mật khẩu (UC 1.1)
const handleResetPassword = (id) => {
  ElMessageBox.prompt('Nhập mật khẩu mới cho tài khoản:', 'Reset mật khẩu', {
    confirmButtonText: 'Đặt lại', cancelButtonText: 'Hủy',
    inputPattern: /^.{6,}$/, inputErrorMessage: 'Mật khẩu phải có ít nhất 6 ký tự'
  }).then(async ({ value }) => {
    try {
      await userApi.resetPassword(id, value);
      ElMessage.success("Đã reset mật khẩu thành công");
    } catch (e) {
      ElMessage.error(e.response?.data?.message || "Lỗi reset mật khẩu");
    }
  }).catch(() => {});
};

onMounted(loadData);
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap');

.font-meta { font-family: 'Montserrat', sans-serif; }

:deep(.meta-btn-primary) {
  border-radius: 100px !important;
  background-color: #0064E0 !important;
  border-color: #0064E0 !important;
  color: #FFFFFF !important;
  font-family: 'Montserrat', sans-serif;
  font-weight: 500;
  font-size: 14px;
  padding: 10px 24px !important;
  height: auto;
  transition: all 0.2s ease;
}
:deep(.meta-btn-primary:hover) { background-color: #0143B5 !important; transform: scale(1.02); }

:deep(.meta-btn-secondary-sm) {
  background-color: rgba(0, 100, 224, 0.1) !important;
  color: #0064E0 !important;
  border: none !important;
  border-radius: 100px !important;
  font-family: 'Montserrat', sans-serif;
  font-weight: 600;
  font-size: 13px;
  padding: 8px 16px !important;
  height: auto;
  transition: all 0.2s ease;
}
:deep(.meta-btn-secondary-sm:hover) { background-color: rgba(0, 100, 224, 0.2) !important; }

:deep(.meta-btn-danger-sm) {
  background-color: rgba(228, 30, 63, 0.1) !important;
  color: #E41E3F !important;
  border: none !important;
  border-radius: 100px !important;
  font-family: 'Montserrat', sans-serif;
  font-weight: 600;
  font-size: 13px;
  padding: 8px 16px !important;
  height: auto;
  transition: all 0.2s ease;
}
:deep(.meta-btn-danger-sm:hover) { background-color: rgba(228, 30, 63, 0.2) !important; }

:deep(.meta-btn-outline-sm) {
  background-color: transparent !important;
  color: #1C2B33 !important;
  border: 1px solid rgba(10, 19, 23, 0.2) !important;
  border-radius: 100px !important;
  font-family: 'Montserrat', sans-serif;
  font-weight: 500;
  font-size: 13px;
  padding: 8px 16px !important;
  height: auto;
  transition: all 0.2s ease;
}
:deep(.meta-btn-outline-sm:hover) { background-color: rgba(70, 90, 105, 0.05) !important; }

:deep(.meta-card) {
  border-radius: 20px !important;
  border: none !important;
  background-color: #FFFFFF !important;
  padding: 16px;
}

:deep(.meta-input .el-input__wrapper), :deep(.meta-input .el-select__wrapper) {
  border-radius: 8px !important;
  border: 1px solid #CED0D4 !important;
  box-shadow: none !important;
  padding: 8px 12px;
}
:deep(.meta-input .el-input__wrapper.is-focus), :deep(.meta-input .el-select__wrapper.is-focus) {
  border-color: #0064E0 !important;
  box-shadow: 0 0 0 3px rgba(0, 100, 224, 0.2) !important;
}

:deep(.meta-table th.el-table__cell) {
  background-color: #FFFFFF !important;
  color: #5D6C7B !important;
  font-weight: 500 !important;
  border-bottom: 1px solid #DEE3E9 !important;
  padding: 24px 0 16px 0 !important;
  font-size: 14px;
}
:deep(.meta-table td.el-table__cell) {
  border-bottom: 1px solid #F1F4F7 !important;
  padding: 20px 0 !important;
}

:deep(.meta-tag-success), :deep(.meta-tag-error) {
  border-radius: 100px;
  font-weight: 700;
  border: none;
  padding: 6px 16px;
  height: auto;
}
:deep(.meta-tag-success) { background-color: rgba(36, 228, 0, 0.15); color: #007D1E; }
:deep(.meta-tag-error) { background-color: rgba(255, 123, 145, 0.15); color: #E41E3F; }

/* Pagination */
:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: #0064E0 !important;
  border-radius: 8px;
}
:deep(.el-pagination.is-background .el-pager li) {
  border-radius: 8px;
  font-family: 'Montserrat', sans-serif;
}
</style>