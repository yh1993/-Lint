package com.example;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.Issue;

import java.util.Arrays;
import java.util.List;

/**
 * Created by yanghao on 17-8-21.
 */

public class MTIssueRegistry extends IssueRegistry {
    @Override
    public List<Issue> getIssues() {
        return Arrays.asList(MainThreadDetector.ISSUE);
    }
}
