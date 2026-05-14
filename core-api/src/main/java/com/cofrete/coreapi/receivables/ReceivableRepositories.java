package com.cofrete.coreapi.receivables;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface CustomerRepository extends JpaRepository<Customer, String> {

    Optional<Customer> findByAccountIdAndId(String accountId, String id);
}

interface ReceivableRepository extends JpaRepository<Receivable, String> {

    Optional<Receivable> findByAccountIdAndId(String accountId, String id);

    List<Receivable> findByAccountIdOrderByDueDateAscCreatedAtAsc(String accountId);

    List<Receivable> findByAccountIdAndDueDateBeforeAndStatusInOrderByDueDateAscCreatedAtAsc(
        String accountId,
        LocalDate dueDate,
        List<ReceivableStatus> statuses
    );

    List<Receivable> findByCustomerOrderByDueDateAscCreatedAtAsc(Customer customer);
}

interface ReceivablePaymentRepository extends JpaRepository<ReceivablePayment, String> {

    List<ReceivablePayment> findByReceivableOrderByCreatedAtAsc(Receivable receivable);
}

interface ReceivableStatusChangeRepository extends JpaRepository<ReceivableStatusChange, String> {

    List<ReceivableStatusChange> findByReceivableOrderByChangedAtAsc(Receivable receivable);
}
