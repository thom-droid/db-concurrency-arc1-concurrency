package com.example.simplebooking.tx;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/tx")
@RestController
public class TxLabController {

    private final TxLabService txLabService;

    @PostMapping("/runtime")
    public void runtime() {
        txLabService.rollbackOnRuntimeException();
    }

    @PostMapping("/checked")
    public void checked() throws Exception {
        txLabService.noRollbackOnCheckedException();
    }

    @PostMapping("/rollbackfor-checked")
    public void rollbackForChecked() throws Exception {
        txLabService.rollbackWithCheckedException();
    }

    @PostMapping("/runtime-try-catch")
    public void tryCatchRuntimeException() {
        txLabService.tryingCatchException();
    }

    @PostMapping("/tx-during-catch")
    public void transactionDuringCatch() {
        txLabService.tryingTransactionDuringCatch();
    }

    @PostMapping("/rollback-only")
    public void rollbackOnly() {
        txLabService.cannotTxWhenMarkedRollbackOnly();
    }
}
