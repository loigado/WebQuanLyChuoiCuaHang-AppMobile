import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import Login from '@/views/auth/Login.vue';

const routes = [
  { path: '/login', component: Login, meta: { title: 'Đăng nhập' } },
  { 
    path: '/', 
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: 'Bảng điều khiển' }
      },
      
      // ==========================================
      // 1. NHÓM ADMIN (QUẢN TRỊ HỆ THỐNG)
      // ==========================================
      { path: 'admin/branches', component: () => import('@/views/admin/BranchList.vue'), meta: { title: 'Chi nhánh', roles: ['ROLE_ADMIN', 'ADMIN'] }},
      { path: 'admin/branches/new', component: () => import('@/views/admin/BranchForm.vue'), meta: { title: 'Thêm chi nhánh', roles: ['ROLE_ADMIN', 'ADMIN'] }},
      { path: 'admin/branches/edit/:id', component: () => import('@/views/admin/BranchForm.vue'), meta: { title: 'Sửa chi nhánh', roles: ['ROLE_ADMIN', 'ADMIN'] }},
      { path: 'admin/users', component: () => import('@/views/admin/UserList.vue'), meta: { title: 'Quản lý nhân sự', roles: ['ROLE_ADMIN', 'ADMIN', 'ROLE_MANAGER', 'MANAGER'] }},
      { path: 'admin/users/new', component: () => import('@/views/admin/UserForm.vue'), meta: { title: 'Thêm nhân sự', roles: ['ROLE_ADMIN', 'ADMIN', 'ROLE_MANAGER', 'MANAGER'] }},
      { path: 'admin/users/edit/:id', component: () => import('@/views/admin/UserForm.vue'), meta: { title: 'Cập nhật nhân sự', roles: ['ROLE_ADMIN', 'ADMIN', 'ROLE_MANAGER', 'MANAGER'] }},
      { path: 'admin/config', component: () => import('@/views/admin/SystemConfig.vue'), meta: { title: 'Cấu hình hệ thống', roles: ['ROLE_ADMIN', 'ADMIN'] }},
      { 
        path: 'admin/cameras', 
        name: 'AdminCameras',
        component: () => import('@/views/manager/Cameras.vue'),
        meta: { title: 'Quản lý Camera', roles: ['ROLE_ADMIN', 'ADMIN'] } 
      },
      { 
        path: 'admin/audit-logs', 
        name: 'AuditLogs', 
        component: () => import('@/views/admin/SystemLogs.vue'), 
        meta: { title: 'Nhật ký hệ thống', roles: ['ROLE_ADMIN', 'ADMIN'] } 
      },
      // ✅ ĐÃ THÊM: Route báo cáo tổng hợp Admin (UC 1.6)
      { 
        path: 'admin/system-analytics', 
        name: 'SystemAnalytics', 
        component: () => import('@/views/admin/SystemAnalytics.vue'), 
        meta: { title: 'Phân tích hệ thống', roles: ['ROLE_ADMIN', 'ADMIN'] } 
      },
      { 
        path: 'admin/inventory-management', 
        name: 'AdminInventoryManagement', 
        component: () => import('@/views/admin/InventoryManagement.vue'), 
        meta: { title: 'Quản lý kho hệ thống', roles: ['ROLE_ADMIN', 'ADMIN'] } 
      },
      { 
        path: 'admin/announcements', 
        name: 'AdminAnnouncements', 
        component: () => import('@/views/admin/Announcements.vue'), 
        meta: { title: 'Quản lý thông báo', roles: ['ROLE_ADMIN', 'ADMIN'] } 
      },

      // ==========================================
      // 2. NHÓM MANAGER (VẬN HÀNH CHI NHÁNH)
      // ==========================================
      { 
        path: 'manager/categories', 
        name: 'CategoryManage', 
        component: () => import('@/views/manager/CategoryManage.vue'), 
        meta: { title: 'Danh mục Sản phẩm', roles: ['ROLE_ADMIN', 'ADMIN', 'ROLE_MANAGER', 'MANAGER'] } 
      },
      { 
        path: 'manager/products', 
        name: 'ProductManage', 
        component: () => import('@/views/manager/ProductManage.vue'), 
        meta: { title: 'Quản lý Sản phẩm', roles: ['ROLE_ADMIN', 'ADMIN', 'ROLE_MANAGER', 'MANAGER'] } 
      },
      
      // Quản trị tồn kho nâng cao
      { 
        path: 'manager/inventory-detail', 
        name: 'InventoryDetail',
        component: () => import('@/views/manager/DetailedInventory.vue'), 
        meta: { title: 'Quản trị kho chi tiết', roles: ['ROLE_ADMIN', 'ADMIN', 'ROLE_MANAGER', 'MANAGER'] } 
      },
      
      // Kiểm kê kho định kỳ (Stocktake)
      { 
        path: 'manager/stocktake', 
        name: 'Stocktake',
        component: () => import('@/views/manager/Stocktake.vue'), 
        meta: { title: 'Kiểm kê kho', roles: ['ROLE_ADMIN', 'ADMIN', 'ROLE_MANAGER', 'MANAGER'] } 
      },

      // Lịch sử biến động kho
      { 
        path: 'manager/inventory-history', 
        name: 'InventoryHistory',
        component: () => import('@/views/manager/InventoryHistory.vue'), 
        meta: { title: 'Lịch sử biến động kho', roles: ['ROLE_ADMIN', 'ADMIN', 'ROLE_MANAGER', 'MANAGER'] } 
      },

      { 
        path: 'manager/employees', 
        name: 'ManagerEmployees',
        component: () => import('@/views/admin/UserList.vue'), 
        meta: { title: 'Nhân sự chi nhánh', roles: ['ROLE_MANAGER', 'MANAGER'] } 
      },
      { 
        path: 'manager/reports', 
        name: 'BranchReports',
        component: () => import('@/views/manager/BranchReport.vue'), 
        meta: { title: 'Báo cáo chi nhánh', roles: ['ROLE_MANAGER', 'MANAGER'] } 
      },
      { 
        path: 'manager/attendance-report', 
        name: 'BranchAttendanceReport',
        component: () => import('@/views/manager/BranchAttendanceReport.vue'), 
        meta: { title: 'Báo cáo công & lương', roles: ['ROLE_MANAGER', 'MANAGER'] } 
      },
      { path: 'manager/inventory', component: () => import('@/views/manager/Inventory.vue'), meta: { title: 'Tồn kho', roles: ['ROLE_ADMIN', 'ADMIN', 'ROLE_MANAGER', 'MANAGER'] }},
      { path: 'manager/attendance', component: () => import('@/views/manager/Attendance.vue'), meta: { title: 'Chấm công chi nhánh', roles: ['ROLE_ADMIN', 'ADMIN', 'ROLE_MANAGER', 'MANAGER'] }},
      { path: 'manager/stock-requests', name: 'StockRequests', component: () => import('@/views/manager/StockRequest.vue'), meta: { title: 'Yêu cầu kho', roles: ['ROLE_ADMIN', 'ADMIN', 'ROLE_MANAGER', 'MANAGER'] }},
      { path: 'manager/shifts', name: 'ShiftManagement', component: () => import('@/views/admin/ShiftList.vue'), meta: { title: 'Ca làm việc', roles: ['ROLE_ADMIN', 'ADMIN', 'ROLE_MANAGER', 'MANAGER'] }},
      { path: 'manager/cameras', component: () => import('@/views/manager/Cameras.vue'), meta: { title: 'Giám sát Camera', roles: ['ROLE_ADMIN', 'ADMIN', 'ROLE_MANAGER', 'MANAGER'] }},
      
      // ✅ THÊM MỚI: Quản lý đơn xin nghỉ phép (Duyệt/Từ chối)
      { 
        path: 'manager/leave-requests', 
        name: 'LeaveManagement', 
        component: () => import('@/views/manager/LeaveManagement.vue'), 
        meta: { title: 'Quản lý nghỉ phép', roles: ['ROLE_ADMIN', 'ADMIN', 'ROLE_MANAGER', 'MANAGER'] } 
      },

      // ==========================================
      // 3. NHÓM KẾ TOÁN (KIỂM SOÁT TÀI CHÍNH)
      // ==========================================
      { 
        path: 'accountant/inventory-data', 
        name: 'AccountantInventory',
        component: () => import('@/views/accountant/InventoryData.vue'), 
        meta: { title: 'Dữ liệu kho hệ thống', roles: ['ROLE_ACCOUNTANT', 'ACCOUNTANT', 'ROLE_ADMIN', 'ADMIN'] } 
      },
      { 
        path: 'accountant/financial-reports', 
        name: 'FinancialReports',
        component: () => import('@/views/accountant/FinancialReport.vue'), 
        meta: { title: 'Báo cáo tài chính kho', roles: ['ROLE_ACCOUNTANT', 'ACCOUNTANT', 'ROLE_ADMIN', 'ADMIN'] } 
      },
      { 
        path: 'accountant/attendance-reports', 
        name: 'AccountantAttendance',
        component: () => import('@/views/accountant/AttendanceReport.vue'), 
        meta: { title: 'Báo cáo chấm công tổng', roles: ['ROLE_ACCOUNTANT', 'ACCOUNTANT', 'ROLE_ADMIN', 'ADMIN'] } 
      },
      
      // Duyệt phiếu kho
      { 
        path: 'admin/approve-inventory', 
        name: 'ApproveInventory', 
        component: () => import('@/views/admin/ApproveInventory.vue'), 
        meta: { title: 'Duyệt phiếu kho', roles: ['ROLE_ADMIN', 'ADMIN', 'ROLE_ACCOUNTANT', 'ACCOUNTANT'] }
      },

      { 
        path: 'admin/approval-history', 
        name: 'ApprovalHistory', 
        component: () => import('@/views/admin/ApprovalHistory.vue'), 
        meta: { title: 'Lịch sử duyệt kho', roles: ['ROLE_ADMIN', 'ADMIN', 'ROLE_ACCOUNTANT', 'ACCOUNTANT'] }
      },

      // ==========================================
      // 4. NHÓM NHÂN VIÊN (DỮ LIỆU CÁ NHÂN)
      // ==========================================
      // ✅ ĐÃ SỬA: Dùng view riêng cho Employee thay vì reuse Manager views
      { 
        path: 'employee/attendance', 
        name: 'EmployeeAttendance',
        component: () => import('@/views/employee/MyAttendance.vue'), 
        meta: { title: 'Chấm công cá nhân', roles: ['ROLE_EMPLOYEE', 'EMPLOYEE'] } 
      },
      { 
        path: 'employee/my-shifts', 
        name: 'EmployeeShifts',
        component: () => import('@/views/employee/MyShifts.vue'), 
        meta: { title: 'Lịch làm việc của tôi', roles: ['ROLE_EMPLOYEE', 'EMPLOYEE'] } 
      },
      // ✅ ĐÃ THÊM: Đơn xin nghỉ phép cho Employee (UC 4)
      { 
        path: 'employee/leave-requests', 
        name: 'EmployeeLeaveRequests',
        component: () => import('@/views/employee/MyLeaveRequests.vue'), 
        meta: { title: 'Đơn nghỉ phép', roles: ['ROLE_EMPLOYEE', 'EMPLOYEE'] } 
      }
    ]
  },
  { 
    path: '/403', 
    name: 'Forbidden',
    component: { template: '<h2 style="text-align:center; padding: 50px;">403 - Không có quyền truy cập!</h2>' } 
  }
];

const router = createRouter({ 
  history: createWebHistory(), 
  routes 
});

/**
 * ✅ Sử dụng RETURN thay vì gọi next(). 
 * Đây là cú pháp chuẩn của Vue Router 4.
 */
router.beforeEach((to) => {
  const auth = useAuthStore();
  
  // 1. Cập nhật tiêu đề trang
  if (to.meta.title) {
    document.title = to.meta.title + ' - Branch Management';
  }

  // ✅ ĐÃ SỬA: Dùng to.matched.some() để check cả parent route
  // 2. Kiểm tra yêu cầu Đăng nhập (check cả route cha)
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  if (requiresAuth && !auth.token) {
    return '/login'; // Chuyển hướng về Login
  }

  // 3. Kiểm tra quyền truy cập (Roles)
  if (to.meta.roles) {
    if (!to.meta.roles.includes(auth.role)) {
      return { name: 'Forbidden' }; // Chuyển hướng sang trang 403
    }
  }

  // Nếu mọi thứ ổn, không cần return gì cả
});

export default router;