<template>
  <div class="px-8 py-16 md:px-16 bg-[#F1F4F7] min-h-screen font-meta">
    <div class="max-w-[1440px] mx-auto">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-12 gap-6">
        <div>
          <h2 class="text-[36px] font-medium text-[#1C2B33] tracking-tight leading-[1.28]">Yêu Cầu Xuất Nhập Kho</h2>
          <p class="text-[18px] text-[#5D6C7B] mt-2 font-normal">Khởi tạo phiếu luân chuyển vật lý hàng hóa giữa các điểm bán</p>
        </div>
      </div>

      <el-row :gutter="24" class="mb-12">
        <el-col :xs="24" :sm="8" class="mb-6 md:mb-0">
          <el-card shadow="never" class="meta-stat-card border-t-[4px] border-t-[#31A24C] cursor-pointer transition-transform hover:-translate-y-1" @click="openDialog('import')">
            <div class="flex items-center gap-5">
              <div class="w-14 h-14 rounded-full bg-[#E6F4EA] text-[#31A24C] flex items-center justify-center text-[24px]">📥</div>
              <div>
                <div class="font-bold text-[#1C2B33] text-[18px]">Nhập Kho</div>
                <div class="text-[14px] text-[#5D6C7B]">Yêu cầu bổ sung hàng từ NCC</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="8" class="mb-6 md:mb-0">
          <el-card shadow="never" class="meta-stat-card border-t-[4px] border-t-[#E41E3F] cursor-pointer transition-transform hover:-translate-y-1" @click="openDialog('export')">
            <div class="flex items-center gap-5">
              <div class="w-14 h-14 rounded-full bg-[#FCE8EB] text-[#E41E3F] flex items-center justify-center text-[24px]">📤</div>
              <div>
                <div class="font-bold text-[#1C2B33] text-[18px]">Xuất Kho</div>
                <div class="text-[14px] text-[#5D6C7B]">Xuất bán lẻ hoặc xuất hủy hàng</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="8">
          <el-card shadow="never" class="meta-stat-card border-t-[4px] border-t-[#0064E0] cursor-pointer transition-transform hover:-translate-y-1" @click="openDialog('transfer')">
            <div class="flex items-center gap-5">
              <div class="w-14 h-14 rounded-full bg-[#E8F3FF] text-[#0064E0] flex items-center justify-center text-[24px]">⇆</div>
              <div>
                <div class="font-bold text-[#1C2B33] text-[18px]">Điều Chuyển</div>
                <div class="text-[14px] text-[#5D6C7B]">Luân chuyển giữa các chi nhánh</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-dialog v-model="dialogVisible" :title="getDialogTitle" width="850px" class="meta-dialog" top="5vh" destroy-on-close>
        <div class="flex flex-col gap-6 pt-2">
          
          <div class="bg-[#F7F8FA] p-6 rounded-[16px] border border-[#DEE3E9]">
            <h3 class="text-[16px] font-bold text-[#0064E0] mb-4 flex items-center gap-2"><span>1️⃣</span> Tuyến luân chuyển & Lý do</h3>
            <el-row :gutter="24">
              <el-col :span="12" v-if="isExport || isTransfer">
                <div class="flex flex-col gap-2">
                  <label class="text-[14px] font-bold text-[#1C2B33]">Kho nguồn (Xuất từ):</label>
                  <el-select v-model="form.fromBranchId" :placeholder="isTransfer ? 'Sử dụng Quét Kho bên dưới 👇' : 'Chọn kho xuất'" class="meta-input w-full" :disabled="isTransfer || (isExport && !isAdmin)">
                    <el-option v-for="b in branches" :key="b.branchId" :label="b.branchName" :value="b.branchId" />
                  </el-select>
                  <span v-if="isExport && !isAdmin" class="text-[12px] text-[#8595A4] italic">Mặc định xuất từ chi nhánh của bạn.</span>
                </div>
              </el-col>

              <el-col :span="12" v-if="isImport || isTransfer">
                <div class="flex flex-col gap-2">
                  <label class="text-[14px] font-bold text-[#1C2B33]">Kho đích (Nhận tại):</label>
                  <el-select v-model="form.toBranchId" placeholder="Chọn kho nhận hàng" class="meta-input w-full" :disabled="!isAdmin">
                    <el-option v-for="b in branches" :key="b.branchId" :label="b.branchName" :value="b.branchId" />
                  </el-select>
                  <span v-if="isImport && !isAdmin" class="text-[12px] text-[#8595A4] italic">Mặc định nhập hàng về chi nhánh của bạn.</span>
                </div>
              </el-col>

              <el-col :span="24" class="mt-4">
                <div class="flex flex-col gap-2">
                  <label class="text-[14px] font-bold text-[#1C2B33]">Lý do thực hiện phiếu:</label>
                  <input v-model="form.reason" class="meta-native-input" placeholder="Ví dụ: Bổ sung hàng bán dịp cuối tuần..." />
                </div>
              </el-col>
            </el-row>
          </div>

          <div class="border border-[#DEE3E9] p-6 rounded-[16px]">
            <h3 class="text-[16px] font-bold text-[#31A24C] mb-4 flex items-center gap-2"><span>2️⃣</span> Danh sách Hàng hóa</h3>

            <div class="flex flex-col md:flex-row gap-4 items-end mb-6 bg-[#E8F3FF] p-4 rounded-[12px] border border-[#ADD4E0]">
              <div class="flex-1 flex flex-col gap-2 w-full">
                <label class="text-[14px] font-bold text-[#0064E0]">Chọn Sản phẩm cần thêm</label>
                <div class="flex gap-2 mb-2">
                  <el-select v-model="currentItem.productId" filterable placeholder="Tìm và chọn từ danh sách..." class="meta-input flex-1">
                    <el-option v-for="p in products" :key="p.productId" :label="p.productName" :value="p.productId" />
                  </el-select>
                  <el-button v-if="isTransfer && !form.fromBranchId" class="meta-btn-warning" @click="openNearbyModalFromCart">
                    🔍 Quét Kho
                  </el-button>
                </div>
              </div>
              <div class="flex flex-col gap-2 w-[140px]">
                <label class="text-[14px] font-bold text-[#0064E0]">Số lượng</label>
                <el-input-number v-model="currentItem.quantity" :min="0.01" :controls="false" class="meta-input-number w-full" />
              </div>
              <el-button class="meta-btn-primary" @click="addItem" :disabled="isTransfer && !form.fromBranchId" :loading="isLoading">
                Thêm Vào Lưới
              </el-button>
            </div>

            <div v-if="isTransfer && form.fromBranchId" class="text-[13px] text-[#D6311F] mb-4 italic font-medium">
              * Đã khóa tuyến xuất từ: <b>{{ getBranchName(form.fromBranchId) }}</b>. Hệ thống sẽ tự động kiểm tra tồn kho tại đây khi bạn thêm món mới.
            </div>

            <el-table :data="form.details" class="meta-table-inner" style="width: 100%" empty-text="Chưa có sản phẩm nào trong phiếu">
              <el-table-column type="index" label="STT" width="80" align="center" />
              <el-table-column label="Tên sản phẩm" min-width="250">
                <template #default="{row}"><span class="font-medium text-[#1C2B33]">{{ getProductName(row.productId) }}</span></template>
              </el-table-column>
              <el-table-column label="Số lượng yêu cầu" width="160" align="center">
                <template #default="{row}">
                  <span class="text-[#0064E0] font-bold text-[18px]">{{ row.quantity }}</span> <span class="text-[#5D6C7B]">{{ getProductUnit(row.productId) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="Thao tác" width="100" align="center">
                <template #default="scope">
                  <el-button class="meta-btn-icon-danger" circle size="small" @click="removeItem(scope.$index)">✕</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          
        </div>
        <template #footer>
          <div class="flex gap-3 justify-end pt-4 border-t border-[#DEE3E9]">
            <el-button class="meta-btn-secondary" @click="dialogVisible = false">Hủy bỏ phiếu</el-button>
            <el-button class="meta-btn-primary" @click="handleCreateRequest" :loading="isLoading">
              Gửi Yêu Cầu Duyệt ({{ form.details.length }} món)
            </el-button>
          </div>
        </template>
      </el-dialog>

      <el-dialog v-model="nearbyModalVisible" title="🧭 Quét Tồn Kho Lân Cận" width="600px" class="meta-dialog">
        <div class="mb-4 text-[14px] text-[#5D6C7B]">Hệ thống đã rà soát các chi nhánh có sẵn mặt hàng này:</div>
        <el-table :data="nearbyBranches" v-loading="loadingNearby" class="meta-table-inner" empty-text="Sản phẩm này hiện đang hết hàng trên toàn hệ thống">
          <el-table-column label="Chi nhánh có hàng" prop="branchName" min-width="200">
            <template #default="{row}"><span class="font-medium text-[#1C2B33]">{{ row.branchName }}</span></template>
          </el-table-column>
          <el-table-column label="Cách đây" width="120" align="center">
            <template #default="{row}">
               <span :class="row.distance < 5000 ? 'text-[#31A24C] font-bold' : 'text-[#F7B928]'">
                {{ (row.distance / 1000).toFixed(1) }} km
               </span>
            </template>
          </el-table-column>
          <el-table-column label="Khả dụng" prop="availableQuantity" width="120" align="center">
            <template #default="{row}"><span class="text-[18px] font-bold text-[#0064E0]">{{ row.availableQuantity }}</span></template>
          </el-table-column>
          <el-table-column width="100" align="center">
            <template #default="{row}">
              <el-button class="meta-btn-secondary-sm" @click="selectNearbyBranch(row.branchId)">Chọn kho</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
/**
 * Stock Request Component
 * Handles the creation of Import, Export, and Transfer requests.
 * Includes inventory availability checks and nearby stock scanning.
 */
import { ref, onMounted, computed } from 'vue';
import api from '@/api/axios.js';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/stores/auth';

// --- State Management ---
const auth = useAuthStore();
const products = ref([]);
const branches = ref([]);
const isLoading = ref(false);
const dialogVisible = ref(false);

// Nearby Stock States
const nearbyModalVisible = ref(false);
const nearbyBranches = ref([]);
const loadingNearby = ref(false);

// Form States
const form = ref({ 
  transactionType: 'import', 
  fromBranchId: null, 
  toBranchId: null, 
  reason: '', 
  note: '', 
  details: [] 
});
const currentItem = ref({ productId: null, quantity: 1, unitPrice: 0 });

// --- Computed Properties ---
const isAdmin = computed(() => auth.role === 'ADMIN' || auth.role === 'ROLE_ADMIN');
const isTransfer = computed(() => form.value.transactionType === 'transfer');
const isExport = computed(() => form.value.transactionType === 'export');
const isImport = computed(() => form.value.transactionType === 'import');

const getDialogTitle = computed(() => {
  if (isImport.value) return 'Tạo Phiếu Yêu Cầu Nhập Kho';
  if (isExport.value) return 'Tạo Phiếu Yêu Cầu Xuất Kho';
  return 'Tạo Phiếu Điều Chuyển Nội Bộ';
});

// --- Data Fetching ---
const fetchData = async () => {
  isLoading.value = true;
  try {
    const [resProd, resBranch] = await Promise.all([
      api.get('/manager/products').catch(() => ({ data: { data: [] } })),
      api.get('/admin/branches/active').catch(() => ({ data: { data: [] } }))
    ]);
    
    products.value = resProd.data?.data?.content || resProd.data?.data || [];
    branches.value = resBranch.data?.data || [];
  } catch (error) {
    ElMessage.error('Lỗi tải dữ liệu hệ thống!');
  } finally {
    isLoading.value = false;
  }
};

// --- Form Actions ---
const openDialog = (type) => {
  const userBranchId = auth.user?.branchId || auth.user?.branch?.branchId || auth.branchId || Number(sessionStorage.getItem('branchId')) || null;
  
  form.value = { 
    transactionType: type, 
    fromBranchId: (type === 'export') ? userBranchId : null, 
    toBranchId: (type === 'import' || type === 'transfer') ? userBranchId : null, 
    reason: '', 
    details: [] 
  };
  
  currentItem.value = { productId: null, quantity: 1, unitPrice: 0 };
  dialogVisible.value = true;
};

/**
 * Validates and adds a product to the request list.
 * Performs inventory checks for Export and Transfer operations.
 */
const addItem = async () => {
  if (!validateItemInput()) return;

  if (isTransfer.value || isExport.value) {
    const isAvailable = await checkInventoryAvailability();
    if (!isAvailable) return;
  }

  updateItemList();
  resetCurrentItem();
};

const validateItemInput = () => {
  if (!currentItem.value.productId) { ElMessage.warning("Vui lòng chọn sản phẩm"); return false; }
  if (currentItem.value.quantity <= 0) { ElMessage.warning("Số lượng phải lớn hơn 0"); return false; }
  if ((isExport.value || isTransfer.value) && !form.value.fromBranchId) {
    ElMessage.warning("Vui lòng xác định kho nguồn trước!"); 
    return false;
  }
  return true;
};

const checkInventoryAvailability = async () => {
  isLoading.value = true;
  try {
    const res = await api.get('/manager/inventory/find-nearby', { params: { productId: currentItem.value.productId } });
    const stockInfo = (res.data?.data || []).find(b => b.branchId === form.value.fromBranchId);

    if (!stockInfo) {
      ElMessage.error(`Sản phẩm này không có tại ${getBranchName(form.value.fromBranchId)}!`);
      return false;
    }

    const currentQtyInList = form.value.details.find(d => d.productId === currentItem.value.productId)?.quantity || 0;
    const totalRequested = currentItem.value.quantity + currentQtyInList;

    if (stockInfo.availableQuantity < totalRequested) {
      ElMessage.warning(`Kho chỉ còn ${stockInfo.availableQuantity} ${getProductUnit(currentItem.value.productId)} khả dụng.`);
      return false;
    }
    return true;
  } catch (error) {
    return false;
  } finally {
    isLoading.value = false;
  }
};

const updateItemList = () => {
  const existing = form.value.details.find(d => d.productId === currentItem.value.productId);
  if (existing) {
    existing.quantity += currentItem.value.quantity; 
  } else {
    form.value.details.push({ ...currentItem.value });
  }
};

const resetCurrentItem = () => {
  currentItem.value = { productId: null, quantity: 1, unitPrice: 0 };
};

const removeItem = (index) => {
  form.value.details.splice(index, 1);
  if (form.value.details.length === 0 && isTransfer.value) {
    form.value.fromBranchId = null;
  }
};

// --- Nearby Scanning Logic ---
const openNearbyModalFromCart = async () => {
  if (!currentItem.value.productId) return ElMessage.warning("Chọn 1 sản phẩm trước!");
  nearbyModalVisible.value = true;
  loadingNearby.value = true;
  try {
    const res = await api.get('/manager/inventory/find-nearby', { params: { productId: currentItem.value.productId } });
    nearbyBranches.value = res.data?.data || [];
  } catch (error) {
    ElMessage.error("Lỗi tìm kiếm kho lân cận"); 
  } finally {
    loadingNearby.value = false;
  }
};

const selectNearbyBranch = (branchId) => {
  form.value.fromBranchId = branchId;
  nearbyModalVisible.value = false;
  addItem(); 
  ElMessage.success(`Đã xác định tuyến xuất từ ${getBranchName(branchId)}!`);
};

// --- Submission Logic ---
const handleCreateRequest = async () => {
  if (!validateFormSubmission()) return;

  isLoading.value = true;
  try {
    if (isTransfer.value) {
      await submitTransferRequests();
    } else {
      await submitStockRequest();
    }
    dialogVisible.value = false;
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'Lỗi gửi yêu cầu. Vui lòng kiểm tra lại.'); 
  } finally {
    isLoading.value = false;
  }
};

const validateFormSubmission = () => {
  if (form.value.details.length === 0) { ElMessage.warning('Thêm ít nhất 1 sản phẩm'); return false; }
  if (!form.value.reason.trim()) { ElMessage.warning('Nhập lý do tạo phiếu'); return false; }
  if (isImport.value && !form.value.toBranchId) { ElMessage.warning('Chọn kho nhận'); return false; }
  if (isExport.value && !form.value.fromBranchId) { ElMessage.warning('Chọn kho xuất'); return false; }
  if (isTransfer.value && (!form.value.fromBranchId || !form.value.toBranchId)) { ElMessage.warning('Chọn đủ kho đi và đến'); return false; }
  if (isTransfer.value && form.value.fromBranchId === form.value.toBranchId) { ElMessage.warning('Kho đi/đến không được trùng nhau'); return false; }
  return true;
};

const submitTransferRequests = async () => {
  const promises = form.value.details.map(item => {
    const payload = {
      productId: item.productId,
      fromBranchId: form.value.fromBranchId,
      toBranchId: form.value.toBranchId,
      quantity: item.quantity,
      reason: form.value.reason,
      priority: 'Normal'
    };
    return api.post('/manager/stock/transfer/request', payload);
  });
  await Promise.all(promises);
  ElMessage.success('Tạo phiếu điều chuyển thành công, vui lòng chờ duyệt'); 
};

const submitStockRequest = async () => {
  const res = await api.post('/manager/stock-requests', form.value);
  if (res.data?.success) { 
    ElMessage.success('Tạo phiếu thành công, vui lòng chờ duyệt'); 
  }
};

// --- Helper Functions ---
const getProductName = (id) => products.value.find(x => x.productId === id)?.productName || 'N/A';
const getProductUnit = (id) => products.value.find(x => x.productId === id)?.unit || '';
const getBranchName = (id) => branches.value.find(x => x.branchId === id)?.branchName || 'N/A';

onMounted(fetchData);
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap');
.font-meta { font-family: 'Montserrat', sans-serif; }

:deep(.meta-stat-card) { border-radius: 20px !important; border-left: none; border-right: none; border-bottom: none; background-color: #FFFFFF !important; padding: 24px !important; box-shadow: 0 12px 28px rgba(0,0,0,0.05) !important; }

:deep(.meta-btn-primary) { border-radius: 100px !important; background-color: #0064E0 !important; border-color: #0064E0 !important; color: #FFFFFF !important; font-family: 'Montserrat', sans-serif; font-weight: 500; font-size: 14px; padding: 10px 24px !important; height: auto; transition: all 0.2s ease; }
:deep(.meta-btn-primary:hover) { background-color: #0143B5 !important; transform: scale(1.02); }
:deep(.meta-btn-secondary) { background-color: transparent !important; color: #1C2B33 !important; border: 2px solid rgba(10, 19, 23, 0.12) !important; border-radius: 100px !important; font-family: 'Montserrat', sans-serif; font-weight: 500; font-size: 14px; padding: 10px 24px !important; height: auto; transition: all 0.2s ease; }
:deep(.meta-btn-warning) { background-color: #F7B928 !important; color: #FFFFFF !important; border: none !important; border-radius: 100px !important; font-family: 'Montserrat', sans-serif; font-weight: 600; padding: 10px 20px !important; height: auto; transition: all 0.2s ease; }
:deep(.meta-btn-secondary-sm) { background-color: rgba(0, 100, 224, 0.1) !important; color: #0064E0 !important; border: none !important; border-radius: 100px !important; font-weight: 600; padding: 6px 16px !important; transition: all 0.2s ease; }
:deep(.meta-btn-icon-danger) { background-color: rgba(228, 30, 63, 0.1) !important; color: #E41E3F !important; border: none !important; transition: all 0.2s ease; }
:deep(.meta-btn-success-sm) { border-radius: 100px !important; background-color: #31A24C !important; border-color: #31A24C !important; color: #FFFFFF !important; font-size: 12px; padding: 4px 12px !important; height: auto; }
:deep(.meta-btn-danger-sm) { border-radius: 100px !important; background-color: #E41E3F !important; border-color: #E41E3F !important; color: #FFFFFF !important; font-size: 12px; padding: 4px 12px !important; height: auto; }
:deep(.meta-btn-primary-sm) { border-radius: 100px !important; background-color: #0064E0 !important; border-color: #0064E0 !important; color: #FFFFFF !important; font-size: 12px; padding: 4px 12px !important; height: auto; }
:deep(.meta-tag-warning) { background-color: rgba(255,226,0,0.15); color: #9C6C00; border-radius: 100px; font-weight:700; border:none; padding: 4px 12px; height:auto; }
:deep(.meta-tag-info) { background-color: rgba(0,100,224,0.15); color: #0064E0; border-radius: 100px; font-weight:700; border:none; padding: 4px 12px; height:auto; }
:deep(.meta-tag-success) { background-color: rgba(36,228,0,0.15); color: #007D1E; border-radius: 100px; font-weight:700; border:none; padding: 4px 12px; height:auto; }
:deep(.meta-tag-error) { background-color: rgba(255,123,145,0.15); color: #E41E3F; border-radius: 100px; font-weight:700; border:none; padding: 4px 12px; height:auto; }
:deep(.meta-input .el-select__wrapper) { border-radius: 8px !important; border: 1px solid #CED0D4 !important; box-shadow: none !important; }

.meta-native-input { width: 100%; padding: 10px 14px; border-radius: 8px; border: 1px solid #CED0D4; font-family: 'Montserrat', sans-serif; font-size: 14px; color: #1C2B33; transition: border-color 0.2s, box-shadow 0.2s; }
.meta-native-input:focus { outline: none; border-color: #0064E0; box-shadow: 0 0 0 3px rgba(0, 100, 224, 0.2); }

:deep(.meta-input .el-input__wrapper), :deep(.meta-input-number .el-input__wrapper) { border-radius: 8px !important; border: 1px solid #CED0D4 !important; box-shadow: none !important; padding: 8px 12px; }
:deep(.meta-input .el-input__wrapper.is-focus), :deep(.meta-input-number .el-input__wrapper.is-focus) { border-color: #0064E0 !important; box-shadow: 0 0 0 3px rgba(0, 100, 224, 0.2) !important; }

:deep(.meta-table-inner th.el-table__cell) { background-color: #FFFFFF !important; color: #5D6C7B !important; font-weight: 500 !important; border-bottom: 1px solid #DEE3E9 !important; padding: 16px 0 !important; font-size: 14px; }
:deep(.meta-table-inner td.el-table__cell) { border-bottom: 1px solid #F1F4F7 !important; padding: 16px 0 !important; }

:deep(.meta-dialog) { border-radius: 24px !important; font-family: 'Montserrat', sans-serif; }
:deep(.meta-dialog .el-dialog__header) { border-bottom: 1px solid #DEE3E9; padding-bottom: 20px; margin-right: 0; }
:deep(.meta-dialog .el-dialog__title) { font-weight: 600; color: #1C2B33; font-size: 20px; }
</style>