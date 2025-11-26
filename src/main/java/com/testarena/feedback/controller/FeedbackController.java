package com.testarena.feedback.controller;

import com.testarena.feedback.dto.FeedbackResponse;
import com.testarena.feedback.service.FeedbackOrchestrator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tests")
public class FeedbackController {
    private final FeedbackOrchestrator orchestrator;

    public FeedbackController(FeedbackOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    // GET /tests/feedback/{testId}
    @GetMapping("/feedback/{testId}")
    public ResponseEntity<FeedbackResponse> getFeedback(@PathVariable String testId) {
        // For demo: orchestrator loads test results (or you can supply them)
        FeedbackResponse response = orchestrator.provideFeedbackFor(testId);
        return ResponseEntity.ok(response);
    }
}
