<template>
  <div class="px-8 py-16 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[1440px] mx-auto">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-12 gap-6">
        <div>
          <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">Sổ Chi Tiết Biến Động Kho</h2>
          <div class="mt-2 flex items-center gap-2">
            <el-tag v-if="filterProductId" class="meta-tag-info border-0">🔍 Đang truy vết SP ID: {{ filterProductId }}</el-tag>
            <el-tag v-if="contextBranchId" class="meta-tag-info border-0 bg-[#E8F3FF] text-[#0064E0]">📍 Kho đang xem: Chi nhánh {{ contextBranchId }}</el-tag>
            <el-tag v-else class="meta-tag-info border-0 bg-[#F2F0E6] text-[#6441D2]">🌐 Đang xem TỔNG TOÀN HỆ THỐNG</el-tag>
          </div>
        </div>
        <el-button class="meta-btn-outline" @click="fetchHistory" :loading="isLoading">
          <span class="mr-2">↻</span> Làm mới sổ
        </el-button>
      </div>

      <el-card shadow="never" class="meta-card mb-8">
        <div class="flex gap-4 items-center">
          <span class="text-[14px] font-bold text-[#1C2B33]">Kỳ biến động:</span>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="→"
            start-placeholder="Từ ngày"
            end-placeholder="Đến ngày"
            format="DD/MM/YYYY"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
            clearable
            class="meta-input w-[350px]"
          />
        </div>
      </el-card>

      <el-card shadow="never" class="meta-card shadow-[0_12px_28px_0_rgba(0,0,0,0.05)]">
        <el-table :data="historyRequests" v-loading="isLoading" class="meta-table" style="width: 100%">
          <el-table-column label="Mã Phiếu" prop="transactionCode" width="160">
            <template #default="{row}"><span class="font-bold text-[#1C2B33]">{{ row.transactionCode }}</span></template>
          </el-table-column>

          <el-table-column label="Số lượng thay đổi" width="200" align="center">
            <template #default="{row}">
              <div v-if="row.details && row.details.length > 0">
                <div v-for="item in row.details" :key="item.productId">
                  <div v-if="!filterProductId || Number(item.productId) === filterProductId">
                    <span :class="getChangeClass(row.transactionType, row)" class="text-[24px] font-black">
                      {{ getChangePrefix(row.transactionType, row) }}{{ item.quantity }}
                    </span>
                    <span class="text-[14px] text-[#5D6C7B] ml-1">{{ item.productUnit }}</span>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="Sản phẩm" min-width="250">
            <template #default="{row}">
              <div v-if="row.details">
                <div v-for="item in row.details" :key="item.productId">
                  <div v-if="!filterProductId || Number(item.productId) === filterProductId" class="font-medium text-[#1C2B33]">
                    {{ item.productName }}
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="Chi tiết giao dịch tại kho" min-width="300">
            <template #default="{row}">
              <div v-if="row.transactionType?.toLowerCase() === 'transfer'" class="bg-[#F1F4F7] p-3 rounded-[8px] border border-[#DEE3E9] text-[13px]">
                <div class="mb-1"><span class="text-[#E41E3F] font-bold">📤 Rời khỏi:</span> <span class="text-[#5D6C7B]">{{ row.fromBranchName || 'Kho ' + row.fromBranchId }}</span></div>
                <div><span class="text-[#31A24C] font-bold">📥 Nhập vào:</span> <span class="text-[#5D6C7B]">{{ row.toBranchName || 'Kho ' + row.toBranchId }}</span></div>
              </div>
              <div v-else-if="row.transactionType?.toLowerCase() === 'import'" class="bg-[#E6F4EA] p-3 rounded-[8px] border border-[#31A24C] border-opacity-20 text-[13px]">
                <span class="text-[#31A24C] font-bold">📥 Nhập thẳng vào:</span> <span class="text-[#1C2B33] font-medium">{{ row.toBranchName }}</span>
              </div>
              <div v-else class="bg-[#FCE8EB] p-3 rounded-[8px] border border-[#E41E3F] border-opacity-20 text-[13px]">
                <span class="text-[#E41E3F] font-bold">📤 Xuất khỏi:</span> <span class="text-[#1C2B33] font-medium">{{ row.fromBranchName }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="Ngày GD" width="180" align="center" fixed="right">
            <template #default="{row}"><span class="text-[#5D6C7B]">{{ formatDate(row.updatedAt) }}</span></template>
          </el-table-column>
        </el-table>

        <div class="mt-8 flex justify-between items-center border-t border-[#DEE3E9] pt-6">
          <span class="text-[14px] text-[#5D6C7B]">Tổng cộng: <b class="text-[#1C2B33]">{{ totalElements }}</b> bản ghi</span>
          <el-pagination
            background
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            layout="sizes, prev, pager, next"
            :total="totalElements"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue';
import { useRoute } from 'vue-router';
import api from '@/api/axios.js';
import { ElMessage } from 'element-plus';

const route = useRoute();

const historyRequests = ref([]);
const isLoading = ref(false);
const totalElements = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const dateRange = ref([]); 

const filterProductId = computed(() => route.query.productId && route.query.productId !== 'null' ? Number(route.query.productId) : null);
const contextBranchId = computed(() => route.query.branchId && route.query.branchId !== 'null' ? Number(route.query.branchId) : null);

const formatDate = (d) => d ? new Date(d).toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric' }) : '---';

const fetchHistory = async () => {
  if (isLoading.value) return; 
  isLoading.value = true;
  try {
    const startDate = dateRange.value?.length === 2 ? dateRange.value[0] : null;
    const endDate = dateRange.value?.length === 2 ? dateRange.value[1] : null;

    const res = await api.get('/manager/stock-requests', {
      params: {
        page: currentPage.value - 1,
        size: pageSize.value,
        productId: filterProductId.value,
        branchId: contextBranchId.value, 
        startDate: startDate, 
        endDate: endDate,     
        sort: 'updatedAt,desc'
      }
    });

    const pageData = res.data?.data;
    historyRequests.value = pageData?.content || [];
    totalElements.value = pageData?.totalElements || 0;
  } catch (error) {
    ElMessage.error('Không thể tải dữ liệu biến động');
  } finally {
    isLoading.value = false;
  }
};

const handleDateChange = () => {
  currentPage.value = 1; 
  fetchHistory();
};

const getChangePrefix = (type, row) => {
  const t = type?.toLowerCase();
  const currentId = contextBranchId.value; 

  if (t === 'import') return '+ ';
  if (t === 'export') return '- ';
  if (t === 'transfer') {
    if (!currentId) return '⇆ '; 
    if (row.toBranchId === currentId) return '+ ';
    if (row.fromBranchId === currentId) return '- ';
  }
  return '';
};

const getChangeClass = (type, row) => {
  const p = getChangePrefix(type, row);
  if (p === '+ ') return 'text-[#31A24C]'; // Meta Success Green
  if (p === '- ') return 'text-[#E41E3F]'; // Meta Error Red
  if (p === '⇆ ') return 'text-[#0064E0]'; // Meta Blue
  return 'text-[#5D6C7B]';
};

const handlePageChange = (val) => { currentPage.value = val; fetchHistory(); };
const handleSizeChange = (val) => { pageSize.value = val; currentPage.value = 1; fetchHistory(); };

watch(() => route.query.productId, () => { currentPage.value = 1; fetchHistory(); });

onMounted(fetchHistory);
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap');
.font-meta { font-family: 'Montserrat', sans-serif; }

:deep(.meta-card) { border-radius: 20px !important; border: none !important; background-color: #FFFFFF !important; padding: 16px; }

:deep(.meta-btn-outline) { background-color: transparent !important; color: #1C2B33 !important; border: 2px solid rgba(10, 19, 23, 0.12) !important; border-radius: 100px !important; font-family: 'Montserrat', sans-serif; font-weight: 500; font-size: 14px; padding: 10px 22px !important; height: auto; transition: all 0.2s ease; }
:deep(.meta-btn-outline:hover) { background-color: rgba(70, 90, 105, 0.05) !important; }

:deep(.meta-input .el-input__wrapper) { border-radius: 8px !important; border: 1px solid #CED0D4 !important; box-shadow: none !important; padding: 8px 12px; }
:deep(.meta-input .el-input__wrapper.is-focus) { border-color: #0064E0 !important; box-shadow: 0 0 0 3px rgba(0, 100, 224, 0.2) !important; }

:deep(.meta-table th.el-table__cell) { background-color: #FFFFFF !important; color: #5D6C7B !important; font-weight: 500 !important; border-bottom: 1px solid #DEE3E9 !important; padding: 24px 0 16px 0 !important; font-size: 14px; }
:deep(.meta-table td.el-table__cell) { border-bottom: 1px solid #F1F4F7 !important; padding: 20px 0 !important; }

:deep(.meta-tag-info) { border-radius: 100px; font-weight: 700; padding: 6px 16px; height: auto; }

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) { background-color: #0064E0 !important; border-radius: 8px; }
</style>