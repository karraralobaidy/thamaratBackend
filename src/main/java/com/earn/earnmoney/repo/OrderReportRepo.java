package com.earn.earnmoney.repo;

import com.earn.earnmoney.model.OrderReport;
import com.earn.earnmoney.model.OrderReport.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderReportRepo extends JpaRepository<OrderReport, Long> {
    List<OrderReport> findByStatus(ReportStatus status);

    List<OrderReport> findByOrder_Id(Long orderId);
}
