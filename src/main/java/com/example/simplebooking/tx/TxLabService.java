package com.example.simplebooking.tx;

import com.example.simplebooking.audit.AuditService;
import com.example.simplebooking.tx.exception.CheckedTxException;
import com.example.simplebooking.tx.exception.CheckedTxSubException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class TxLabService {

    private final AuditService auditService;

    /**
     * <p>
     *     {@code Transactional}은 기본적으로 런타임 예외와 Error에 롤백을 시도한다.
     *     이 메서드는 {@link AuditService#log(String)}를 롤백하게 된다.
     * </p>
     */
    @Transactional
    public void rollbackOnRuntimeException() {
        auditService.log("saved before rollback?");
        throw new IllegalStateException("unchecked boom");
    }

    /**
     * <p>
     *     checked 예외는 기본적으로 처리하지 않기 때문에 이 메서드에서는 첫번째 트랜잭션은 롤백되지 않는다.
     * </p>
     */
    @Transactional
    public void noRollbackOnCheckedException() throws Exception {
        auditService.log("this is not rolled back");
        throw new IOException("checked Exception");
    }

    /**
     * <p>
     *     하지만 어노테이션에 명시함으로써 checked 예외도 롤백 대상에 포함시킬 수 있다.
     *     checked 예외를 상속하는 자식 클래스 모두 대상이 될 수 있다.
     * </p>
     */
    @Transactional(rollbackFor = CheckedTxException.class)
    public void rollbackWithCheckedException() throws Exception {
        auditService.log("is this rolled back then?");
        throw new CheckedTxSubException("checked but might be caught exception");
    }

    /**
     *  메서드 흐름 내에서 예외가 발생해도 catch하여 처리한다면 정상적으로 트랜잭션을 수행한다.
     */
    @Transactional
    public void tryingCatchException() {
        auditService.log("before try catch");
        try {
            throw new IllegalArgumentException("intentional boom");
        } catch (Exception e) {
            // do nothing
        }
        auditService.log("after try catch");
    }

    /**
     * 예외를 처리해주면 트랜잭션은 모두 성공하게 된다.
     *
     */
    @Transactional
    public void tryingTransactionDuringCatch() {
        try {
            auditService.log("before try2");
            throw new IllegalStateException("unintentional boom");
        } catch (Exception e) {
            auditService.log("during catch");
        }
        auditService.log("after try2");
    }

    /**
     * <p>
     *     하지만 JPA/Hibernate 에서 심각하다고 판단하는 예외
     *     아래 예시에서는 {@link ConstraintViolationException}와 같은 경우
     *     트랜잭션이 실패했을 때 해당 메서드의 트랜잭션을 rollback only로 설정하기 때문에
     *     추가적인 동작을 행할 수 없다.
     * </p>
     * <p>
     *     따라서 아래 메서드에 기록된 트랜잭션은 모두 실패한다.
     * </p>
     *
     */
    @Transactional
    public void cannotTxWhenMarkedRollbackOnly() {
        auditService.log("rollback only so not gonna be saved");
        try {
            auditService.log(null);
        } catch (Exception e) {
            auditService.log("I will do my work");
        }
    }
}
