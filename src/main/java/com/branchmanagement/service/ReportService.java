package com.branchmanagement.service;

import com.branchmanagement.dto.DashboardReportResponse;
import java.io.OutputStream;

public interface ReportService {
    DashboardReportResponse getDashboardStatistics();
    void exportToExcel(OutputStream outputStream);
}