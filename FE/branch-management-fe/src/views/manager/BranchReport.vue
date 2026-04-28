<template>
  <div class="px-8 py-16 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[1440px] mx-auto">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-12 gap-6">
        <div>
          <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">Trung tâm Báo cáo Chi nhánh</h2>
          <p class="text-[18px] text-[#5D6C7B] mt-2 font-normal">Dữ liệu tổng hợp về nhân sự và luân chuyển hàng hóa</p>
        </div>
        <div class="flex gap-4">
          <el-button class="meta-btn-primary shadow-[0_2px_4px_0_rgba(0,0,0,0.1)]" @click="handleExportAttendance" :loading="exportLoadingAtt">
            <span class="mr-2 text-[16px]">📥</span> Xuất Excel Chấm công
          </el-button>
          <el-button class="meta-btn-primary shadow-[0_2px_4px_0_rgba(0,0,0,0.1)]" @click="handleExportInventory" :loading="exportLoadingInv">
            <span class="mr-2 text-[16px]">📥</span> Xuất Excel Kho
          </el-button>
        </div>
      </div>

      <el-row :gutter="24" class="mb-10">
        <el-col :span="24" :md="8" class="mb-4 md:mb-0">
          <el-card shadow="never" class="meta-stat-card shadow-[0_12px_28px_0_rgba(0,0,0,0.05)] border-t-[4px] border-t-[#0064E0]">
            <div class="text-[14px] font-bold text-[#5D6C7B] mb-2 uppercase tracking-wide">Tổng nhân sự chi nhánh</div>
            <div class="text-[48px] font-medium text-[#0064E0] leading-none">{{ summary.totalStaff }}</div>
          </el-card>
        </el-col>
        
        <el-col :span="24" :md="8" class="mb-4 md:mb-0">
          <el-card shadow="never" class="meta-stat-card shadow-[0_12px_28px_0_rgba(0,0,0,0.05)] border-t-[4px] border-t-[#F7B928]">
            <div class="text-[14px] font-bold text-[#5D6C7B] mb-2 uppercase tracking-wide">Sản phẩm dưới định mức (Min)</div>
            <div class="text-[48px] font-medium text-[#F7B928] leading-none">{{ summary.lowStockCount }}</div>
          </el-card>
        </el-col>

        <el-col :span="24" :md="8">
          <el-card shadow="never" class="meta-stat-card shadow-[0_12px_28px_0_rgba(0,0,0,0.05)] border-t-[4px] border-t-[#E41E3F]">
            <div class="text-[14px] font-bold text-[#5D6C7B] mb-2 uppercase tracking-wide">Chấm công bất thường</div>
            <div class="text-[48px] font-medium text-[#E41E3F] leading-none">{{ summary.abnormalAttendance }}</div>
          </el-card>
        </el-col>
      </el-row>

      <div class="bg-white rounded-[24px] shadow-[0_12px_28px_0_rgba(0,0,0,0.05)] overflow-hidden">
        <el-tabs type="card" class="meta-tabs pt-4 px-4">
          <el-tab-pane>
            <template #label>
              <div class="text-[16px] font-medium flex items-center gap-2 px-2"><span>📅</span> Báo cáo Chấm công</div>
            </template>
            <div class="p-6">
              <div class="mb-6 flex gap-4 items-center">
                <span class="text-[14px] font-bold text-[#1C2B33]">Kỳ báo cáo:</span>
                <el-date-picker 
                  v-model="attendanceDate" 
                  type="month" 
                  placeholder="Chọn tháng"
                  format="MM/YYYY"
                  value-format="YYYY-MM"
                  @change="fetchAttendanceReport"
                  class="meta-input w-64"
                />
              </div>
              <el-table :data="attendanceData" class="meta-table" style="width: 100%" v-loading="loading">
                <el-table-column prop="username" label="Mã NV" width="160">
                  <template #default="{row}"><span class="text-[#5D6C7B] font-mono">{{ row.username }}</span></template>
                </el-table-column>
                <el-table-column prop="fullName" label="Họ tên nhân viên" min-width="250">
                  <template #default="{row}"><span class="text-[#1C2B33] font-medium text-[16px]">{{ row.fullName }}</span></template>
                </el-table-column>
                <el-table-column prop="totalHours" label="Tổng giờ công" width="180" align="center">
                  <template #default="{row}"><span class="text-[#0064E0] font-bold text-[18px]">{{ row.totalHours }}h</span></template>
                </el-table-column>
                <el-table-column prop="abnormalDays" label="Số ngày bất thường" width="200" align="center">
                  <template #default="{row}">
                    <span v-if="row.abnormalDays > 0" class="text-[#E41E3F] font-bold text-[18px]">{{ row.abnormalDays }}</span>
                    <span v-else class="text-[#CED0D4]">—</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>

          <el-tab-pane>
            <template #label>
              <div class="text-[16px] font-medium flex items-center gap-2 px-2"><span>📦</span> Báo cáo Nhập - Xuất - Tồn</div>
            </template>
            <div class="p-6">
              <div class="mb-6 text-[14px] text-[#5D6C7B] bg-[#F1F4F7] p-4 rounded-[12px] flex items-center gap-2">
                <span>ℹ️</span> Báo cáo chi tiết biến động số lượng hàng hóa vật lý tại kho trong kỳ hiện tại.
              </div>
              <el-table :data="inventoryData" class="meta-table" style="width: 100%" v-loading="loading">
                <el-table-column prop="productName" label="Sản phẩm" min-width="250">
                  <template #default="{row}"><span class="text-[#1C2B33] font-medium">{{ row.productName }}</span></template>
                </el-table-column>
                <el-table-column prop="openingStock" label="Tồn đầu kỳ" width="150" align="right">
                  <template #default="{row}"><span class="text-[#5D6C7B]">{{ row.openingStock }}</span></template>
                </el-table-column>
                <el-table-column prop="inQuantity" label="Nhập (+)" width="150" align="right">
                  <template #default="{row}"><span class="text-[#31A24C] font-medium">+{{ row.inQuantity }}</span></template>
                </el-table-column>
                <el-table-column prop="outQuantity" label="Xuất (-)" width="150" align="right">
                  <template #default="{row}"><span class="text-[#E41E3F] font-medium">-{{ row.outQuantity }}</span></template>
                </el-table-column>
                <el-table-column prop="closingStock" label="Tồn cuối kỳ" width="150" align="right">
                  <template #default="{row}"><span class="text-[#1C2B33] font-bold text-[18px]">{{ row.closingStock }}</span></template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '@/api/axios';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();
const loading = ref(false);
const exportLoadingAtt = ref(false);
const exportLoadingInv = ref(false);
const attendanceDate = ref(new Date().toISOString().slice(0, 7)); // Khởi tạo YYYY-MM
const summary = ref({ totalStaff: 0, lowStockCount: 0, abnormalAttendance: 0 });
const attendanceData = ref([]);
const inventoryData = ref([]);

const fetchSummary = async () => {
  try {
    const res = await api.get('/manager/reports/summary');
    if (res.data?.success) summary.value = res.data.data;
  } catch (error) {
    console.error("Lỗi tải tóm tắt báo cáo:", error);
  }
};

const fetchAttendanceReport = async () => {
  loading.value = true;
  try {
    const res = await api.get('/manager/reports/attendance', {
      params: { month: attendanceDate.value }
    });
    attendanceData.value = res.data?.data || [];
  } catch (error) {
    ElMessage.error("Không thể tải báo cáo chấm công");
  } finally {
    loading.value = false;
  }
};

const fetchInventoryReport = async () => {
  try {
    const res = await api.get('/manager/reports/inventory');
    inventoryData.value = res.data?.data || [];
  } catch (error) {
    console.error("Lỗi tải báo cáo kho");
  }
};

const handleExportAttendance = async () => {
  if (!auth.user?.branchId) return ElMessage.error("Không tìm thấy chi nhánh");
  exportLoadingAtt.value = true;
  try {
    const dateStr = attendanceDate.value || new Date().toISOString().slice(0, 7);
    const [year, month] = dateStr.split('-').map(Number);
    const startDate = new Date(year, month - 1, 1).toISOString().split('T')[0];
    const endDate = new Date(year, month, 0).toISOString().split('T')[0];
    
    const res = await api.get(`/manager/reports/branch/${auth.user.branchId}/attendance/excel`, {
      params: { startDate, endDate },
      responseType: 'blob'
    });
    const url = window.URL.createObjectURL(new Blob([res.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `ChamCong_${auth.user.branchId}_${startDate}.xlsx`);
    document.body.appendChild(link);
    link.click();
    ElMessage.success("Đã tải xuống Excel chấm công");
  } catch (error) {
    console.error(error);
    ElMessage.error("Lỗi khi xuất file Excel chấm công");
  } finally {
    exportLoadingAtt.value = false;
  }
};

const handleExportInventory = async () => {
  if (!auth.user?.branchId) return ElMessage.error("Không tìm thấy chi nhánh");
  exportLoadingInv.value = true;
  try {
    const res = await api.get(`/manager/reports/branch/${auth.user.branchId}/inventory/excel`, {
      responseType: 'blob'
    });
    const url = window.URL.createObjectURL(new Blob([res.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `TonKho_${auth.user.branchId}.xlsx`);
    document.body.appendChild(link);
    link.click();
    ElMessage.success("Đã tải xuống Excel tồn kho");
  } catch (error) {
    console.error(error);
    ElMessage.error("Lỗi khi xuất file Excel tồn kho");
  } finally {
    exportLoadingInv.value = false;
  }
};

onMounted(() => {
  fetchSummary();
  fetchAttendanceReport();
  fetchInventoryReport();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap');
.font-meta { font-family: 'Montserrat', sans-serif; }

:deep(.meta-stat-card) { border-radius: 20px !important; border-left: none; border-right: none; border-bottom: none; background-color: #FFFFFF !important; padding: 28px 24px !important; }

:deep(.meta-btn-primary) { border-radius: 100px !important; background-color: #0064E0 !important; border-color: #0064E0 !important; color: #FFFFFF !important; font-family: 'Montserrat', sans-serif; font-weight: 500; font-size: 14px; padding: 10px 24px !important; height: auto; transition: all 0.2s ease; }
:deep(.meta-btn-primary:hover) { background-color: #0143B5 !important; transform: scale(1.02); }

:deep(.meta-input .el-input__wrapper) { border-radius: 8px !important; border: 1px solid #CED0D4 !important; box-shadow: none !important; padding: 8px 12px; }
:deep(.meta-input .el-input__wrapper.is-focus) { border-color: #0064E0 !important; box-shadow: 0 0 0 3px rgba(0, 100, 224, 0.2) !important; }

/* Tabs Styling */
:deep(.meta-tabs.el-tabs--card > .el-tabs__header) { border-bottom: 1px solid #DEE3E9; margin: 0; padding-bottom: 10px; }
:deep(.meta-tabs.el-tabs--card > .el-tabs__header .el-tabs__nav) { border: none; }
:deep(.meta-tabs.el-tabs--card > .el-tabs__header .el-tabs__item) { border: none; font-family: 'Montserrat', sans-serif; color: #5D6C7B; transition: all 0.2s; padding: 0 24px; border-radius: 100px; margin-right: 8px; }
:deep(.meta-tabs.el-tabs--card > .el-tabs__header .el-tabs__item.is-active) { color: #0064E0; background-color: #E8F3FF; }
:deep(.meta-tabs.el-tabs--card > .el-tabs__header .el-tabs__item:hover) { color: #0064E0; }

/* Table */
:deep(.meta-table th.el-table__cell) { background-color: #FFFFFF !important; color: #5D6C7B !important; font-weight: 500 !important; border-bottom: 1px solid #DEE3E9 !important; padding: 24px 0 16px 0 !important; font-size: 14px; }
:deep(.meta-table td.el-table__cell) { border-bottom: 1px solid #F1F4F7 !important; padding: 24px 0 !important; }
</style>